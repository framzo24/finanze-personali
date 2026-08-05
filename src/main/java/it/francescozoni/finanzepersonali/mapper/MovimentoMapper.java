package it.francescozoni.finanzepersonali.mapper;

import it.francescozoni.finanzepersonali.dto.MovimentoResponse;
import it.francescozoni.finanzepersonali.model.Movimento;
import org.springframework.stereotype.Component;

@Component
public class MovimentoMapper {

    public MovimentoResponse toResponse(
            Movimento movimento
    ) {
        return new MovimentoResponse(
                movimento.getId(),
                movimento.getDescrizione(),
                movimento.getImporto(),
                movimento.getTipo(),
                movimento.getCategoria().getNome(),
                movimento.getDataMovimento()
        );
    }

    public MovimentoResponse toEntity() {
        return null;
    }

}
