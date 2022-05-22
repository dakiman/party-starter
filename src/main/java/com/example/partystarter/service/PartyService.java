package com.example.partystarter.service;

import com.example.partystarter.model.Drink;
import com.example.partystarter.model.Party;
import com.example.partystarter.model.request.PostPartyRequest;
import com.example.partystarter.model.response.PartyResponse;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.PartyRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class PartyService {
    private final PartyRepository partyRepository;
    private final DrinkRepository drinkRepository;

    public PartyResponse getParty(Integer id) {
        Party party = partyRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cant find party by id"));

        return ConvertUtils.mapPartyToResponse(party);
    }

    public PartyResponse saveParty(PostPartyRequest request) {
        List<Drink> drinks = drinkRepository.findAllById(request.getDrinks());

        if (drinks.size() < request.getDrinks().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cant find all drinks by id");
        }

        Party party = Party.builder()
                .drinks(drinks)
                .name(request.getName() == null ? "New party" : request.getName())
                .build();

        party = partyRepository.save(party);

        return ConvertUtils.mapPartyToResponse(party);
    }

}
