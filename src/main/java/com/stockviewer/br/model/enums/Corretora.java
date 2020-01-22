package com.stockviewer.br.model.enums;

public enum Corretora {

    MODAL_MAIS("Modal Mais"), INTER_DTVM("Inter DTVM"), RICO("Rico"), XDEX("XDex");

    private String value;

    Corretora(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Corretora getCorretoraByValue(String value){
        for (Corretora corretora : Corretora.values()) {
            if (corretora.getValue().equals(value)) return corretora;
        }
        throw new IllegalArgumentException("Corretora " + value + " não encontrado no enum");
    }
}
