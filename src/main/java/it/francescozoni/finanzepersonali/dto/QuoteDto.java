package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class QuoteDto {

    private QuoteDto() {
    }

    public enum StatoQuota { DA_INVIARE, PARZIALE, INVIATA, DICHIARATA, A_POSTO }

    /** coinquilinoId null = tu. mancante è negativo se è stato inviato più della quota. */
    public record QuotaPersona(Long coinquilinoId, String nome, boolean tu, BigDecimal quota,
                               BigDecimal inviato, BigDecimal mancante, StatoQuota stato) {
    }

    public record VersamentoResponse(Long id, Long bollettaId, Long coinquilinoId, String personaNome, boolean tu,
                                     BigDecimal importo, LocalDate data, String nota, boolean dichiarato,
                                     Long movimentoId) {
    }

    public record BollettaQuote(Long bollettaId, TipoBolletta tipo, String fornitore, LocalDate periodoDal,
                                LocalDate periodoAl, LocalDate dataScadenza, BigDecimal importo, StatoBolletta stato,
                                boolean scaduta, boolean storica, List<QuotaPersona> persone,
                                List<VersamentoResponse> versamenti) {
    }

    public record PersonaProspetto(Long coinquilinoId, String nome, boolean tu) {
    }

    public record CellaProspetto(BigDecimal quota, BigDecimal inviato, BigDecimal mancante) {
    }

    /** tipo null = riga della somma. Le celle seguono l'ordine delle persone. */
    public record RigaProspetto(TipoBolletta tipo, int bollette, BigDecimal totale, List<CellaProspetto> celle) {
    }

    public record ProspettoResponse(boolean configurato, LocalDate contaDal, String proprietario,
                                    List<PersonaProspetto> persone, List<RigaProspetto> utenze, RigaProspetto somma) {
    }

    public record VoceVersamento(
            @NotNull(message = "La bolletta è obbligatoria") Long bollettaId,
            @NotNull(message = "L'importo è obbligatorio")
            @DecimalMin(value = "0.01", message = "L'importo deve essere maggiore di zero") BigDecimal importo) {
    }

    /** coinquilinoId null = invio tuo. */
    public record VersamentiRequest(
            Long coinquilinoId,
            @NotNull(message = "La data è obbligatoria") LocalDate data,
            @Size(max = 500, message = "La nota può avere al massimo 500 caratteri") String nota,
            Boolean registraMovimento,
            @NotEmpty(message = "Scegli almeno una bolletta") @Size(max = 50) List<@Valid VoceVersamento> voci) {
    }
}