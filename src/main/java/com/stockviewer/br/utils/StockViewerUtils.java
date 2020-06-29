package com.stockviewer.br.utils;

import com.stockviewer.br.model.Ativo;
import com.stockviewer.br.model.enums.ClasseAtivo;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StockViewerUtils {

    final private static SimpleDateFormat SDF_HM = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    final private static SimpleDateFormat SDF_DT = new SimpleDateFormat("dd/MM/yyyy");

    public static BigDecimal getBigDecimal(Object v) {
        if (v == null) return null;
        if ("#N/A".equals(v.toString())) return BigDecimal.ZERO;
        String value = v.toString().toUpperCase().replace("R$","").replace(",", ".").trim();
        if (value.length() > 2 && ".".equals(value.substring(value.length()-2, value.length()-1))) {
            value = value + "0";
        }
        if (value.length() > 6 && ".".equals(value.substring(value.length()-7, value.length()-6))) {
            value = value.replaceFirst("[.]", "");
        }
        return new BigDecimal(value);
    }

    public static String getStr(Object v) {
        if (v == null) return null;
        return v.toString();
    }

    public static Integer getInteger(Object v) {
        if (v == null) return null;
        String value = v.toString().toUpperCase().replace("R$","").replace(",", ".").trim();
        return Integer.parseInt(value);
    }

    static Date getDataHora(Object v) throws ParseException {
        if (v == null) return null;
        return SDF_HM.parse(v.toString());
    }

    static Date getData(Object v) throws ParseException {
        if (v == null) return null;
        return SDF_DT.parse(v.toString());
    }

    public static String getMesAno(Date data) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(data);
        return getMes(cal.get(Calendar.MONTH)) + "/" + cal.get(Calendar.YEAR);
    }

    private static String getMes(int mes) {
        switch (mes) {
            case 0: return "Janeiro";
            case 1: return "Fevereiro";
            case 2: return "Março";
            case 3: return "Abril";
            case 4: return "Maio";
            case 5: return "Junho";
            case 6: return "Julho";
            case 7: return "Agosto";
            case 8: return "Setembro";
            case 9: return "Outubro";
            case 10: return "Novembro";
            case 11: return "Dezembro";
        }
        return "indefinido";
    }

    public static LinkedHashMap<String, BigDecimal> getInitialMap() {
        LinkedHashMap<String, BigDecimal> map = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        final int MES_ATUAL = cal.get(Calendar.MONTH);
        final int ANO_ATUAL = cal.get(Calendar.YEAR);
        int mesAporte = 4;
        int anoAporte = 2018;
        while (!(mesAporte == MES_ATUAL && anoAporte == ANO_ATUAL)) {
            if (mesAporte == 12) {
                mesAporte = 0;
                anoAporte++;
            }
            map.put(getMes(mesAporte) + "/" + anoAporte, BigDecimal.ZERO);
            if (!(mesAporte == MES_ATUAL && anoAporte == ANO_ATUAL)) {
                mesAporte++;
            }
        }
        map.put(getMes(MES_ATUAL) + "/" + ANO_ATUAL, BigDecimal.ZERO);
        return map;
    }

    public static String relevancia(BigDecimal valor) {
        if (valor == null || BigDecimal.ZERO.compareTo(valor) >= 0) return "";
        StringBuilder barras = new StringBuilder();
        final BigDecimal cem = new BigDecimal(100);
        final BigDecimal mil = new BigDecimal(1000);
        final BigDecimal cincoMil = new BigDecimal(5000);
//        while (valor.compareTo(cincoMil) >= 0) {
//            barras.append(String.format("%c", (char)0x2593));
//            valor = valor.subtract(cincoMil);
//        }
        while (valor.compareTo(mil) >= 0) {
            barras.append(String.format("%c", (char)0x2592));
            valor = valor.subtract(mil);
        }
        while (valor.compareTo(cem) >= 0) {
            barras.append(String.format("%c", (char)0x2591));
            valor = valor.subtract(cem);
        }
        return barras.toString();
    }

    public static BigDecimal aplicaPercentual(BigDecimal valor, BigDecimal total) {
        if (valor == null) return BigDecimal.ZERO;
        if (total == null) return new BigDecimal(100);
        return valor.multiply(new BigDecimal(100)).divide(total, 2, RoundingMode.HALF_EVEN);
    }

    public static Ativo getPrecoAtivoFromYahoo(Ativo ativo) throws IOException {
        Stock stocks = YahooFinance.get(ativo.getTicker().concat(".SA")); // single request
        ativo.setCotacao(stocks.getQuote(true).getPrice());
        return ativo;
    }

    public static void getPrecoAtivoFromYahoo(List<Ativo> ativos) throws IOException {

        if (ativos == null || ativos.isEmpty())
            return;

        String[] tickers = new String[] {
            ativos.stream()
                    .filter(a -> a.getClasseAtivo().equals(ClasseAtivo.ACOES) || a.getClasseAtivo().equals(ClasseAtivo.ETF_IVVB11) || a.getClasseAtivo().equals(ClasseAtivo.FII))
                    .map(Ativo::getTicker).collect(Collectors.joining(".SA,"))
        };

        tickers[0] = tickers[0].concat(".SA");

        Map<String, Stock> stocks = YahooFinance.get(tickers); // single request

        ativos.forEach(a -> {
            try {
                if (stocks.get(a.getTicker().concat(".SA")) != null)
                    a.setCotacao(stocks.get(a.getTicker().concat(".SA")).getQuote(false).getPrice());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
