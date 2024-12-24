package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;

import java.util.Set;

@RequestScoped
@Named("profileBean")
@Authenticated
public class ProfileBean {

    @Inject
    SecurityContext securityContext;

    private String name;
    private Set<String> roles;

    public void init() {
        this.name = securityContext.getCallerPrincipal().getName();
        this.roles = securityContext.getAllDeclaredCallerRoles();
    }

    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
