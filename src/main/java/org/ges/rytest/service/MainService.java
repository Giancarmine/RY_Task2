package org.ges.rytest.service;

import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import org.ges.rytest.client.RoutesClient;
import org.ges.rytest.model.dao.*;
import org.ges.rytest.model.dto.RoutesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MainService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${resource.tasks}")
    private String resource;

    // Use calendars instead of Date as some methods are deprecated
    private final Calendar departureDate = Calendar.getInstance();

    public List<RoutesDTO> findAllRoute() {
        return Arrays.stream(restTemplate.getForObject(resource, RoutesDTO[].class)).collect(Collectors.toList());
    }

    public List<Route> findAllRoutesIATA() {
        RoutesClient routesClient = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(RoutesClient.class))
                .logLevel(Logger.Level.FULL)
                .target(RoutesClient.class, "https://services-api.ryanair.com/locate/3/routes");

        return routesClient.findAllRoute();
    }

    public Schedule findAllSchedulesIATA(String departure, String arrival, Integer year, Integer month) {
        RoutesClient routesClient = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(RoutesClient.class))
                .logLevel(Logger.Level.FULL)
                .target(RoutesClient.class, "https://services-api.ryanair.com/timtbl/3/schedules/"
                                            + departure
                                            + "/" + arrival
                                            +"/years/" + year
                                            +"/months/" + month );

        log.info("URL: {}", "https://services-api.ryanair.com/timtbl/3/schedules/" + departure + "/" + arrival + "/years/" + year + "/months/" + month);

        try {
            return routesClient.findAllSchedule();
        } catch (FeignException e) {
            if (e.getMessage().contains("status 404")) {
                log.info("Status 404 - Interconnections not found");
                return null;
            }
            throw e;
        }
    }

    public ResponseEntity<PriorityQueue<Interconnection>> findInterconnections(String departure, String arrival, Date departureDateTime, Date arrivalDateTime) throws ParseException {

        // Prepare var
        var stopsSorter = Comparator.comparing(Interconnection::getStops);
        var availableConnections = new PriorityQueue<>(stopsSorter);

        // Get all routes
        List<Route> allRoutes = findAllRoutesIATA();
        // Filter Ryanair only
        allRoutes = allRoutes.stream()
                .filter(route -> "RYANAIR".equals(route.getOperator()))
                .collect(Collectors.toList());

        // Get all routes departing from departure
        List<Route> departingRoutes = allRoutes.stream()
                .filter(route -> departure.equals(route.getAirportFrom()))
                .collect(Collectors.toList());

        // Get all routes arriving to arrival
        List<Route> arrivingRoutes = allRoutes.stream()
                .filter(route -> arrival.equals(route.getAirportTo()))
                .collect(Collectors.toList());

        // For each departing route
        for (Route departingRoute: departingRoutes)
        {
            // If its destination is equal to arrival, possible direct connection
            if (arrival.equals(departingRoute.getAirportTo()))
            {
                setDirectFlights(availableConnections, departingRoute.getAirportFrom(), arrival, departureDateTime, arrivalDateTime, true);
            }

            // For each arriving route, if its departure is equal to departure's arrival, possible interconnection
            for (Route arrivingRoute: arrivingRoutes)
            {
                if (departingRoute.getAirportTo().equals(arrivingRoute.getAirportFrom()))
                {
                    // List of available flights for leg 1, we will have to iterate them
                    PriorityQueue <Interconnection> legsOneList = new PriorityQueue<>(stopsSorter);

                    // Flight 1
                    // Get schedules for flight1 (direct from airport 1 to airport 2) and save them into listLegOne
                    this.setDirectFlights(legsOneList, departingRoute.getAirportFrom(), departingRoute.getAirportTo(), departureDateTime, arrivalDateTime, false);

                    // Flight 2
                    // Get schedules for flight2 and check if the interconnection is possible with flight1. If so, save it into availableConnections
                    this.setInterconnectedFlights(availableConnections, legsOneList, arrivingRoute.getAirportFrom(), arrivingRoute.getAirportTo(), departureDateTime, arrivalDateTime);
                }
            }
        }

        if (availableConnections.isEmpty()) {
            log.info("Response: Status 404 - NO Interconnections Viable");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(availableConnections, HttpStatus.OK);
    }

    protected void setDirectFlights (final PriorityQueue<Interconnection> availableConnections, final String departure, final String arrival,
                                     final Date departureDateTime, final Date arrivalDateTime, final Boolean isOneLeg) throws ParseException {
        // Use calendars instead of Date as some methods are deprecated
        final Calendar departureDate = Calendar.getInstance();
        departureDate.setTime(departureDateTime);
        final Integer departingYear = departureDate.get(Calendar.YEAR);
        // Month is 0-index based
        final Integer departingMonth = departureDate.get(Calendar.MONTH)+1;

        final Calendar arrivalDate = Calendar.getInstance();
        arrivalDate.setTime(arrivalDateTime);

        // Get the schedule
        Schedule schedule = findAllSchedulesIATA(departure, arrival, departingYear, departingMonth);
        if (schedule != null) {
            for (Day day: schedule.getDays()) {
                for (Flight flight : day.getFlights()) {
                    // For each flight of each day, check if schedule is correct (true as is a direct flight and null first leg)
                    List<Calendar> departingArrivalFlightDates= new ArrayList<>();
                    if (isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate,
                            day, flight, departingArrivalFlightDates,
                            true, null)) {
                        // If so, set the leg, add it to legs, and store connection
                        final List<Leg> legs = new ArrayList<Leg>();
                        final Leg leg = new Leg();
                        leg.setDepartureAirport(departure);
                        leg.setArrivalAirport(arrival);
                        leg.setDepartureDateTime(getFormatedCalendar(departingArrivalFlightDates.get(0)));
                        leg.setArrivalDateTime(getFormatedCalendar(departingArrivalFlightDates.get(1)));
                        legs.add(leg);

                        final Interconnection interconnection = new Interconnection();
                        interconnection.setStops(0);
                        interconnection.setLegs(legs);

                        availableConnections.add(interconnection);
                    }
                }
            }
        }

    }

    protected void setInterconnectedFlights (final PriorityQueue<Interconnection> availableConnections, final PriorityQueue<Interconnection> legsOneConnections, final String departure, final String arrival,
                                             final Date departureDateTime, final Date arrivalDateTime) throws ParseException {

        departureDate.setTime(departureDateTime);
        final Integer departingYear = departureDate.get(Calendar.YEAR);
        // Month is 0-index based
        final Integer departingMonth = departureDate.get(Calendar.MONTH) + 1;

        final Calendar arrivalDate = Calendar.getInstance();
        arrivalDate.setTime(arrivalDateTime);

        // For each first leg available
        for (Interconnection connectionOne : legsOneConnections) {
            for (Leg legOne : connectionOne.getLegs()) {
                // Get schedules for flight 2 (airport 2 or leg 1 arrival to airport 3)
                Schedule scheduleTwo = findAllSchedulesIATA(departure, arrival, departingYear, departingMonth);
                if (scheduleTwo != null) {
                    for (Day day : scheduleTwo.getDays()) {
                        for (Flight flight : day.getFlights()) {
                            // For each flight of each day, check if schedule is correct (false as is a direct flight)
                            List<Calendar> departingArrivalFlightDates = new ArrayList<Calendar>();
                            if (isScheduleAvailable(departingYear, departingMonth, departureDate, arrivalDate,
                                    day, flight, departingArrivalFlightDates,
                                    false, legOne)) {
                                Leg legTwo = new Leg();
                                legTwo.setDepartureAirport(departure);
                                legTwo.setArrivalAirport(arrival);
                                legTwo.setDepartureDateTime(getFormatedCalendar(departingArrivalFlightDates.get(0)));
                                legTwo.setArrivalDateTime(getFormatedCalendar(departingArrivalFlightDates.get(1)));

                                // Add leg one and leg two as an interconnected flight
                                final List<Leg> legs = new ArrayList<Leg>();
                                legs.add(legOne);
                                legs.add(legTwo);

                                Interconnection interconnection = new Interconnection();
                                interconnection.setStops(1);
                                interconnection.setLegs(legs);

                                availableConnections.add(interconnection);

                            }
                        }
                    }
                }
            }
        }
    }

    public Boolean isScheduleAvailable (final Integer departingYear, final Integer departingMonth, Calendar departureDate, Calendar arrivalDate,
                                        final Day day, final Flight flight,	List<Calendar> departingArrivalFlightDates,
                                        final Boolean isDirect, final Leg legOne) throws ParseException {
        final Calendar departingDateFlight = getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getDepartureTime(), 0);

        final Calendar arrivingDateFlight = getCalendarFromFlightDate(departingYear, departingMonth, day.getDay(), flight.getArrivalTime(), 0);
        departingArrivalFlightDates.add(departingDateFlight);
        departingArrivalFlightDates.add(arrivingDateFlight);

        if (isDirect)
        {
            // Departing date must be after and arrival date must be before
            return departingDateFlight.after(departureDate) && arrivingDateFlight.before(arrivalDate);
        }
        else
        {
            // Warning we have to add two hours to leg one arrival time and ensure that is before departing time
            final String legOneArrivalWithInterconnectionString = legOne.getArrivalDateTime();
            Date legOneArrivalWithInterconnectionDate = new Date();


            legOneArrivalWithInterconnectionDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(legOneArrivalWithInterconnectionString);

            final Calendar legOneArrivalWithInterconnection = Calendar.getInstance();
            legOneArrivalWithInterconnection.setTime(legOneArrivalWithInterconnectionDate);
            // Increase two hours
            legOneArrivalWithInterconnection.add(Calendar.HOUR_OF_DAY, 2);

            // Departing date must be after and arrival date must be before
            // AND departing date with interconnection before that arrivalDateFlight
            return departingDateFlight.after(departureDate) && arrivingDateFlight.before(arrivalDate)
                    && legOneArrivalWithInterconnection.before(departingDateFlight);

        }
    }

    public Calendar getCalendarFromFlightDate(final Integer year, final Integer month, final Integer day, final String dateString, final Integer interconnectionHours)
    {
        final String[] time = dateString.split ( ":" );
        final int hour = Integer.parseInt ( time[0].trim() );
        final int minute = Integer.parseInt ( time[1].trim() );

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        // Zero index based
        calendar.set(Calendar.MONTH, month-1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour + interconnectionHours);
        calendar.set(Calendar.MINUTE, minute);
        return calendar;

    }

    public String getFormatedCalendar(final Calendar calendar)
    {
        final Date date = calendar.getTime();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        return dateFormat.format(date);
    }
}
