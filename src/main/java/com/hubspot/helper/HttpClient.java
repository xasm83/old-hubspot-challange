package com.hubspot.helper;

import com.hubspot.model.Countries;
import com.hubspot.model.Partners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/*
        For the same of simplicity I am not introducing interfaces or app based exceptions.
    RestClientException fully satisfies all the needs and there is no type specific
    exception handling.
        The same goes for HttpClient interface - there is no need to introduce it
    as we have only one class implementation and further function requirements are not known.
        Also it is always possible to change HttpClient from class to interface
    without adjusting the rest of the code once there is a need for multiple implementations.
 */

@Component
public class HttpClient {
    private static Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    @Value("${partners.url}")
    private String partnersUrl;

    @Value("${result.url}")
    private String resultUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Partners getPartners() {
        ResponseEntity<Partners> response;
        try {
            response = restTemplate.getForEntity(partnersUrl, Partners.class);
            LOG.info("Received the following response:\n {}", response.getBody());
        } catch (RestClientException exception) {
            LOG.error("Failed to get partners. ", exception);
            throw exception;
        }

        return response.getBody();
    }


    public void postAvailabilities(Countries countries) {
        try {
            ResponseEntity<Countries> response = restTemplate.postForEntity(resultUrl, countries, Countries.class);
            LOG.info("Received HTTP response code: {}" , response.getStatusCode());
        } catch (RestClientException exception) {
            LOG.error("Failed to post availabilities.", exception);
            throw exception;
        }
    }
}
