package com.example.partystarter.tasks;

import com.example.partystarter.service.DrinksService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
@Slf4j
public class ScheduledTasks {

    private final DrinksService drinksService;

    public ScheduledTasks(DrinksService drinksService) {
        this.drinksService = drinksService;
    }

    @Scheduled(fixedRate = 6000000)
    public void retrieveIngredients() {
        log.info("Retrieving ingredients data from job");
        drinksService.retrieveAndSaveIngredients();
        log.info("Ingredients retrieved");

        log.info("Retrieving Drinks data for all ingredients");
        drinksService.retrieveDrinksForAllIngredients();
        log.info("Drinks retrieved");
    }

}