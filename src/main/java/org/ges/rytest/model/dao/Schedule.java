package org.ges.rytest.model.dao;

import lombok.Data;

import java.util.List;

@Data
public class Schedule {
    private int month;
    private List<Day> days;
}
