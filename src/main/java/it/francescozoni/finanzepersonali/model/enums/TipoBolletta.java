package it.francescozoni.finanzepersonali.model.enums;

public enum TipoBolletta {
    LUCE("kWh", "Luce"), GAS("Smc", "Gas"), ACQUA("m³", "Acqua"), WIFI(null, "Wi-Fi");

    private final String unita;
    private final String etichetta;

    TipoBolletta(String unita, String etichetta) {
        this.unita = unita;
        this.etichetta = etichetta;
    }

    public String getUnita() {
        return unita;
    }

    public String getEtichetta() {
        return etichetta;
    }
}