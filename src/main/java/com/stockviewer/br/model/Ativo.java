package com.stockviewer.br.model;

import com.stockviewer.br.model.enums.ClasseAtivo;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ativo")
public class Ativo {

    @Id
    @Column(name = "id_ativo")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ativo_seq")
    @SequenceGenerator(name="ativo_seq", sequenceName="ati_seq")
    private Long id;
    @Column(length = 7, unique = true, nullable = false)
    private String ticker;
    private String nome;
    private BigDecimal cotacao;
    private ClasseAtivo classeAtivo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ClasseAtivo getClasseAtivo() {
        return classeAtivo;
    }

    public void setClasseAtivo(ClasseAtivo classeAtivo) {
        this.classeAtivo = classeAtivo;
    }

}
