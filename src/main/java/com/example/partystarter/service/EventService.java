package com.example.partystarter.service;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.*;
import com.example.partystarter.model.enums.EventFilter;
import com.example.partystarter.model.request.PostEventRequest;
import com.example.partystarter.model.request.PostEventRequest.LocationRequest;
import com.example.partystarter.model.request.PutEventRequest;
import com.example.partystarter.model.response.EventResponse;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.repo.UserRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final DrinkRepository drinkRepository;
    private final IngredientRepository ingredientRepository;
    private final ArtistService artistService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public EventResponse getEvent(Integer id) {
        Event event = eventRepository
                .findById(id)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Cant find event by id"));

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

} 