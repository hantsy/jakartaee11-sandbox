package com.example.el;

import jakarta.el.ELContext;
import jakarta.el.ELManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.el.ELAwareBeanManager;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ElCaller {
    private final static Logger LOGGER = Logger.getLogger(ElCaller.class.getName());

    @Inject
    ELAwareBeanManager beanManager;

    @Inject
    ElGreeter elGreeter;

    public String sayHello() {
        ELManager elManager = new ELManager();
        var resolvedResult = beanManager.getELResolver().invoke(elManager.getELContext(), elGreeter, "hello", new Class[]{String.class}, new String[]{"ELAwareBeanManager"});
        LOGGER.log(Level.FINEST, "resolved result: {0}", new Object[]{resolvedResult});
        return (String) resolvedResult;
    }

}
