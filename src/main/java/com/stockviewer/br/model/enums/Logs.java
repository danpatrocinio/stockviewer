package com.stockviewer.br.model.enums;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public enum Logs {

    CARTEIRA("C", "Carteira consolidada"),
    CLASSE("S", "Classes de ativos"),
    APORTE("A", "Aportes mensais"),
    MOEDA("M", "Moedas expostas"),
    TUDO("T", "Tudo");

    private String tipo;
    private String descricao;

    Logs(String tipo, String descricao) {
        this.tipo = tipo;
        this.descricao = descricao;
    }

    public static Logs getLogByTipo(String tipo){
        for (Logs tipoLog : Logs.values()) {
            if (tipoLog.getTipo().equals(tipo)) return tipoLog;
        }
        return null;
    }

    public static Map<Logs, Boolean> getLogs (String[] args) {
        Map<Logs, Boolean> logs = new LinkedHashMap<>();
        if (args != null && args.length > 0)
            Arrays.asList(args).forEach(s -> logs.put(getLogByTipo(s), getLogByTipo(s) != null));
        else logs.put(TUDO, true);
        return logs;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getTipo() {
        return tipo;
    }
}