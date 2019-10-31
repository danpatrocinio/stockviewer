package com.stockviewer.br.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="ativo_carteira")
public class AtivoCarteira {

    @Id
    @Column(name="id_ativo_carteira")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ativo_carteira_seq")
    @SequenceGenerator(name="ativo_carteira_seq", sequenceName="atc_seq")
    private Integer idAtivoCarteira;

    @ManyToOne
    @JoinColumn(name = "id_ativo")
    private Ativo ativo;

    private Integer quantidade;

    private BigDecimal cotacao;

    private BigDecimal precoMedio;

    public Integer getIdAtivoCarteira() {
        return idAtivoCarteira;
    }

    public void setIdAtivoCarteira(Integer idAtivoCarteira) {
        this.idAtivoCarteira = idAtivoCarteira;
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

    public BigDecimal getCotacao() {
        return cotacao;
    }

    public void setCotacao(BigDecimal cotacao) {
        this.cotacao = cotacao;
    }

    public BigDecimal getPrecoMedio() {
        return precoMedio;
    }

    public void setPrecoMedio(BigDecimal precoMedio) {
        this.precoMedio = precoMedio;
    }
}
