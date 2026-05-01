package com.example.partystarter.service;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.*;
import com.example.partystarter.model.enums.EventFilter;
import com.example.partystarter.model.request.PostEventRequest;
import com.example.partystarter.model.request.PostEventRequest.LocationRequest;
import com.example.partystarter.model.request.PutEventRequest;
import com.example.partystarter.model.response.EventResponse;
import com.example.partystarter.model.response.ShareLinkResponse;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.repo.UserRepository;
import com.example.partystarter.utils.ConvertUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final DrinkRepository drinkRepository;
    private final IngredientRepository ingredientRepository;
    private final ArtistService artistService;
    private final UserRepository userRepository;
    private final String frontendBaseUrl;

    public EventService(EventRepository eventRepository,
                        DrinkRepository drinkRepository,
                        IngredientRepository ingredientRepository,
                        ArtistService artistService,
                        UserRepository userRepository,
                        @Value("${application.share.frontend-base-url}") String frontendBaseUrl) {
        this.eventRepository = eventRepository;
        this.drinkRepository = drinkRepository;
        this.ingredientRepository = ingredientRepository;
        this.artistService = artistService;
        this.userRepository = userRepository;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Integer id) {
        Event event = eventRepository
                .findById(id)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Cant find event by id"));

        // Public events are readable without auth. Private events are creator-only.
        if (Boolean.TRUE.equals(event.getIsPrivate())) {
            User currentUser = getCurrentUserOrNull();
            if (currentUser == null) {
                throw new ResourceException(HttpStatus.UNAUTHORIZED, "Authentication required to view this event");
            }
            User creator = event.getCreator();
            if (creator == null || !creator.getId().equals(currentUser.getId())) {
                // creator == null (orphaned legacy row) → no one can claim ownership; 403 is correct.
                throw new ResourceException(HttpStatus.FORBIDDEN, "You are not the creator of this event");
            }
        }

        return ConvertUtils.mapEventToResponse(event);
    }

    @Transactional
    public EventResponse saveEvent(PostEventRequest request) {
        User user = getCurrentUser();

        List<Drink> drinks = validateAndGetDrinks(request.getDrinks());
        List<Ingredient> ingredients = validateAndGetIngredients(request.getIngredients());
        Set<Artist> artists = artistService.getOrCreateArtists(request.getArtists());
        Location location = createLocationFromRequest(request.getLocation());

        Event event = buildEventFromRequest(request, user, drinks, ingredients, artists, location);
        event = eventRepository.save(event);

        return ConvertUtils.mapEventToResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(Integer id, PutEventRequest request) {
        Event event = eventRepository
                .findById(id)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Event not found"));

        User currentUser = getCurrentUser();
        if (!event.getCreator().getId().equals(currentUser.getId())) {
            throw new ResourceException(HttpStatus.FORBIDDEN, "You are not the creator of this event");
        }

        List<Drink> drinks = validateAndGetDrinks(request.getDrinks());
        List<Ingredient> ingredients = validateAndGetIngredients(request.getIngredients());
        Set<Artist> artists = artistService.getOrCreateArtists(request.getArtists());
        Location location = createLocationFromPutRequest(request.getLocation());

        event.setName(Optional.ofNullable(request.getName()).orElse("New event"));
        event.setDate(request.getDate());
        event.setTime(request.getTime());
        event.setLocation(location);
        event.setArtists(artists);
        event.setDrinks(drinks);
        event.setIngredients(ingredients);
        event.setFoodItems(Optional.ofNullable(request.getFood()).orElse(new ArrayList<>()));
        event.setIsPrivate(Optional.ofNullable(request.getIsPrivate()).orElse(false));

        event = eventRepository.save(event);
        return ConvertUtils.mapEventToResponse(event);
    }

    @Transactional
    public void deleteEvent(Integer id) {
        Event event = eventRepository
                .findById(id)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Event not found"));

        User currentUser = getCurrentUser();
        if (!event.getCreator().getId().equals(currentUser.getId())) {
            throw new ResourceException(HttpStatus.FORBIDDEN, "You are not the creator of this event");
        }

        // Use delete(entity) rather than deleteById so JPA cascade rules fire
        // on the managed instance, clearing event_artists, event_drinks,
        // event_ingredients, and event_food_items join/collection rows.
        eventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEvents(EventFilter filter) {
        return switch (filter) {
            case ME -> getEventsByCreator(getCurrentUser());
            default -> throw new ResourceException(
                HttpStatus.BAD_REQUEST,
                "Unsupported filter type: " + filter
            );
        };
    }

    private List<Drink> validateAndGetDrinks(List<Integer> drinkIds) {
        if (isEmpty(drinkIds)) {
            return new ArrayList<>();
        }

        List<Drink> drinks = drinkRepository.findAllById(drinkIds);
        if (drinks.size() < drinkIds.size()) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Can't find all drinks by id");
        }
        return drinks;
    }

    private List<Ingredient> validateAndGetIngredients(List<Integer> ingredientIds) {
        if (isEmpty(ingredientIds)) {
            return new ArrayList<>();
        }

        List<Ingredient> ingredients = ingredientRepository.findAllById(ingredientIds);
        if (ingredients.size() < ingredientIds.size()) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Can't find all ingredients by id");
        }
        return ingredients;
    }

    private Location createLocationFromRequest(LocationRequest locationRequest) {
        return Optional.ofNullable(locationRequest)
                .map(req -> Location.builder()
                        .latitude(req.getLat())
                        .longitude(req.getLng())
                        .description(req.getLocationDescription())
                        .build())
                .orElse(null);
    }

    private Location createLocationFromPutRequest(PutEventRequest.LocationRequest locationRequest) {
        return Optional.ofNullable(locationRequest)
                .map(req -> Location.builder()
                        .latitude(req.getLat())
                        .longitude(req.getLng())
                        .description(req.getLocationDescription())
                        .build())
                .orElse(null);
    }

    private Event buildEventFromRequest(
            PostEventRequest request,
            User creator,
            List<Drink> drinks,
            List<Ingredient> ingredients,
            Set<Artist> artists,
            Location location) {

        return Event.builder()
                .name(Optional.ofNullable(request.getName()).orElse("New event"))
                .date(request.getDate())
                .time(request.getTime())
                .location(location)
                .artists(artists)
                .drinks(drinks)
                .ingredients(ingredients)
                .foodItems(Optional.ofNullable(request.getFood()).orElse(new ArrayList<>()))
                .isPrivate(Optional.ofNullable(request.getIsPrivate()).orElse(false))
                .creator(creator)
                .build();
    }


    private List<EventResponse> getEventsByCreator(User creator) {
        return eventRepository.findByCreatorOrderByCreatedAtDesc(creator)
                .stream()
                .map(ConvertUtils::mapEventToResponse)
                .toList();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();

        return userRepository.getByUsername(username)
                .orElseThrow(() -> new ResourceException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
                ));
    }

    @Transactional
    public ShareLinkResponse issueShareToken(Integer eventId) {
        Event event = loadEventForCreator(eventId);
        if (event.getShareToken() == null) {
            persistFreshToken(event);
        }
        return buildShareLinkResponse(event);
    }

    @Transactional
    public ShareLinkResponse rotateShareToken(Integer eventId) {
        Event event = loadEventForCreator(eventId);
        persistFreshToken(event);
        return buildShareLinkResponse(event);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventByShareToken(String token) {
        Event event = eventRepository.findByShareToken(token)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Share link is invalid or has been revoked"));
        return ConvertUtils.mapEventToResponse(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getPublicEvents(String q, boolean includePast, Pageable pageable) {
        LocalDate since = includePast ? null : LocalDate.now();
        return eventRepository.findPublic(q, since, pageable)
                .map(ConvertUtils::mapEventToResponse);
    }

    private Event loadEventForCreator(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Event not found"));

        User currentUser = getCurrentUser();
        if (!event.getCreator().getId().equals(currentUser.getId())) {
            throw new ResourceException(HttpStatus.FORBIDDEN, "You are not the creator of this event");
        }
        return event;
    }

    private void persistFreshToken(Event event) {
        // UUID collisions on a 122-bit random space are vanishingly unlikely.
        // We do not retry here: after a DataIntegrityViolationException the
        // Hibernate session is rollback-only, so a second saveAndFlush in the
        // same transaction throws TransactionSystemException rather than
        // succeeding. If a collision ever does happen, surfacing 500 to the
        // (creator-only) caller is acceptable.
        event.setShareToken(UUID.randomUUID().toString());
        eventRepository.saveAndFlush(event);
    }

    private ShareLinkResponse buildShareLinkResponse(Event event) {
        String url = frontendBaseUrl + "/shared/" + event.getShareToken();
        return new ShareLinkResponse(event.getShareToken(), url);
    }

    /**
     * Same as getCurrentUser() but returns null if no authentication is present
     * (the SecurityContext is empty on permitAll endpoints).
     */
    private User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetails ud)) {
            return null;
        }
        return userRepository.getByUsername(ud.getUsername()).orElse(null);
    }

}
