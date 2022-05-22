package com.example.partystarter.tasks;

import com.example.partystarter.service.CocktailDbSeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {

    private final CocktailDbSeedService cocktailDbSeedService;
    private final CacheManager cacheManager;

    @Value("${application.seeding.should-seed}")
    private boolean shouldSeed;

    @Scheduled(fixedRate = 6000000)
    public void retrieveDrinks() {
        log.info("Seeding is {}", shouldSeed);

        if(shouldSeed) {
            log.info("Retrieving ingredients data from job");
            cocktailDbSeedService.retrieveAndSaveIngredients();
            log.info("Ingredients retrieved");

            log.info("Retrieving Drinks data for all ingredients");
            cocktailDbSeedService.retrieveDrinksForAllIngredients();
            log.info("Drinks retrieved");
        }
    }

    @Scheduled(fixedRate = 3000000)
    public void evictAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

}