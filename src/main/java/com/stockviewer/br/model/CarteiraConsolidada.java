package com.stockviewer.br.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "carteira")
public class CarteiraConsolidada {

    @Id
    @Column(name = "id_carteira")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="carteira_consolidada_seq")
    @SequenceGenerator(name="carteira_consolidada_seq", sequenceName="car_seq")
    private Long id;

    @JoinColumn(name = "id_carteira")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<AtivoCarteira> ativos;

    private BigDecimal valorCusto;

    private BigDecimal valorMercado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<AtivoCarteira> getAtivos() {
        return ativos;
    }

    public void setAtivos(List<AtivoCarteira> ativos) {
        this.ativos = ativos;
    }

    public BigDecimal getValorCusto() {
        return valorCusto;
    }

    public void setValorCusto(BigDecimal valorCusto) {
        this.valorCusto = valorCusto;
    }

    public BigDecimal getValorMercado() {
        return valorMercado;
    }

    public void setValorMercado(BigDecimal valorMercado) {
        this.valorMercado = valorMercado;
    }
}
