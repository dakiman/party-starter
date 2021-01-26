package com.example.partystarter.tasks;

import com.example.partystarter.service.DrinksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private final DrinksService drinksService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public ScheduledTasks(DrinksService drinksService) {
        this.drinksService = drinksService;
    }

    @Scheduled(fixedRate = 600000)
    public void retrieveIngredients() {
        log.info("Retrieving ingredients data from job");
        drinksService.retrieveAndSaveIngredients();
        log.info("Ingredients retrieved");
    }

}