package com.hubspot;

import com.hubspot.helper.HttpClient;
import com.hubspot.model.Countries;
import com.hubspot.model.Country;
import com.hubspot.model.Partner;
import com.hubspot.model.Partners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Map.entry;


@Component
public class AvailabilitiesService {

    private static final Logger LOG = LoggerFactory.getLogger(AvailabilitiesService.class);

    @Autowired
    private HttpClient httpClient;

    // sorting by date and getting the date with max partners
    private Optional<Date> getCommonStartDate(Map<Date, Set<Partner>> partnersByDate) {
        AtomicInteger maxCount = new AtomicInteger(0);
        AtomicReference<Optional<Date>> earliestDate = new AtomicReference<>(Optional.empty());

        partnersByDate.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(dateAndPartnersEntry -> {
                    int countOfPartners = dateAndPartnersEntry.getValue().size();
                    Date date = dateAndPartnersEntry.getKey();

                    if (countOfPartners > maxCount.get()) {
                        earliestDate.set(Optional.of(date));
                        maxCount.set(countOfPartners);
                    }
                });

        return earliestDate.get();
    }

    //  Gets all the valid start dates for a partner
    private List<Map.Entry<Date, Partner>> getPartnerStartDates(Partner partner) {
        Date[] dates = partner.getAvailableDates().toArray(new Date[]{});
        Arrays.sort(dates);
        Set<Date> startDates = new HashSet<>();

        for (int i = 0; i < dates.length - 1; i++) {
            // we don't care about zone id as two dates are in the same zone
            LocalDate dateOne = LocalDate.ofInstant(dates[i].toInstant(), ZoneId.systemDefault());
            LocalDate dateTwo = LocalDate.ofInstant(dates[i + 1].toInstant(), ZoneId.systemDefault());
            Period period = Period.between(dateOne, dateTwo);
            if (period.getDays() == 1) startDates.add(dates[i]);
        }

        return startDates
                .stream()
                .map(date -> entry(date, partner))
                .toList();
    }

    private Map<Date, Set<Partner>> calculatePartnersByDate(Map.Entry<String, List<Partner>> countryAndPartners) {
        // Calculates start dates for each partner and aggregates them into a map of partners per date
        return countryAndPartners.getValue()
                // streaming List<Partner>
                .stream()
                .map(this::getPartnerStartDates)
                // extracting entries from List<Map.Entry<Date, Partner>>
                .flatMap(Collection::stream)
                //collecting stream of Map.Entry<Date, Partner> with further mapping to Map<Date, Set<Partner>>
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));
    }

    private Country calculateAvailabilitiesByCountry(Map.Entry<String, List<Partner>> countryAndPartners) {
        Map<Date, Set<Partner>> partnersByDate = calculatePartnersByDate(countryAndPartners);
        Optional<Date> startDate = getCommonStartDate(partnersByDate);
        String countryName = countryAndPartners.getKey();

        Country country;
        if (startDate.isEmpty()) {
            country = new Country(0, Collections.emptyList(), countryName, null);
        } else {
            Date date = startDate.get();
            List<String> attendees = partnersByDate.get(date)
                    .stream()
                    .map(Partner::getEmail)
                    .toList();
            country = new Country(attendees.size(), attendees, countryName, date);
        }

        return country;
    }

    //   Group partners by country and calculate avails per country
    Countries calculateAvailabilities(Partners partners) {
        List<Country> countriesList = partners.getPartners()
                .stream()
                .collect(Collectors.groupingBy(Partner::getCountry))
                .entrySet()
                .stream()
                .map(this::calculateAvailabilitiesByCountry)
                // sorting by country to get predictable result
                .sorted(Comparator.comparing(Country::getName))
                .toList();
        LOG.info(countriesList.toString());
        return new Countries(countriesList);
    }

    public void sendAvailabilities() {
        try {
            Partners partners = httpClient.getPartners();
            Countries countries = calculateAvailabilities(partners);
            httpClient.postAvailabilities(countries);
        } catch (Exception exception) {
            LOG.error("App execution failed:\n", exception);
            throw exception;
        }
    }
}
