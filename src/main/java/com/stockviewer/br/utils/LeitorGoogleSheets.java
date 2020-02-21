package com.stockviewer.br.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.stockviewer.br.model.Ativo;
import com.stockviewer.br.model.Operacao;
import com.stockviewer.br.model.enums.ClasseAtivo;
import com.stockviewer.br.model.enums.Corretora;
import com.stockviewer.br.model.enums.TipoOperacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.*;

import static com.stockviewer.br.utils.StockViewerUtils.*;

public class LeitorGoogleSheets {

    private static Sheets sheetsService;
    private static String APPLICATION_NAME = "Operacoes Bolsa";
    private static String SHEET_RANGE_NOTAS = "Notas!A:K"; // Colunas recuperadas
    private static String SHEET_RANGE_ATIVOS = "Ativos!A:D"; // Colunas recuperadas
    private static String SHEET_RANGE_BTC = "BTC!A:F"; // Colunas recuperadas
    private static String SHEET_RANGE_LCI = "LCI!A:F"; // Colunas recuperadas
    private static String SPREADSHEET_ID = "1FRxQbvAVoD_M5QeyQxjGK7P3GlgAUmAl3_hROYsXu7E";
    private static final Logger log = LoggerFactory.getLogger(LeitorGoogleSheets.class);

    private static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = LeitorGoogleSheets.class.getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));
        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    private static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), credential).setApplicationName(APPLICATION_NAME).build();
    }

    public static List<Ativo> getLinhasAtivos() throws IOException, GeneralSecurityException, ParseException {
        sheetsService = getSheetsService();
        List<List<Object>> values;
        int linha = 0;

        // Ativos;
        List<Ativo> ativos = new ArrayList<>();
        ValueRange response = sheetsService.spreadsheets().values().get(SPREADSHEET_ID, SHEET_RANGE_ATIVOS).execute();
        values = response.getValues();
        if (values != null && !values.isEmpty()) {
            Ativo ativo;
            for ( List row : values) {
                ++linha;
                if (linha == 1 || row.get(0).toString().startsWith("Ticker")) continue; // ignora quando for a header da planilha
                if (row.get(0) == null || row.get(0).toString().isEmpty()) break;
                ativo = new Ativo();
                ativo.setTicker(row.get(0).toString());
                ativo.setNome(row.get(1).toString());
                ativo.setCotacao(getBigDecimal(row.get(2)));
                ativo.setClasseAtivo(ClasseAtivo.getTipoByValue(row.get(3).toString()));
            }
        }

        return ativos;
    }

    public static List<Operacao> getLinhasOperacoes() throws IOException, GeneralSecurityException, ParseException {
        sheetsService = getSheetsService();
        List<List<Object>> values;
        int linha = 0;

        // Operações;
        List<Operacao> operacoes = new ArrayList<>();
        ValueRange response = sheetsService.spreadsheets().values().get(SPREADSHEET_ID, SHEET_RANGE_NOTAS).execute();
        values = response.getValues();
        if (values != null && !values.isEmpty()) {
            Ativo ativo;
            TipoOperacao tipo;
            ClasseAtivo classeAtivo;
            Corretora corretora;
            for (List row : values) {
                if (row.get(0).toString().startsWith("Carimbo")) continue; // ignora quando for a header da planilha
                if (row.get(0) == null || row.get(0).toString().isEmpty()) break;
                tipo = TipoOperacao.getTipoByValue(row.get(1).toString());
                classeAtivo = (row.size() >= 11 ? ClasseAtivo.getTipoByValue(row.get(10).toString()) : null);
                ativo = new Ativo();
                ativo.setTicker(row.get(3).toString());
                ativo.setNome(row.size() >= 9 ? row.get(8).toString() : null);
                ativo.setCotacao(row.size() >= 10 ? getBigDecimal(row.get(9)) : null);
                ativo.setClasseAtivo(classeAtivo);
                corretora = Corretora.getCorretoraByValue(row.get(6).toString());
                operacoes.add(new Operacao(getDataHora(row.get(0)), tipo, getData(row.get(2)), ativo,
                        getBigDecimal(row.get(4)), getBigDecimal(row.get(5)), corretora));
            }

            // BitCoins
            Ativo bitcoin = new Ativo();
            bitcoin.setTicker(ClasseAtivo.BTC.name());
            bitcoin.setNome(ClasseAtivo.BTC.getDescricao());
            bitcoin.setClasseAtivo(ClasseAtivo.BTC);
            linha = 0;
            response = sheetsService.spreadsheets().values().get(SPREADSHEET_ID, SHEET_RANGE_BTC).execute();
            values = response.getValues();
            if (values != null || !values.isEmpty()) {
                for (List row : values) {
                    ++linha;
                    if (linha == 1 || row.get(0).toString().startsWith("Operação")) continue; // ignora quando for a header da planilha
                    if (row.get(0) == null || row.get(0).toString().isEmpty()) break;
                    if (linha == 2 && bitcoin.getCotacao() == null) bitcoin.setCotacao(getBigDecimal(row.get(5)));
                    tipo = TipoOperacao.getTipoByValue(row.get(0).toString());
                    operacoes.add(new Operacao(getData(row.get(1)), tipo, getData(row.get(1)), bitcoin,  getBigDecimal(row.get(2)), getBigDecimal(row.get(3)), Corretora.XDEX));
                }
            }

            // LCI
            Ativo lci = new Ativo();
            lci.setTicker(ClasseAtivo.LCI.name());
            lci.setNome(ClasseAtivo.LCI.getDescricao());
            lci.setClasseAtivo(ClasseAtivo.LCI);
            linha = 0;
            BigDecimal vlTotalLci = BigDecimal.ZERO;
            response = sheetsService.spreadsheets().values().get(SPREADSHEET_ID, SHEET_RANGE_LCI).execute();
            values = response.getValues();
            if (values != null || !values.isEmpty()) {
                for (List row : values) {
                    ++linha;
                    if (linha == 1 || row.get(0).toString().startsWith("Operação")) continue; // ignora quando for a header da planilha
                    if (row.get(0) == null || row.get(0).toString().isEmpty()) break;
                    //if (linha == 2 && lci.getCotacao() == null) lci.setCotacao(getBigDecimal(row.get(6)));
                    vlTotalLci = vlTotalLci.add(getBigDecimal(row.get(5)));
                    tipo = TipoOperacao.getTipoByValue(row.get(0).toString());
                    operacoes.add(new Operacao(getData(row.get(1)), tipo, getData(row.get(1)), lci,  BigDecimal.ONE, getBigDecimal(row.get(4)), Corretora.INTER_DTVM));
                }
                lci.setCotacao(vlTotalLci);
                // public Operacao(Date carimboDataHora, TipoOperacao tipo, Date data, Ativo ativo, BigDecimal quantidade, BigDecimal valorUnitario, Corretora corretora) {
            }
        }

        return operacoes;
    }
}
