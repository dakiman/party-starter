package com.example.partystarter.api;

import com.example.partystarter.model.request.PostPartyRequest;
import com.example.partystarter.model.response.PartyResponse;
import com.example.partystarter.service.PartyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@AllArgsConstructor
@RestController
@RequestMapping(path = "/parties")
public class PartyController {

    private final PartyService partyService;

    @PostMapping(path = "")
    public ResponseEntity<PartyResponse> createNewParty(@Valid @RequestBody
                                                            PostPartyRequest request) {
        return ResponseEntity.ok(partyService.saveParty(request));
    }
    
    @GetMapping(path = "/{id}")
    public ResponseEntity<PartyResponse> getParty(@PathVariable(value = "id")
                                                      Integer id) {
        return ResponseEntity.ok(partyService.getParty(id));
    }

}
