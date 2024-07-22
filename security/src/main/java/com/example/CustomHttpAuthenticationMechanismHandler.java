package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanismHandler;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ApplicationScoped
public class CustomHttpAuthenticationMechanismHandler implements HttpAuthenticationMechanismHandler {

    @Inject
    @RestAuthenticationQualifier
    private HttpAuthenticationMechanism restAuthenticationMechanism;

    @Inject
    @WebAuthenticationQualifier
    private HttpAuthenticationMechanism webAuthenticationMechanism;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        System.out.println("The request path: " + path);
        if (path.startsWith("/api")) {
            return restAuthenticationMechanism.validateRequest(request, response, httpMessageContext);
        }

        return webAuthenticationMechanism.validateRequest(request, response, httpMessageContext);
    }
}
