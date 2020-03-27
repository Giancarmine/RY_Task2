package org.ges.rytest.model.dao;

import lombok.Data;

import java.util.List;

@Data
public class Interconnection {
    private int stops;
    private List<Leg> legs;
}
