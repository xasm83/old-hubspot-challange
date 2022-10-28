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
public class Partner {
    String firstName;
    String lastName;
    String email;
    String country;
    List<Date> availableDates;
}
