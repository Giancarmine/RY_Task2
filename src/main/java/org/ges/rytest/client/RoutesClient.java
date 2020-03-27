package org.ges.rytest.client;

import feign.RequestLine;
import org.ges.rytest.model.dao.Route;
import org.ges.rytest.model.dao.Schedule;

import java.util.List;

public interface RoutesClient {
    @RequestLine("GET")
    List<Route> findAllRoute();

    @RequestLine("GET")
    Schedule findAllSchedule();
}
