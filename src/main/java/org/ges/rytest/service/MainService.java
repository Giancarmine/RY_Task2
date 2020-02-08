package org.ges.rytest.service;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.ges.rytest.client.RoutesClient;
import org.ges.rytest.model.dao.Routes;
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

    public List<Routes> findAllRoutesIATA() {
        RoutesClient bookClient = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(RoutesClient.class))
                .logLevel(Logger.Level.FULL)
                .target(RoutesClient.class, "https://services-api.ryanair.com/locate/3/routes");

        return bookClient.findAll();
    }


}
