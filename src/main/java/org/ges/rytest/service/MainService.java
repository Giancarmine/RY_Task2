package org.ges.rytest.service;

import org.ges.rytest.model.dto.RoutesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${resource.tasks}")
    private String resource;

    public List<RoutesDTO> findAllRoute() {
        return Arrays.stream(restTemplate.getForObject(resource, RoutesDTO[].class)).collect(Collectors.toList());
    }


}
