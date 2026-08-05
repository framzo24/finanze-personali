package it.francescozoni.finanzepersonali.configuration;

import it.francescozoni.finanzepersonali.model.Categoria;
import it.francescozoni.finanzepersonali.repository.CategoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseInitializer {

    private final CategoriaRepository repository;

    public DatabaseInitializer(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Bean
    public CommandLineRunner initDatabase() {

        return args -> {
            if (repository.count() == 0) {
                creaCategoria("A", "", "");
                creaCategoria("B", "", "");
            }
        };
    }

    private Categoria creaCategoria(String nome, String icona, String colore) {
        return repository.save(
                Categoria.builder()
                        .nome(nome)
                        .icona(icona)
                        .colore(colore)
                        .build()
        );
    }
}
