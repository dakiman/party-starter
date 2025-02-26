package com.example.partystarter.service;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.*;
import com.example.partystarter.model.request.PostEventRequest;
import com.example.partystarter.model.request.PostEventRequest.LocationRequest;
import com.example.partystarter.model.response.EventResponse;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.repo.UserRepository;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import com.example.partystarter.model.enums.EventFilter;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final DrinkRepository drinkRepository;
    private final IngredientRepository ingredientRepository;
    private final ArtistService artistService;
    private final UserRepository userRepository;

    public EventResponse getEvent(Integer id) {
        Event event = eventRepository
                .findById(id)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Cant find event by id"));

        return ConvertUtils.mapEventToResponse(event);
    }

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
        if (drinkIds == null || drinkIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Drink> drinks = drinkRepository.findAllById(drinkIds);
        if (drinks.size() < drinkIds.size()) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Can't find all drinks by id");
        }
        return drinks;
    }

    private List<Ingredient> validateAndGetIngredients(List<Integer> ingredientIds) {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Ingredient> ingredients = ingredientRepository.findAllById(ingredientIds);
        if (ingredients.size() < ingredientIds.size()) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Can't find all ingredients by id");
        }
        return ingredients;
    }

    private Location createLocationFromRequest(LocationRequest locationRequest) {
        if (locationRequest == null) {
            return null;
        }
        
        return Location.builder()
                .latitude(locationRequest.getLat())
                .longitude(locationRequest.getLng())
                .description(locationRequest.getLocationDescription())
                .build();
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