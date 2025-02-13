package com.example.partystarter.service;

import com.example.partystarter.exception.ResourceException;
import com.example.partystarter.model.*;
import com.example.partystarter.model.request.PostPartyRequest;
import com.example.partystarter.model.response.PartyResponse;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.PartyRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class PartyService {
    private final PartyRepository partyRepository;
    private final DrinkRepository drinkRepository;
    private final ArtistService artistService;

    public PartyResponse getParty(Integer id) {
        Party party = partyRepository
                .findById(id)
                .orElseThrow(() -> new ResourceException(HttpStatus.NOT_FOUND, "Cant find party by id"));

        return ConvertUtils.mapPartyToResponse(party);
    }

    public PartyResponse saveParty(PostPartyRequest request) {
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

        // Build and save party
        Party party = Party.builder()
                .name(request.getName() == null ? "New party" : request.getName())
                .date(request.getDate())
                .time(request.getTime())
                .location(location)
                .artists(artists)
                .drinks(drinks)
                .foodItems(request.getFood() != null ? request.getFood() : new ArrayList<>())
                .build();

        party = partyRepository.save(party);
        return ConvertUtils.mapPartyToResponse(party);
    }

}
