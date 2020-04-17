package org.ges.rytest.model.dao;

import lombok.Data;

@Data
public class Route {
    private String airportFrom;
    private String airportTo;
    private String connectingAirport;
    private String newRoute;
    private String seasonalRoute;
    private String operator;
    private String group;
    private String carrierCode;
}
