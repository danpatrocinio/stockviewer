package com.stockviewer.br;

import com.stockviewer.br.model.Ativo;
import com.stockviewer.br.model.Operacao;
import com.stockviewer.br.repository.AtivoRepository;
import com.stockviewer.br.repository.OperacaoRepository;
import com.stockviewer.br.utils.LeitorGoogleSheets;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

@SpringBootApplication
public class StockViewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockViewerApplication.class, args);
    }

    @Bean
    public CommandLineRunner populate(OperacaoRepository operacaoRepository, AtivoRepository ativoRepository) throws ParseException, GeneralSecurityException, IOException {
        return (args) -> {
            Ativo ativoSaved;
            for (Operacao operacao: new LeitorGoogleSheets().getLinhas()) {
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
        };
    }

//    private void populateH2() throws IOException, GeneralSecurityException, ParseException {
//        int i = 0;
//        for (Operacao operacao: new LeitorGoogleSheets().getLinhas()) {
//
//            ativoRepository.save(operacao.getAtivo());
//
//            ++i;
//            System.out.println(i + ") " + operacao.getTipo() + ": "
//                    + operacao.getQuantidade() + " "
//                    + operacao.getAtivo().getTicker() + " - " + operacao.getAtivo().getNome()
//                    + " a " + operacao.getValorUnitario()
//                    + " total " + (operacao.getValorUnitario().multiply(new BigDecimal(operacao.getQuantidade()))));
//        }
//    }

}
