package com.hubspot;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.model.Countries;
import com.hubspot.model.Partner;
import com.hubspot.model.Partners;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class AvailabilitiesServiceTests {
    private static final Logger LOG = LoggerFactory.getLogger(AvailabilitiesServiceTests.class);
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Autowired
    AvailabilitiesService availabilitiesService;

//   covering only basic cases here due to time limitations

    @Test
    void testAvailSlotsCalculation() throws ParseException {

//  I could introduce Partners builder but omitting it for the sake of time limitations

//      country with 2 sets of common dates. one is 2 seq days and second is 3 seq days
        List<Date> dates1 = List.of(
                DATE_FORMAT.parse("2017-01-01"),
                DATE_FORMAT.parse("2017-01-02"),
                DATE_FORMAT.parse("2017-01-03"),
                DATE_FORMAT.parse("2017-03-01"),
                DATE_FORMAT.parse("2017-03-02"));
        Partner refPartner1 = new Partner(
                "firstName",
                "lastName",
                "test1@com.com",
                "2usa",
                dates1);

        List<Date> dates2 = List.of(
                DATE_FORMAT.parse("2017-01-01"),
                DATE_FORMAT.parse("2017-01-02"),
                DATE_FORMAT.parse("2017-04-01"),
                DATE_FORMAT.parse("2017-03-01"),
                DATE_FORMAT.parse("2017-03-02"));
        Partner refPartner2 = new Partner(
                "firstName",
                "lastName",
                "test2@com.com",
                "2usa",
                dates2);

//      country with no common dates
        List<Date> dates3 = List.of(
                DATE_FORMAT.parse("2017-01-01"),
                DATE_FORMAT.parse("2018-01-01"));
        Partner refPartner3 = new Partner(
                "firstName",
                "lastName",
                "test3@com.com",
                "1spain",
                dates3);

        List<Date> dates4 = List.of(
                DATE_FORMAT.parse("2019-01-01"),
                DATE_FORMAT.parse("2020-01-01"));
        Partner refPartner4 = new Partner(
                "firstName",
                "lastName",
                "test4@com.com",
                "1spain",
                dates4);


        // country with no dates at all
        List<Date> dates5 = List.of();
        Partner refPartner5 = new Partner(
                "firstName",
                "lastName",
                "test5@com.com",
                "3germany",
                dates3);


        Partners refPartners = new Partners(List.of(refPartner1, refPartner2, refPartner3, refPartner4, refPartner5));
        Countries countries = availabilitiesService.calculateAvailabilities(refPartners);

        // test start date is null when no common dates
        assertEquals(null, countries.getCountries().get(0).getStartDate());
        assertEquals(0, countries.getCountries().get(0).getAttendeeCount());
        assertEquals("1spain", countries.getCountries().get(0).getName());

        //test start date is the earliest and common when there avail slots
        assertEquals(2, countries.getCountries().get(1).getAttendeeCount());
        assertEquals(DATE_FORMAT.parse("2017-01-01"), countries.getCountries().get(1).getStartDate());
        assertEquals("2usa", countries.getCountries().get(1).getName());

        //test start date is the earliest and common when there avail slots
        assertEquals(0, countries.getCountries().get(2).getAttendeeCount());
        assertEquals(null, countries.getCountries().get(2).getStartDate());
        assertEquals("3germany", countries.getCountries().get(2).getName());
    }

    @Test
    void testPartnerParsing() throws JacksonException, ParseException {
        String rawJson = """
                	{"partners":
                		[{	
                			"firstName":"firstName",
                			"lastName":"lastName",  
                			"email":"test@com.com", 
                			"country":"usa",
                			"availableDates":["2017-05-03","2017-05-04"]
                		}]
                	}
                """;

        ObjectMapper mapper = new ObjectMapper();
        Partners partners = mapper.readValue(rawJson, Partners.class);

        List<Date> dates = List.of(
                DATE_FORMAT.parse("2017-05-03"),
                DATE_FORMAT.parse("2017-05-04"));
        Partner refPartner = new Partner(
                "firstName",
                "lastName",
                "test@com.com",
                "usa",
                dates);
        Partners refPartners = new Partners(List.of(refPartner));
        assertEquals(refPartners, partners);
    }
}
