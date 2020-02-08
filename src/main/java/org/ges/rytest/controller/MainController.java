package org.ges.rytest.controller;

import com.google.gson.Gson;
import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.ges.rytest.model.dao.Routes;
import org.ges.rytest.service.MainService;
import org.ges.rytest.client.RoutesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value="RY - Test2 API")
@RestController
public class MainController {

    @Autowired
    private MainService service;
    @Autowired
    private Gson gson;


    @ApiOperation(value = "Check the App Status", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity sayHello() {
        return new ResponseEntity<>(gson.toJson("Hello, I'm Alive!"), HttpStatus.OK);
    }

    // @GetMapping("/timtbl/3/schedules/{departure}/{arrival}/years/{year}/months/{month}")
    // public ResponseEntity routes(@PathVariable String departure,
    //                             @PathVariable String arrival,
    //                             @PathVariable String year,
    //                             @PathVariable String month) {

    @ApiOperation(value = "Check the IATA Service Status", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAll() {
        return new ResponseEntity<>(gson.toJson(service.findAllRoutesIATA()), HttpStatus.OK);
    }
}
