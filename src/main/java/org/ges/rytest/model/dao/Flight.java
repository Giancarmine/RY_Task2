package org.ges.rytest.model.dao;

import lombok.Data;

@Data
public class Flight {
    private String carrierCode;
    private int number;
    private String departureTime;
    private String arrivalTime;
}
