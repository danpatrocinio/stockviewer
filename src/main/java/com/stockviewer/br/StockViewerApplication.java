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
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.stockviewer.br.utils.StockViewerUtils.*;

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
            consolidarCarteira(ativoRepository, carteiraRepository);
            consolidaAportes(operacaoRepository);
        };
    }

    private void buscarDados(OperacaoRepository operacaoRepository, AtivoRepository ativoRepository) throws IOException, GeneralSecurityException, ParseException {
        log("Carregando dados do Google Sheets");

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

        log(count + " operações carregadas");
    }

    private void consolidarCarteira(AtivoRepository ativoRepository, CarteiraConsolidadaRepository carteiraRepository) {
        log("Consolidando carteira");

        CarteiraConsolidada carteira = new CarteiraConsolidada();
        carteira.setAtivos(new ArrayList<>());
        carteira.setValorCusto(BigDecimal.ZERO);
        carteira.setValorMercado(BigDecimal.ZERO);
        AtivoCarteira ativoCarteira;
        Ativo ativo;
        int i = 0;
        System.out.println("  # | ATIVO  |   QTD | PREÇO MÉDIO |     COTACAO |       CUSTO | VALOR DE MERCADO | RETORNO");
        for (Object[] row : carteiraRepository.consolidaCarteira()) {
            ativoCarteira = new AtivoCarteira();
            ativo = ativoRepository.findByTicker(getStr(row[1]));
            ativoCarteira.setAtivo(ativo);
            ativoCarteira.setQuantidade(getInteger(row[2]));
            ativoCarteira.setCotacao(ativo.getCotacao());
            ativoCarteira.setPrecoMedio(getBigDecimal(row[3]));
            carteira.getAtivos().add(ativoCarteira);
            carteira.setValorCusto(carteira.getValorCusto().add(ativoCarteira.getPrecoMedio().multiply(getBigDecimal(row[2]))));
            carteira.setValorMercado(carteira.getValorMercado().add(ativoCarteira.getAtivo().getCotacao().multiply(getBigDecimal(row[2]))));
            showCarteira(++i, row, ativoCarteira);
        }

        carteiraRepository.save(carteira);

        BigDecimal retorno = carteira.getValorMercado().divide(carteira.getValorCusto(), RoundingMode.HALF_UP)
                .subtract(new BigDecimal(1)).multiply(new BigDecimal(100));
        System.out.println(String.format("%s |%s |%s | %s | %s | %s | %s | %s",
                mountStr(" ", 3),
                mountStr(" ", 7),
                mountStr(" ", 6),
                mountStr(" ", 11),
                mountStr(" ", 11),
                mountStr(carteira.getValorCusto(), 11),
                mountStr(carteira.getValorMercado(), 16),
                mountStr(retorno.toString().replace(".00", " %"), 6)));

        System.out.println();
        log( carteira.getAtivos().size() + " ativos em carteira");
    }

    private void consolidaAportes(OperacaoRepository operacaoRepository) {

        log("Aportes mensais");

        String mesAno;
        BigDecimal aporte;
        LinkedHashMap<String, BigDecimal> aporteMap = getInitialMap();
        System.out.println("  # |            MÊS |     APORTE");
        for (Operacao operacao : operacaoRepository.findByOrderByDataAsc()) {
            BigDecimal vlOperacao = operacao.getValorUnitario().multiply(new BigDecimal(operacao.getQuantidade()));
            mesAno = getMesAno(operacao.getData());
            aporte = aporteMap.get(mesAno);
            aporte = TipoOperacao.COMPRA.equals(operacao.getTipo()) ? aporte.add(vlOperacao) : aporte.subtract(vlOperacao);
            aporteMap.put(mesAno, aporte);
        }

        int i = 0;
        int naoOperados = 0;
        BigDecimal totalAportes = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> aporteMes :  aporteMap.entrySet()) {
            if (aporteMes.getValue().compareTo(BigDecimal.ZERO) > 0) {
                ++i;
            } else {
                ++naoOperados;
            }
            totalAportes = totalAportes.add(aporteMes.getValue());
            System.out.println(String.format("%s |%s |%s |%s ", "   ", mountStr(aporteMes.getKey(), 15), mountStr(aporteMes.getValue(), 11),
                    relevancia(getBigDecimal(aporteMes.getValue()))));
        }
        System.out.println();

        log("meses operados: " + i + " | não operados: " + naoOperados + " | valor total: " + totalAportes + " | média mensal: " + totalAportes.divide(new BigDecimal(aporteMap.size()), RoundingMode.HALF_EVEN));
    }

    private void showCarteira(int i, Object[] row, AtivoCarteira ativoCarteira) {

        BigDecimal retorno = ativoCarteira.getCotacao().divide(ativoCarteira.getPrecoMedio(), RoundingMode.HALF_UP)
                .subtract(new BigDecimal(1)).multiply(new BigDecimal(100));

        System.out.println(String.format("%s |%s |%s | %s | %s | %s | %s | %s | %s",
                mountStr(i, 3),
                mountStr(ativoCarteira.getAtivo().getTicker(), 7),
                mountStr(ativoCarteira.getQuantidade(), 6),
                mountStr(ativoCarteira.getPrecoMedio(), 11),
                mountStr(ativoCarteira.getCotacao(), 11),
                mountStr(getBigDecimal(row[4]), 11),
                mountStr(getBigDecimal(row[5]), 16),
                mountStr(retorno.toString().replace(".00", " %"), 6),
                relevancia(getBigDecimal(row[5]))));
    }

    private String relevancia(BigDecimal valor) {

        if (valor == null || BigDecimal.ZERO.compareTo(valor) >= 0) return "";
        StringBuilder barras = new StringBuilder("");
        BigDecimal cem = new BigDecimal(100);
        while (valor.compareTo(cem) >= 0) {
            barras.append("|");
            valor = valor.subtract(cem);
        }
        return barras.toString();
    }

    private void log(String msg) {
        log.info(String.format("\n\n\t\t\t [ %s ]\n", msg));
    }
}
