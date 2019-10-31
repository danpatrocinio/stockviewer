package com.stockviewer.br.utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StockViewerUtils {

    private static SimpleDateFormat SDF_HM = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    private static SimpleDateFormat SDF_DT = new SimpleDateFormat("dd/MM/yyyy");

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

}
