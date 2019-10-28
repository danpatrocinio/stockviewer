package com.stockviewer.br.model.enums;

public enum TipoOperacao {

    COMPRA("C", "Compra"),
    VENDA("V", "Venda"),
    AGRUPAMENTO("A", "Agrupamento"),
    DESDOBRAMENTO("D", "Desdobramento");

    private String tipo;
    private String descricao;

    TipoOperacao(String tipo, String descricao) {
        this.tipo = tipo;
        this.descricao = descricao;
    }

    public static TipoOperacao getTipoByValue(String descricao){
        for (TipoOperacao tipoOp : TipoOperacao.values()) {
            if (tipoOp.getDescricao().equals(descricao)) return tipoOp;
        }
        throw new IllegalArgumentException("Tipo de operação " + descricao + " não encontrado no enum");
    }

    public String getDescricao() {
        return descricao;
    }

    public String getTipo() {
        return tipo;
    }
}
