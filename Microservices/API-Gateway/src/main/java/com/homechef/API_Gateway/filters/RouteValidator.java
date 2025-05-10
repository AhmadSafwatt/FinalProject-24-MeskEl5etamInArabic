package com.homechef.API_Gateway.filters;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;


// this class is used to create a filter for the API Gateway
@Component
public class RouteValidator {

    // this is the list of endpoints that we don't want the gateway
    // to authenticate the user for
    // because asking the user to authenticate while authenticating/registering
    // makes no sense
    public static final List<String> apiEndpoints = List.of(
            "/auth/register",
            "/auth/token",
            "/auth/validate-token",
            "/auth/verify-email",
            "/auth/reset-password",
            "/auth/update-password",
            "/kubernetes"
    );

    public Predicate<ServerHttpRequest> isSecured = request -> {
        // check if the request is for a secured endpoint
        return apiEndpoints.stream().anyMatch(uri -> request.getURI().getPath().contains(uri));
    };


}
