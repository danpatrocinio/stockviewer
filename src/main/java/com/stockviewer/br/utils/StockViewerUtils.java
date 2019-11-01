package com.stockviewer.br.utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StockViewerUtils {

    private static SimpleDateFormat SDF_HM = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    private static SimpleDateFormat SDF_DT = new SimpleDateFormat("dd/MM/yyyy");
    private static int MES_APORTE_INICIAL = 4;
    private static int ANO_APORTE_INICIAL = 2018;

    public static BigDecimal getBigDecimal(Object v) {
        if (v == null) return null;
        String value = v.toString().toUpperCase().replace("R$","").replace(",", ".").trim();
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

    public static Date getDataHora(Object v) throws ParseException {
        if (v == null) return null;
        return SDF_HM.parse(v.toString());
    }

    public static Date getData(Object v) throws ParseException {
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

    public static String getMes(int mes) {
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

    public static Map<String, BigDecimal> getInitialMap() {
        Map<String, BigDecimal> map = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        final int MES_ATUAL = cal.get(Calendar.MONTH);
        final int ANO_ATUAL = cal.get(Calendar.YEAR);
        int mesAporte = MES_APORTE_INICIAL;
        int anoAporte = ANO_APORTE_INICIAL;
        while (!(mesAporte == MES_ATUAL && anoAporte == ANO_ATUAL)) {
            if (mesAporte == 12) {
                mesAporte = 0;
                anoAporte++;
            }
            map.put(getMes(mesAporte) + "/" + anoAporte, BigDecimal.ZERO);
            mesAporte++;
        }

//        for (Map.Entry<String, BigDecimal> mesano : map.entrySet()) {
//            System.out.println(String.format("%s |%s |%s ", "   ", mountStr(mesano.getKey(), 15), mountStr(mesano.getValue(), 11)));
//        }

        return map;
    }

}
