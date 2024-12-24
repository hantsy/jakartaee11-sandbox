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

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class MultipleHttpAuthenticationMechanismHandler implements HttpAuthenticationMechanismHandler {
    private final static Logger LOGGER = Logger.getLogger(MultipleHttpAuthenticationMechanismHandler.class.getName());

    @Inject
    @RestAuthenticationQualifier
    private HttpAuthenticationMechanism restAuthenticationMechanism;

    @Inject
    @WebAuthenticationQualifier
    private HttpAuthenticationMechanism webAuthenticationMechanism;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        LOGGER.log(Level.INFO, "The request path(without context path): {0}", path);
        if (path.startsWith("/api")) {
            return restAuthenticationMechanism.validateRequest(request, response, httpMessageContext);
        }

        return webAuthenticationMechanism.validateRequest(request, response, httpMessageContext);
    }
}
