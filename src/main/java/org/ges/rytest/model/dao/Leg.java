package org.ges.rytest.model.dao;


import lombok.Data;

@Data
public class Leg {
    private String departureAirport;
    private String arrivalAirport;
    private String departureDateTime;
    private String arrivalDateTime;
}
