package com.example.partystarter.service;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.*;
import com.example.partystarter.model.request.PostEventRequest;
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
        // Get the authenticated user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        
        User user = userRepository.getByUsername(username)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "User not found"));

        // Validate and get drinks
        List<Drink> drinks = drinkRepository.findAllById(request.getDrinks());
        if (drinks.size() < request.getDrinks().size()) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Can't find all drinks by id");
        }

        // Validate and get ingredients
        List<Ingredient> ingredients = new ArrayList<>();
        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            ingredients = ingredientRepository.findAllById(request.getIngredients());
            if (ingredients.size() < request.getIngredients().size()) {
                throw new ResourceException(HttpStatus.NOT_FOUND, "Can't find all ingredients by id");
            }
        }

        // Get or create artists
        Set<Artist> artists = artistService.getOrCreateArtists(request.getArtists());

        // Create location
        Location location = null;
        if (request.getLocation() != null) {
            location = Location.builder()
                    .latitude(request.getLocation().getLat())
                    .longitude(request.getLocation().getLng())
                    .description(request.getLocation().getLocationDescription())
                    .build();
        }

        // Build and save event
        Event event = Event.builder()
                .name(request.getName() == null ? "New event" : request.getName())
                .date(request.getDate())
                .time(request.getTime())
                .location(location)
                .artists(artists)
                .drinks(drinks)
                .ingredients(ingredients)
                .foodItems(request.getFood() != null ? request.getFood() : new ArrayList<>())
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .creator(user)
                .build();

        event = eventRepository.save(event);
        return ConvertUtils.mapEventToResponse(event);
    }

    public List<EventResponse> getEvents(EventFilter filter) {
        switch (filter) {
            case ME -> {
                User user = getCurrentUser();
                return eventRepository.findByCreatorOrderByCreatedAtDesc(user)
                        .stream()
                        .map(ConvertUtils::mapEventToResponse)
                        .toList();
            }
            default -> throw new ResourceException(
                HttpStatus.BAD_REQUEST, 
                "Unsupported filter type: " + filter
            );
        }
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

    // public List<EventResponse> getMyEvents() {
    //     // Get the authenticated user
    //     Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    //     String username = ((UserDetails) principal).getUsername();
        
    //     User user = userRepository.getByUsername(username)
    //             .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "User not found"));

    //     return eventRepository.findByCreatorOrderByCreatedAtDesc(user)
    //             .stream()
    //             .map(ConvertUtils::mapEventToResponse)
    //             .toList();
    // }
} 