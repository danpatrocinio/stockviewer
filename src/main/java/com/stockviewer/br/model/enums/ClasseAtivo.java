package com.stockviewer.br.model.enums;

import java.util.Arrays;

public enum ClasseAtivo {

    FII("F", "Fundos de Investimentos Imobiliários"),
    LCI("L", "Letra de Crédito Imobiliário"),
    ACOES("A", "Ações"),
    ETF_IVVB11("E", "Indice SP500"),
    SELIC_CDI_DI("S", "Tesouro Selic, CDI ou Fundo de Renda Fixa DI"),
    BTC("B", "BitCoin"),
    DOLAR("D", "Dolar"),
    ;

    final private String tipo;
    final private String descricao;
    final private static String[] ativosDolarizados = new String[] {"IVV", "VOO", "VNQ", "VIOV", "EEMV", "SLYV", "IAU"};

    ClasseAtivo(String tipo, String descricao) {
        this.tipo = tipo;
        this.descricao = descricao;
    }

    public static ClasseAtivo getTipoByValue(String tipo) {
        if (tipo == null || tipo.isEmpty()) return null;
        for (ClasseAtivo classeAtivo : ClasseAtivo.values()) {
            if (classeAtivo.getTipo().equals(tipo)) return classeAtivo;
        }
        throw new IllegalArgumentException("Classe de ativo " + tipo + " não encontrado no enum");
    }

    public static boolean isAtivoDolarizado(String ticker) {
        return Arrays.stream(ativosDolarizados).anyMatch(s -> s.equals(ticker));
    }

    public String getDescricao() {
        return descricao;
    }

    public String getTipo() {
        return tipo;
    }

}
