package org.ges.rytest.controller;

import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.ges.rytest.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    @ApiOperation(value = "Check the IATA Routes Service Status", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/getAllRoutes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAll() {
        return new ResponseEntity<>(gson.toJson(service.findAllRoutesIATA()), HttpStatus.OK);
    }

    @ApiOperation(value = "Check the IATA Schedules Service Status", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/getAllSchedules/{departure}/{arrival}/years/{year}/months/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAll(@PathVariable String departure,
                                 @PathVariable String arrival,
                                 @PathVariable String year,
                                 @PathVariable String month) {
        return new ResponseEntity<>(gson.toJson(service.findAllSchedulesIATA(departure,
                arrival,
                year,
                month)), HttpStatus.OK);
    }

    @ApiOperation(value = "Get Interconnections", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/interconnections", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAll(@RequestParam String departure,
                                 @RequestParam String arrival,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime  departureDateTime,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalDateTime) {
        return new ResponseEntity<>(gson.toJson(service.findInterconnections(departure,
                arrival,
                departureDateTime,
                arrivalDateTime)), HttpStatus.OK);
    }
}
