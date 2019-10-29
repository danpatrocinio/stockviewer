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
            consolidarCarteira(operacaoRepository, ativoRepository, carteiraRepository);
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

    private void consolidarCarteira(OperacaoRepository operacaoRepository, AtivoRepository ativoRepository, CarteiraConsolidadaRepository carteiraRepository) {
        log.info("\n\n\t\t\t\t\t\t [ Consolidando carteira ]\n");
        /*
        SELECT * FROM (
             select operacao.id_ativo, ticker,
             sum(case when (tipo = 0 or tipo = 3) then quantidade else (-1 * quantidade ) end) as quantidade,
             ROUND(case when (sum(case when (tipo = 0 or tipo = 3) then quantidade else (-1 * quantidade ) end) > 0) then
                    (sum(case when (tipo = 0 or tipo = 3) then (valor_unitario * quantidade) else (-1 * valor_unitario * quantidade ) end) / sum(case when (tipo = 0 or tipo = 3) then quantidade else (-1 * quantidade ) end)) end, 2) as preco_medio,
             sum(case when (tipo = 0 or tipo = 3) then (valor_unitario * quantidade) else (-1 * valor_unitario * quantidade ) end) as total_custo,
             sum(case when (tipo = 0 or tipo = 3) then (ativo.cotacao * quantidade) else (-1 * ativo.cotacao * quantidade ) end) as total_mercado
            from operacao
            join ativo on (ativo.id_ativo=operacao.id_ativo)
            group by operacao.id_ativo)
        WHERE total_mercado > 0;

         */

        CarteiraConsolidada carteira = new CarteiraConsolidada();
        carteira.setAtivos(new ArrayList<>());
        carteira.setValorCusto(BigDecimal.ZERO);
        carteira.setValorMercado(BigDecimal.ZERO);
        AtivoCarteira ativoCarteira;
        for (Object[] row : carteiraRepository.consolidaCarteira()) {
//            System.out.println(String.format("ativo: %d-%s, quantidade: %d, preco_medio: %.2f, total_custo: %.2f, total_mercado: %.2f ",
//                    getInteger(row[0]), getStr(row[1]), getInteger(row[2]), getBigDecimal(row[3]), getBigDecimal(row[4]), getBigDecimal(row[5])));
            ativoCarteira = new AtivoCarteira();
            ativoCarteira.setAtivo(ativoRepository.findByTicker(getStr(row[1])));
            ativoCarteira.setQuantidade(getInteger(row[2]));
            ativoCarteira.setPrecoMedio(getBigDecimal(row[3]));
            carteira.getAtivos().add(ativoCarteira);
            carteira.setValorCusto(carteira.getValorCusto().add(ativoCarteira.getPrecoMedio().multiply(getBigDecimal(row[2]))));
            carteira.setValorMercado(carteira.getValorMercado().add(ativoCarteira.getAtivo().getCotacao().multiply(getBigDecimal(row[2]))));
        }
        carteiraRepository.save(carteira);

        log.info("\n\n\t\t\t\t\t\t [ " + carteira.getAtivos().size() + " ativos em carteira ]\n");
    }

}
