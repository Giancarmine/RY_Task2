package org.ges.rytest.model.dao;

import lombok.Data;

import java.util.List;

@Data
public class Day {
    private int day;
    private List<Flight> flights;
}
