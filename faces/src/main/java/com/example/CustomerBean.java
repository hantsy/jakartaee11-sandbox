package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
@Named
public class CustomerBean {
    private final static Logger LOGGER= Logger.getLogger(CustomerBean.class.getName());

    @Inject
    FacesContext facesContext;

    private Customer customer;

    public Customer getCustomer() {
        return customer;
    }

    //@PostConstruct
    // use faces event to initialize it
    public void init(PreRenderViewEvent event) {
        if(facesContext.isPostback()){
            LOGGER.log(Level.INFO, "postback, skipping initialization");
            return;
        }

        LOGGER.log(Level.INFO, "initializing");
        customer = new Customer(
                "Foo",
                "Bar",
                Optional.ofNullable(null),
                new EmailAddress[]{
                        new EmailAddress("foo@example.com", true),
                        new EmailAddress("bar@example.com", false)
                },
                new Address("123 Main St", "Anytown", "CA", "12345")
        );
        LOGGER.log(Level.INFO, "initialized");
    }
}

