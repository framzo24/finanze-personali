package it.francescozoni.finanzepersonali.security;

import it.francescozoni.finanzepersonali.exception.CredenzialiNonValideException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class UtenteCorrente {

    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token
                && token.getToken().getClaims().get("uid") instanceof Number uid) {
            return uid.longValue();
        }
        throw new CredenzialiNonValideException();
    }
}