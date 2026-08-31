package dev.aditya.orderservice.Security;

import dev.aditya.orderservice.Exceptions.CustomAuthorizationException;
import dev.aditya.orderservice.Model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class AuthFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("LoadBalancedRestTemplate")
    RestTemplate restTemplate;

    @Autowired
    CustomAuthEntryPoint customAuthEntryPoint;

    private final String verificationURL = "http://User-Auth-Service/user/validate";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);// This needs to be collected and then passed forward otherwise it dies here.

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity<Void> requestHeaderEntity = new HttpEntity<>(headers);

        ResponseEntity<String> authenticatedResponse = restTemplate.postForEntity(verificationURL, requestHeaderEntity, String.class);

        if(authenticatedResponse.getStatusCode().is2xxSuccessful()) {

            //This here tells spring that the request is authenticated, So every filter needs this to pass the request which need to be authenticated.
            UsernamePasswordAuthenticationToken authenticationToken =
                    UsernamePasswordAuthenticationToken.authenticated(createUser(authenticatedResponse.getHeaders()),
                            null,
                            AuthorityUtils.createAuthorityList(authenticatedResponse.getHeaders().getFirst("X-USER-ROLES")));

            SecurityContext newContext = SecurityContextHolder.createEmptyContext();
            newContext.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(newContext);

            //Only go ahead if authorized otherwise it should hit entry point and stop execution
            filterChain.doFilter(request, response);
        } else if (request.getServletPath().equals("/staus")) {
            //Allow the request to pass through in case of payment details update.
            filterChain.doFilter(request, response);
        } else {
            customAuthEntryPoint.commence(request, response, new CustomAuthorizationException("Authentication failed!! Possible Theft!"));
        }
    }


    // Helper Method
    private User createUser(HttpHeaders headers){
        User newUser = new User();
        newUser.setUserId(Long.valueOf(headers.getFirst("X-USER-ID")));
        newUser.setUserName(headers.getFirst("X-USER-NAME"));
        newUser.setEmail(headers.getFirst("X-USER-EMAIL"));
        newUser.setPhoneNumber(headers.getFirst("X-USER-PHONE"));
        return newUser;
    }
}
