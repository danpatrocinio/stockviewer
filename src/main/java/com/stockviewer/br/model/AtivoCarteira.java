package com.stockviewer.br.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="ativo_carteira")
public class AtivoCarteira {

    @Id
    @Column(name="id_ativo_carteira")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer idAtivoCarteira;

    @ManyToOne
    @JoinColumn(name = "id_ativo")
    private Ativo ativo;

    private Integer quantidade;

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

    public BigDecimal getPrecoMedio() {
        return precoMedio;
    }

    public void setPrecoMedio(BigDecimal precoMedio) {
        this.precoMedio = precoMedio;
    }
}
