package com.stockviewer.br.model;

import com.stockviewer.br.model.enums.Corretora;
import com.stockviewer.br.model.enums.TipoOperacao;

import java.math.BigDecimal;
import java.util.Date;

public class Operacao {

    private Date carimboDataHora;
    private TipoOperacao tipo;
    private Date data;
    private Ativo ativo;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private Corretora corretora;

    public Operacao(Date carimboDataHora, TipoOperacao tipo, Date data, Ativo ativo, Integer quantidade, BigDecimal valorUnitario, Corretora corretora) {
        this.carimboDataHora = carimboDataHora;
        this.tipo = tipo;
        this.data = data;
        this.ativo = ativo;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.corretora = corretora;
    }

    public Date getCarimboDataHora() {
        return carimboDataHora;
    }

    public void setCarimboDataHora(Date carimboDataHora) {
        this.carimboDataHora = carimboDataHora;
    }

    public TipoOperacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoOperacao tipo) {
        this.tipo = tipo;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Ativo getAtivo() {
        return ativo;
    }

    public void setAtivo(Ativo ativo) {
        this.ativo = ativo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Corretora getCorretora() {
        return corretora;
    }

    public void setCorretora(Corretora corretora) {
        this.corretora = corretora;
    }
}
