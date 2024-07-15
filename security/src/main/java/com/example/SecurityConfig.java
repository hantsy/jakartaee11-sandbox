package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.InMemoryIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.InMemoryIdentityStoreDefinition.Credentials;

@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login.faces",
                errorPage = "/login.faces?error"
        )
)
@InMemoryIdentityStoreDefinition(
        value = {
                @Credentials(callerName = "peter", password = "secret1", groups = {"foo", "bar"}),
                @Credentials(callerName = "john", password = "secret2", groups = {"foo", "kaz"}),
                @Credentials(callerName = "carla", password = "secret3", groups = {"foo"})
        }
)
@ApplicationScoped
public class SecurityConfig {
}
