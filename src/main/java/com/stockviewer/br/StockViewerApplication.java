package com.stockviewer.br;

import com.stockviewer.br.model.Ativo;
import com.stockviewer.br.model.AtivoCarteira;
import com.stockviewer.br.model.CarteiraConsolidada;
import com.stockviewer.br.model.Operacao;
import com.stockviewer.br.model.enums.TipoOperacao;
import com.stockviewer.br.repository.AtivoRepository;
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
    public CommandLineRunner populateApiData(OperacaoRepository operacaoRepository, AtivoRepository ativoRepository) {
        return (args) -> {
            buscarDados(operacaoRepository, ativoRepository);
            consolidarCarteira(operacaoRepository, ativoRepository);
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
            operacaoRepository.save(operacao);
        }

        log.info("\n\n\t\t\t\t\t\t [ " + count + " linhas de operações carregadas ]\n");
    }

    private void consolidarCarteira(OperacaoRepository operacaoRepository, AtivoRepository ativoRepository) {
        log.info("\n\n\t\t\t\t\t\t [ Consolidando carteira ]\n");

        Map<String, Operacao> consolidado = new HashMap<>();
        CarteiraConsolidada carteira = new CarteiraConsolidada();
        int count = 0;
        for (Operacao operacao : operacaoRepository.findAll()) {
            String ticker = operacao.getAtivo().getTicker();
            Integer quantidade = operacao.getQuantidade();
            BigDecimal valor = operacao.getValorUnitario().multiply(new BigDecimal(quantidade));
            if (operacao.getTipo().equals(TipoOperacao.VENDA)) {
                quantidade = quantidade * -1;
                valor = valor.multiply(new BigDecimal(-1));
            }
            if (consolidado.containsKey(ticker)) {

            } else {
                Operacao op = new Operacao();
                Ativo at = new Ativo();
                at.setTicker(ticker);
                at.setNome(operacao.getAtivo().getNome());
                op.setAtivo(at);
            }
        }
        carteira.setAtivos(new ArrayList<>());
        log.info("\n\n\t\t\t\t\t\t [ " + count + " ativos em carteira ]\n");
    }
}
