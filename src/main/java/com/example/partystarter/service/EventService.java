package com.example.partystarter.service;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.*;
import com.example.partystarter.model.request.PostEventRequest;
import com.example.partystarter.model.response.EventResponse;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.EventRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final DrinkRepository drinkRepository;
    private final ArtistService artistService;

    public EventResponse getEvent(Integer id) {
        Event event = eventRepository
                .findById(id)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Cant find event by id"));

        return ConvertUtils.mapEventToResponse(event);
    }

    public EventResponse saveEvent(PostEventRequest request) {
        // Validate and get drinks
        List<Drink> drinks = drinkRepository.findAllById(request.getDrinks());
        if (drinks.size() < request.getDrinks().size()) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Can't find all drinks by id");
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
                .foodItems(request.getFood() != null ? request.getFood() : new ArrayList<>())
                .build();

        event = eventRepository.save(event);
        return ConvertUtils.mapEventToResponse(event);
    }
} 