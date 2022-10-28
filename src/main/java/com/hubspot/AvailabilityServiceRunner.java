package com.hubspot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
class AvailabilityServiceRunner implements CommandLineRunner {

    @Autowired
    private AvailabilitiesService availabilitiesService;

    @Override
    public void run(String... args) {
        availabilitiesService.sendAvailabilities();
    }
}
