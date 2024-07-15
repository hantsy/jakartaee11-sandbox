package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.logging.Level;
import java.util.logging.Logger;

import static jakarta.faces.application.FacesMessage.SEVERITY_ERROR;
import static jakarta.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static jakarta.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import static jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;

@Named
@RequestScoped
public class LoginBean {

    @Inject
    private SecurityContext securityContext;

    @NotNull
    @Size(min = 3, max = 15, message = "Username must be between 3 and 15 characters")
    private String username;

    @NotNull
    @Size(min = 5, max = 50, message = "Password must be between 5 and 50 characters")
    private String password;

    @Inject
    Logger LOG;

    @Inject
    FacesContext facesContext;

    @Inject
    ExternalContext externalContext;

    public void login() {
        Credential credential = new UsernamePasswordCredential(username, new Password(password));

        AuthenticationStatus status = securityContext.authenticate(
                (HttpServletRequest) externalContext.getRequest(),
                (HttpServletResponse) externalContext.getResponse(),
                withParams().credential(credential)
        );

        LOG.log(Level.INFO, "authentication result:{0}", status);
        if (status.equals(SEND_CONTINUE)) {
            // Authentication mechanism has send a redirect, should not
            // send anything to response from JSF now.
            facesContext.responseComplete();
        } else if (status.equals(SEND_FAILURE)) {
            addError(facesContext, "Authentication failed");
        }

    }

    private static void addError(FacesContext context, String message) {
        context.addMessage(
                null,
                new FacesMessage(SEVERITY_ERROR, message, null)
        );
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
