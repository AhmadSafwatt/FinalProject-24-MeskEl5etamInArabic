package com.homechef.API_Gateway.filters;


import com.homechef.API_Gateway.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.client.RestTemplate;


// this class is used to create a filter for the API Gateway
// it is used to authenticate the user before allowing access to the API Gateway
// and subsequently to the microservices our application offers
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator routeValidator;

//    method removed because it is less secure than using JwtUtil
//    @Autowired
//    private RestTemplate template;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    //the input is in the form of webflux server web exchange
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            //validate this exchange for all endpoints
            // except the ones we don't want to authenticate
            if (routeValidator.isSecured.test(exchange.getRequest())) {
                //check if the header contains the token
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    //if not, throw an exception
                    throw new RuntimeException("Missing Authorization Header");
                }

                //get the token from the header
                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

                //check if the token is valid
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    //if it is, we can proceed
                    //otherwise, throw an exception
                    String token = authHeader.substring(7);
                }
                else {
                    throw new RuntimeException("Invalid Authorization Header");
                }

                try {
                    // call the auth service to validate the token


//                  //TODO: modify according to actual AuthService routes
//                  template.getForObject("http://auth-service/auth/validate?token={token}", Boolean.class, authHeader);

                    //validate the token using the JwtUtil class
                    jwtUtil.validateToken(authHeader.substring(7));


                } catch (Exception e) {
                    //if the token is invalid, throw an exception
                    throw new RuntimeException("Invalid Token");
                }

            }
            return chain.filter(exchange);
        };
    }

    //we don't have any special config params
    // so we can just use an empty class
    public static class Config {
    }



}
