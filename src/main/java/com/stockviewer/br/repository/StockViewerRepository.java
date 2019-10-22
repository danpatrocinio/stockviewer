package com.stockviewer.br.repository;

import com.stockviewer.br.model.Operacao;
import com.stockviewer.br.utils.LeitorGoogleSheets;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

public class StockViewerRepository {

    @Autowired
    private OperacaoRepository operacaoRepository;

    @Autowired
    private AtivoRepository ativoRepository;

    public void populateStockviewerData() {
        try {

            for (Operacao operacao : LeitorGoogleSheets.getLinhas()) {
                ativoRepository.save(operacao.getAtivo());
                operacaoRepository.save(operacao);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
