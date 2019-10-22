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
import com.stockviewer.br.model.enums.Corretora;
import com.stockviewer.br.model.enums.TipoOperacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LeitorGoogleSheets {

    private static Sheets sheetsService;
    private static String APPLICATION_NAME = "Operacoes Bolsa";
    private static String SHEET_RANGE = "Notas!A:J"; // Colunas recuperadas
    private static SimpleDateFormat SDF_HM = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    private static SimpleDateFormat SDF_DT = new SimpleDateFormat("dd/MM/yyyy");
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

    private static BigDecimal getBigDecimal(Object v) {
        if (v == null) return null;
        String value = v.toString().toUpperCase().replace("R$","").replace(",", ".").trim();
        return new BigDecimal(value);
    }
    private static Integer getInteger(Object v) {
        if (v == null) return null;
        String value = v.toString().toUpperCase().replace("R$","").replace(",", ".").trim();
        return Integer.parseInt(value);
    }

    private static Date getDataHora(Object v) throws ParseException {
        if (v == null) return null;
        return SDF_HM.parse(v.toString());
    }

    private static Date getData(Object v) throws ParseException {
        if (v == null) return null;
        return SDF_DT.parse(v.toString());
    }

    public static List<Operacao> getLinhas() throws IOException, GeneralSecurityException, ParseException {
        sheetsService = getSheetsService();
        ValueRange response = sheetsService.spreadsheets().values().get(SPREADSHEET_ID, SHEET_RANGE).execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            log.info("\n\n\t\t\t\t\t\t [ Sem dados para ler ]\n");
            return Collections.emptyList();
        } else {
            List<Operacao> operacoes = new ArrayList<>();
            Ativo ativo;
            TipoOperacao tipo;
            Corretora corretora;
            for (List row : values) {
                if (row.get(0).toString().startsWith("Carimbo")) continue; // ignora quando for a header da planilha
                tipo = TipoOperacao.getTipoByValue(row.get(1).toString());
                ativo = new Ativo();
                ativo.setTicker(row.get(3).toString());
                ativo.setNome(row.size() >= 9 ? row.get(8).toString() : null);
                ativo.setCotacao(row.size() >= 10 ? getBigDecimal(row.get(9)) : null);
                corretora = Corretora.getCorretoraByValue(row.get(6).toString());
                operacoes.add(new Operacao(getDataHora(row.get(0)), tipo, getData(row.get(2)), ativo,
                        getInteger(row.get(4)), getBigDecimal(row.get(5)), corretora));
            }
            return operacoes;
        }
    }
}
