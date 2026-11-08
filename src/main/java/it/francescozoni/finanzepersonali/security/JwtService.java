package it.francescozoni.finanzepersonali.security;

import it.francescozoni.finanzepersonali.model.Utente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    public record TokenEmesso(String token, long scadenzaMs) {
    }

    private final JwtEncoder encoder;
    private final Duration durata;

    public JwtService(JwtEncoder encoder, @Value("${app.security.jwt-durata-giorni:7}") long giorni) {
        this.encoder = encoder;
        this.durata = Duration.ofDays(giorni);
    }

    public TokenEmesso emetti(Utente utente) {
        Instant adesso = Instant.now();
        Instant scadenza = adesso.plus(durata);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("finanze-personali")
                .subject(utente.getUsername())
                .issuedAt(adesso)
                .expiresAt(scadenza)
                .claim("uid", utente.getId())
                .build();

        String token = encoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        return new TokenEmesso(token, scadenza.toEpochMilli());
    }
}