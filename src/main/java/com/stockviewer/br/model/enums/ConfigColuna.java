package com.stockviewer.br.model.enums;

public enum ConfigColuna {

    COLUNA_BASE3("coluna de base", 3),
    ATIVO("ativo", 7),
    QTD("quantidade", 10),
    PRECO_MEDIO("preco medio", 11),
    COTACAO("cotacao", 11),
    CUSTO("custo", 11),
    VL_MERCADO("valor mercado", 13),
    MES_APORTE("mes aporte", 15),
    VL_APORTE("valor aporte", 11),
    VL_TOTAL_CLASSE("valor classe", 10),
    PERCENTUAL_CLASSE("percentual classe", 6),
    RETORNO("retorno" , 8);

    private String descricao;
    private int tamanho;

    ConfigColuna(String descricao, int tamanho) {
        this.descricao = descricao;
        this.tamanho = tamanho;
    }

    public String mountColuna(Object v) {
        String espacos = "                              "; // length = 30
        if (v == null) return espacos.substring(0, this.tamanho);

        StringBuilder sb = new StringBuilder();
        while (sb.length() < this.tamanho - v.toString().length()) {
            sb.append(' ');
        }
        sb.append(v.toString());
        return sb.toString();
    }

}