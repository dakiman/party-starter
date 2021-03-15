package com.example.partystarter.api;

import com.example.partystarter.model.Drink;
import com.example.partystarter.model.Party;
import com.example.partystarter.repo.DrinkRepository;
import com.example.partystarter.repo.IngredientRepository;
import com.example.partystarter.repo.PartyRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/parties")
public class PartyController {

    private final PartyRepository partyRepository;
    private final IngredientRepository ingredientRepository;
    private final DrinkRepository drinkRepository;

    @PostMapping(path = "")
    ResponseEntity<Party> createNewParty(@RequestParam List<Drink> drinks) {
        Party party = new Party();
        party.setDrinks(new HashSet<>(drinks));
        party.setName("New party!");
        party = partyRepository.save(party);
//
//        return ResponseEntity.ok(party.getId().toString());
        return ResponseEntity.ok(party);
    }

}
