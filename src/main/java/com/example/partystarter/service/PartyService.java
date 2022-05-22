package com.example.partystarter.service;

import com.example.partystarter.model.Party;
import com.example.partystarter.model.request.PostPartyRequest;
import com.example.partystarter.model.response.PartyResponse;
import com.example.partystarter.repo.PartyRepository;
import com.example.partystarter.utils.ConvertUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@AllArgsConstructor
public class PartyService {
    private final PartyRepository partyRepository;

    public PartyResponse getParty(Integer id) {
        Party party = partyRepository
                .findById(id)
                .orElseThrow();

        return ConvertUtils.mapPartyToResponse(party);
    }

    public PartyResponse saveParty(PostPartyRequest request) {
        Party party = Party.builder()
                .drinks(new HashSet<>(request.getDrinks()))
                .name(request.getName() == null ? "New party" : request.getName())
                .build();

        party = partyRepository.saveAndFlush(party);
        party = partyRepository.findById(party.getId()).orElseThrow();

        return ConvertUtils.mapPartyToResponse(party);
    }

}
