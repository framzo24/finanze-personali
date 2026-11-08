package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest richiesta) {
        return authService.login(richiesta);
    }

    @PostMapping("/registrazione")
    @ResponseStatus(HttpStatus.CREATED)
    public UtenteResponse registra(@Valid @RequestBody RegistrazioneRequest richiesta) {
        return authService.registra(richiesta);
    }

    @GetMapping("/disponibilita")
    public DisponibilitaResponse disponibilita(@RequestParam String nome, @RequestParam String cognome) {
        return authService.disponibilita(nome, cognome);
    }
}