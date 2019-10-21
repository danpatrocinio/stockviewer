package com.stockviewer.br.model;

import java.math.BigDecimal;

public class Ativo {

    private String ticker;
    private String nome;
    private BigDecimal cotacao;

    public Ativo(String ticker, String nome, BigDecimal cotacao) {
        this.ticker = ticker;
        this.nome = nome;
        this.cotacao = cotacao;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getCotacao() {
        return cotacao;
    }

    public void setCotacao(BigDecimal cotacao) {
        this.cotacao = cotacao;
    }
}
