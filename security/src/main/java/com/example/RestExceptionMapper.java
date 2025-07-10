package com.example;

import jakarta.security.enterprise.AuthenticationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RestExceptionMapper implements ExceptionMapper<AuthenticationException> {

    @Override
    public Response toResponse(AuthenticationException exception) {
        if (exception instanceof UnauthorizedException ue) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ue.getMessage()).build();
        }
        if (exception instanceof ForbiddenException fe) {
            return Response.status(Response.Status.FORBIDDEN).entity(fe.getMessage()).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).entity(exception.getMessage()).build();
    }
}
