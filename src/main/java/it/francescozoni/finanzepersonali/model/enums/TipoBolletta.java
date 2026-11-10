package it.francescozoni.finanzepersonali.model.enums;

public enum TipoBolletta {
    LUCE("kWh"), GAS("Smc"), ACQUA("m³"), WIFI(null);

    private final String unita;

    TipoBolletta(String unita) {
        this.unita = unita;
    }

    public String getUnita() {
        return unita;
    }
}