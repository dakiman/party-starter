package com.example.partystarter.tasks;

import com.example.partystarter.service.DrinksService;
import com.example.partystarter.service.IngredientsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {

    private final DrinksService drinksService;
    private final IngredientsService ingredientService;

    @Value("${application.seeding.should-seed}")
    private boolean shouldSeed;

//    @Value("${application.seeding.interval}")
//    private static long seedInterval;

//    TODO How to place config value inside annotation
    @Scheduled(fixedRate = 60000)
    public void retrieveIngredients() {
        log.info("Seeding is {}", shouldSeed);

        if(shouldSeed) {
            log.info("Retrieving ingredients data from job");
            ingredientService.retrieveAndSaveIngredients();
            log.info("Ingredients retrieved");

            log.info("Retrieving Drinks data for all ingredients");
            drinksService.retrieveDrinksForAllIngredients();
            log.info("Drinks retrieved");
        }
    }

}