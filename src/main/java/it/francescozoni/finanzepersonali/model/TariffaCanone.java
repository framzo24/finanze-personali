// TariffaCanone.java
package it.francescozoni.finanzepersonali.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tariffe_canone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffaCanone extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canone_id", nullable = false)
    private Canone canone;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importo;

    @Column(nullable = false)
    private LocalDate validaDal;
}