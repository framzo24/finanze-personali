// ImpostazioneResponse.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;

public record ImpostazioneResponse(TipoBolletta tipo, int periodicitaMesi, int preavvisoGiorni) {
}