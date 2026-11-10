// config/BolletteInizializzatore.java
package it.francescozoni.finanzepersonali.config;

import it.francescozoni.finanzepersonali.model.Canone;
import it.francescozoni.finanzepersonali.model.Coinquilino;
import it.francescozoni.finanzepersonali.model.TariffaCanone;
import it.francescozoni.finanzepersonali.model.Utente;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import it.francescozoni.finanzepersonali.repository.CanoneRepository;
import it.francescozoni.finanzepersonali.repository.CoinquilinoRepository;
import it.francescozoni.finanzepersonali.repository.TariffaCanoneRepository;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import it.francescozoni.finanzepersonali.service.AuthService;
import it.francescozoni.finanzepersonali.service.CanoneService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@Order(2)
public class BolletteInizializzatore implements ApplicationRunner {

    private final UtenteRepository utenti;
    private final CoinquilinoRepository coinquilini;
    private final CanoneRepository canoni;
    private final TariffaCanoneRepository tariffe;
    private final CanoneService canoneService;
    private final String wifiDal;

    public BolletteInizializzatore(UtenteRepository utenti,
                                   CoinquilinoRepository coinquilini,
                                   CanoneRepository canoni,
                                   TariffaCanoneRepository tariffe,
                                   CanoneService canoneService,
                                   @Value("${app.bollette.wifi-dal:2025-01-01}") String wifiDal) {
        this.utenti = utenti;
        this.coinquilini = coinquilini;
        this.canoni = canoni;
        this.tariffe = tariffe;
        this.canoneService = canoneService;
        this.wifiDal = wifiDal;
    }

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        Utente proprietario = utenti.findByUsername(AuthService.USERNAME_PROPRIETARIO).orElse(null);
        if (proprietario == null) {
            return;
        }
        Long utenteId = proprietario.getId();

        if (coinquilini.findAllByUtenteIdAndDeletedFalseOrderByIdAsc(utenteId).isEmpty()) {
            coinquilini.save(Coinquilino.builder().utente(proprietario).nome("Giovanni").build());
            coinquilini.save(Coinquilino.builder().utente(proprietario).nome("Lorenzo").build());
            log.info("Creati i coinquilini Giovanni e Lorenzo");
        }

        if (!canoni.existsByUtenteIdAndTipoAndDeletedFalse(utenteId, TipoBolletta.WIFI)) {
            Coinquilino giovanni = coinquilini.findAllByUtenteIdAndDeletedFalseOrderByIdAsc(utenteId).stream()
                    .filter(c -> c.getNome().equalsIgnoreCase("Giovanni"))
                    .findFirst()
                    .orElse(null);

            Canone wifi = canoni.save(Canone.builder()
                    .utente(proprietario)
                    .tipo(TipoBolletta.WIFI)
                    .fornitore("Fastweb")
                    .giornoScadenza(1)
                    .pagante(giovanni)
                    .attivo(true)
                    .build());

            tariffe.save(TariffaCanone.builder()
                    .canone(wifi)
                    .importo(new BigDecimal("30.90"))
                    .validaDal(LocalDate.parse(wifiDal))
                    .build());

            log.info("Creato il canone Wi-Fi Fastweb (30,90 euro dal {})", wifiDal);
        }

        canoneService.generaScadenze(utenteId);
    }
}