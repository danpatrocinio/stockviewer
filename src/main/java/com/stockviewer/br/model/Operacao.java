package com.stockviewer.br.model;

import com.stockviewer.br.model.enums.Corretora;
import com.stockviewer.br.model.enums.TipoOperacao;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "operacao")
public class Operacao {

    @Id
    @Column(name = "id_operacao")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    @Column(name = "carimbo_data_hora")
    private Date carimboDataHora;
    private TipoOperacao tipo;
    private Date data;
    @ManyToOne
    @JoinColumn(name = "id_ativo", nullable = false)
    private Ativo ativo;
    private Integer quantidade;
    @Column(name = "valor_unitario")
    private BigDecimal valorUnitario;
    private Corretora corretora;

    public Operacao(){}

    public Operacao(Date carimboDataHora, TipoOperacao tipo, Date data, Ativo ativo, Integer quantidade, BigDecimal valorUnitario, Corretora corretora) {
        this.carimboDataHora = carimboDataHora;
        this.tipo = tipo;
        this.data = data;
        this.ativo = ativo;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.corretora = corretora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
