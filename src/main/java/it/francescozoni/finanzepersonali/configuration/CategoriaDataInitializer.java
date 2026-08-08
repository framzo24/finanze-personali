package it.francescozoni.finanzepersonali.configuration;

import it.francescozoni.finanzepersonali.model.Categoria;
import it.francescozoni.finanzepersonali.repository.CategoriaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class CategoriaDataInitializer implements ApplicationRunner {


    private final CategoriaRepository repository;


    public CategoriaDataInitializer(
            CategoriaRepository repository
    ) {
        this.repository = repository;
    }


    @Override
    public void run(ApplicationArguments args) {


        if (repository.count() > 0) {

            log.debug(
                    "Categorie già presenti. Nessuna inizializzazione necessaria."
            );

            return;

        }


        log.info(
                "Creazione categorie iniziali"
        );


        Categoria entrate =
                Categoria.builder()
                        .nome("Entrate")
                        .icona("💰")
                        .colore("#4CAF50")
                        .build();


        Categoria uscite =
                Categoria.builder()
                        .nome("Uscite")
                        .icona("💸")
                        .colore("#F44336")
                        .build();



        Categoria casa =
                Categoria.builder()
                        .nome("Casa")
                        .icona("🏠")
                        .colore("#2196F3")
                        .categoriaPadre(uscite)
                        .build();



        Categoria alimentari =
                Categoria.builder()
                        .nome("Alimentari")
                        .icona("🍔")
                        .colore("#FF9800")
                        .categoriaPadre(uscite)
                        .build();



        Categoria stipendio =
                Categoria.builder()
                        .nome("Stipendio")
                        .icona("💼")
                        .colore("#8BC34A")
                        .categoriaPadre(entrate)
                        .build();



        Categoria bonus =
                Categoria.builder()
                        .nome("Bonus")
                        .icona("🎁")
                        .colore("#9C27B0")
                        .categoriaPadre(entrate)
                        .build();



        repository.saveAll(
                List.of(
                        entrate,
                        uscite,
                        casa,
                        alimentari,
                        stipendio,
                        bonus
                )
        );


        log.info(
                "Categorie iniziali create correttamente"
        );

    }

}