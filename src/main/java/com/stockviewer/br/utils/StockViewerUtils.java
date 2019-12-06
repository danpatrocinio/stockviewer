package com.stockviewer.br.utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

public class StockViewerUtils {

    private static SimpleDateFormat SDF_HM = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    private static SimpleDateFormat SDF_DT = new SimpleDateFormat("dd/MM/yyyy");

    public static BigDecimal getBigDecimal(Object v) {
        if (v == null) return null;
        String value = v.toString().toUpperCase().replace("R$","").replace(",", ".").trim();
        if (value.length() > 2 && ".".equals(value.substring(value.length()-2, value.length()-1))) {
            value = value + "0";
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

    public static String mountStr(Object v, int tam) {
        String espacos = "                              "; // length = 30
        if (v == null) return espacos.substring(0, tam);

        StringBuilder sb = new StringBuilder();
        while (sb.length() < tam - v.toString().length()) {
            sb.append(' ');
        }
        sb.append(v.toString());
        return sb.toString();
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
            mesAporte++;
        }
        map.put(getMes(MES_ATUAL) + "/" + ANO_ATUAL, BigDecimal.ZERO);
        return map;
    }

    public static String relevancia(BigDecimal valor) {
        if (valor == null || BigDecimal.ZERO.compareTo(valor) >= 0) return "";
        StringBuilder barras = new StringBuilder();
        BigDecimal cem = new BigDecimal(100);
        while (valor.compareTo(cem) >= 0) {
            barras.append("|");
            valor = valor.subtract(cem);
        }
        return barras.toString();
    }

}
