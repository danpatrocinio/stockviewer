package com.stockviewer.br;

import com.stockviewer.br.model.Operacao;
import com.stockviewer.br.utils.LeitorGoogleSheets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.ParseException;

@SpringBootApplication
public class StockViewerApplication {

    public static void main(String[] args) throws ParseException, GeneralSecurityException, IOException {
        SpringApplication.run(StockViewerApplication.class, args);
        int i = 0;
        for (Operacao operacao: new LeitorGoogleSheets().getLinhas()) {
            ++i;
            System.out.println(i + ") " + operacao.getTipo() + ": "
                    + operacao.getQuantidade() + " "
                    + operacao.getAtivo().getTicker() + " - " + operacao.getAtivo().getNome()
                    + " a " + operacao.getValorUnitario()
                    + " total " + (operacao.getValorUnitario().multiply(new BigDecimal(operacao.getQuantidade()))));
        }
    }

}
