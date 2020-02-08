package org.ges.rytest.client;

import feign.RequestLine;
import org.ges.rytest.model.dao.Routes;

import java.util.List;

public interface RoutesClient {
    @RequestLine("GET")
    List<Routes> findAll();
}
