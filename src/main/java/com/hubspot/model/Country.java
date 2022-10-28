package com.hubspot.model;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Country {
    private int attendeeCount;
    private List<String> attendees;
    private String name;
    private Date startDate;
}
