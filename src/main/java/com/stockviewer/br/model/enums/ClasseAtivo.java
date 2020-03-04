package com.stockviewer.br.model.enums;

public enum ClasseAtivo {

    FII("F", "Fundos de Investimentos Imobiliários"),
    LCI("L", "Letra de Crédito Imobiliário"),
    ACOES("A", "Ações"),
    SELIC("S", "Tesouro Selic"),
    ETF_IVVB11("E", "Indice SP500"),
    BTC("B", "BitCoin")
    ;

    private String tipo;
    private String descricao;

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

    public String getDescricao() {
        return descricao;
    }

    public String getTipo() {
        return tipo;
    }

}
