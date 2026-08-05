package it.francescozoni.finanzepersonali.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(length = 50)
    private String icona;

    @Column(length = 20)
    private String colore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_padre_id")
    private Categoria categoriaPadre;

    @OneToMany(mappedBy = "categoriaPadre")
    private List<Categoria> sottoCategorie = new ArrayList<>();
}