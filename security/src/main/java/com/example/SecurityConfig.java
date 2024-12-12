package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.InMemoryIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.InMemoryIdentityStoreDefinition.Credentials;


@BasicAuthenticationMechanismDefinition(
        realmName = "BasicAuth",
        qualifiers = {RestAuthenticationQualifier.class}
)
@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login.faces",
                errorPage = "/login.faces?error"
        ),
        qualifiers = {WebAuthenticationQualifier.class}
)
@InMemoryIdentityStoreDefinition(
        value = {
                @Credentials(callerName = "admin", password = "password", groups = {"web", "rest"}),
                @Credentials(callerName = "webuser", password = "password", groups = {"web"}),
                @Credentials(callerName = "restuser", password = "password", groups = {"rest"})
        }
)
@ApplicationScoped
public class SecurityConfig {
}

