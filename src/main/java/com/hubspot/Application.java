package com.hubspot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    private static Logger LOG = LoggerFactory
            .getLogger(Application.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(Application.class, args);
        LOG.info("APPLICATION FINISHED");
    }
}
