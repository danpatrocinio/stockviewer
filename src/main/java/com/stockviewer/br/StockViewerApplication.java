package com.stockviewer.br;

import com.stockviewer.br.model.Ativo;
import com.stockviewer.br.model.AtivoCarteira;
import com.stockviewer.br.model.CarteiraConsolidada;
import com.stockviewer.br.model.Operacao;
import com.stockviewer.br.model.enums.TipoOperacao;
import com.stockviewer.br.repository.AtivoRepository;
import com.stockviewer.br.repository.CarteiraConsolidadaRepository;
import com.stockviewer.br.repository.OperacaoRepository;
import com.stockviewer.br.utils.LeitorGoogleSheets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class StockViewerApplication {

    private static final Logger log = LoggerFactory.getLogger(StockViewerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(StockViewerApplication.class, args);
    }

    @Bean
    public CommandLineRunner populateApiData(OperacaoRepository operacaoRepository, AtivoRepository ativoRepository, CarteiraConsolidadaRepository carteiraRepository) {
        return (args) -> {
            buscarDados(operacaoRepository, ativoRepository);
            consolidarCarteira(operacaoRepository, carteiraRepository);
        };
    }

    private void buscarDados(OperacaoRepository operacaoRepository, AtivoRepository ativoRepository) throws IOException, GeneralSecurityException, ParseException {
        log.info("\n\n\t\t\t\t\t\t [ Carregando dados do Google Sheets ]\n");

        Ativo ativoSaved;
        int count = 0;
        for (Operacao operacao: LeitorGoogleSheets.getLinhas()) {
            count++;
            ativoSaved = ativoRepository.findByTicker(operacao.getAtivo().getTicker());
            if (ativoSaved == null) {
                ativoSaved = new Ativo();
                ativoSaved.setTicker(operacao.getAtivo().getTicker());
                ativoSaved.setNome(operacao.getAtivo().getNome());
                ativoSaved.setCotacao(operacao.getAtivo().getCotacao());
                ativoRepository.save(ativoSaved);
                ativoSaved = ativoRepository.findByTicker(operacao.getAtivo().getTicker());
            }
            operacao.setAtivo(ativoSaved);
            operacao.setTipo(operacao.getTipo());
            operacaoRepository.save(operacao);
        }

        log.info("\n\n\t\t\t\t\t\t [ " + count + " linhas de operações carregadas ]\n");
    }

    private void consolidarCarteira(OperacaoRepository operacaoRepository, CarteiraConsolidadaRepository carteiraRepository) {
        log.info("\n\n\t\t\t\t\t\t [ Consolidando carteira ]\n");

        Ativo ativo;
        TipoOperacao tipoOperacao;
        Integer quantidade;
        BigDecimal valorOperacao;
        BigDecimal valorMercado;
        CarteiraConsolidada carteira = new CarteiraConsolidada();
        carteira.setAtivos(new ArrayList<>());
        carteira.setValorCusto(BigDecimal.ZERO);
        carteira.setValorMercado(BigDecimal.ZERO);
        for (Operacao operacao : operacaoRepository.findByOrderByDataAsc()) {
            ativo = operacao.getAtivo();
            tipoOperacao = operacao.getTipo();
            quantidade = operacao.getQuantidade();
            valorOperacao = operacao.getValorUnitario().multiply(new BigDecimal(operacao.getQuantidade()));
            valorMercado = operacao.getAtivo().getCotacao().multiply(new BigDecimal(quantidade));
            if (TipoOperacao.COMPRA.equals(tipoOperacao)) {
                addCompra(carteira, ativo, quantidade, valorOperacao);
            } else if (TipoOperacao.VENDA.equals(tipoOperacao)) {
                valorOperacao = valorOperacao.multiply(new BigDecimal(-1));
                valorMercado = valorMercado.multiply(new BigDecimal(-1));
                addVenda(carteira, ativo, quantidade, valorOperacao);
            } else if (TipoOperacao.AGRUPAMENTO.equals(tipoOperacao)) {
                addAgrupamento(carteira, ativo, quantidade);
            } else if (TipoOperacao.DESDOBRAMENTO.equals(tipoOperacao)) {
                addDesdobramento(carteira, ativo, quantidade);
            }
            carteira.setValorMercado(carteira.getValorMercado().add(valorMercado));
            carteira.setValorCusto(carteira.getValorCusto().add(valorOperacao));
        }

        carteiraRepository.save(carteira);

        log.info("\n\n\t\t\t\t\t\t [ " + carteira.getAtivos().size() + " ativos em carteira ]\n");
    }

    private void addCompra(CarteiraConsolidada carteira, Ativo ativo, Integer quantidade, BigDecimal valorOperacao) {
        for (AtivoCarteira emCarteira: carteira.getAtivos()) {
            if (emCarteira.getAtivo().getTicker().equals(ativo.getTicker())) {
                emCarteira.setQuantidade(emCarteira.getQuantidade() + quantidade);
                return;
            }
        }
        AtivoCarteira ativoCarteira = new AtivoCarteira();
        ativoCarteira.setAtivo(ativo);
        ativoCarteira.setPrecoMedio(valorOperacao);
        ativoCarteira.setQuantidade(quantidade);
        carteira.getAtivos().add(ativoCarteira);
    }

    private void addVenda(CarteiraConsolidada carteira, Ativo ativo, Integer quantidade, BigDecimal valorOperacao) {
        Integer quantidadeNova;

        for (AtivoCarteira emCarteira: carteira.getAtivos()) {
            if (emCarteira.getAtivo().getTicker().equals(ativo.getTicker())) {
                quantidadeNova = emCarteira.getQuantidade() - quantidade;
                if (quantidadeNova.compareTo(0) == 0) {
                    carteira.getAtivos().remove(emCarteira);
                    return;
                }
                emCarteira.setQuantidade(quantidadeNova);
                return;
            }
        }
    }

    private void addAgrupamento(CarteiraConsolidada carteira, Ativo ativo, Integer quantidade) {
        Integer quantidadeNova;

        for (AtivoCarteira emCarteira: carteira.getAtivos()) {
            if (emCarteira.getAtivo().getTicker().equals(ativo.getTicker())) {
                quantidadeNova = emCarteira.getQuantidade() - quantidade;
                if (quantidadeNova.compareTo(0) == 0) {
                    carteira.getAtivos().remove(emCarteira);
                    return;
                }
                emCarteira.setQuantidade(quantidadeNova);
                return;
            }
        }
    }

    private void addDesdobramento(CarteiraConsolidada carteira, Ativo ativo, Integer quantidade) {
        for (AtivoCarteira emCarteira: carteira.getAtivos()) {
            if (emCarteira.getAtivo().getTicker().equals(ativo.getTicker())) {
                emCarteira.setQuantidade(emCarteira.getQuantidade() + quantidade);
                return;
            }
        }
    }
}
