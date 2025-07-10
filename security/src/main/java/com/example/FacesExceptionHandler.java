package com.example;

import jakarta.faces.FacesException;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.security.enterprise.AuthenticationException;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FacesExceptionHandler extends ExceptionHandlerWrapper {

    //@Inject
    private final static Logger LOG = Logger.getLogger(FacesExceptionHandler.class.getName());

    public FacesExceptionHandler(ExceptionHandler wrapped) {
        super(wrapped);
    }

    @Override
    public void handle() throws FacesException {
        LOG.log(Level.INFO, "invoking custom ExceptionHandlder...");
        Iterator<ExceptionQueuedEvent> events = getUnhandledExceptionQueuedEvents().iterator();

        while (events.hasNext()) {
            ExceptionQueuedEvent event = events.next();
            ExceptionQueuedEventContext context = event.getContext();
            Throwable t = context.getException();
            LOG.log(Level.INFO, "Exception@" + t.getClass().getName());
            if (t instanceof ViewExpiredException vee) {
                try {
                    handleViewExpiredException(vee);
                } finally {
                    events.remove();
                }
            } else if (t instanceof AuthenticationException ae) {
                try {
                    handleAuthenticationException(ae);
                } finally {
                    events.remove();
                }
            } else {
                getWrapped().handle();
            }
        }

    }

    private void handleViewExpiredException(ViewExpiredException vee) {
        LOG.log(Level.INFO, "handling exception: {0}", vee.getClass().getName());
        FacesContext context = FacesContext.getCurrentInstance();
        String viewId = vee.getViewId();
        LOG.log(Level.INFO, "view id @" + viewId);
        NavigationHandler nav = context.getApplication().getNavigationHandler();
        nav.handleNavigation(context, null, viewId);
        context.renderResponse();
    }

    private void handleAuthenticationException(Exception e) {
        LOG.log(Level.INFO, "handling exception: {0}", e.getClass().getName());
        FacesContext context = FacesContext.getCurrentInstance();
        String loginViewId = "/login.xhtml";
        LOG.log(Level.INFO, "view id @" + loginViewId);
        NavigationHandler nav = context.getApplication().getNavigationHandler();
        nav.handleNavigation(context, null, loginViewId);
        context.getViewRoot().getViewMap(true).put("errors", e);
        context.renderResponse();
    }
}