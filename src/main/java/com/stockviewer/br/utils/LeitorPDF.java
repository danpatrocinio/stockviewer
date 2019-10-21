package com.stockviewer.br.utils;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.IOException;

public class LeitorPDF {

    private static final String INICIO_LINHA = "R$";
    private static final String FIM_LINHA = "centavo)";

    public String ler(String filePath) {
        String conteudoPdf = getConteudo(filePath);
        int beginIndex = conteudoPdf.indexOf(INICIO_LINHA);
        int endIndex = conteudoPdf.indexOf(FIM_LINHA) + FIM_LINHA.length();
        return conteudoPdf.substring(beginIndex, endIndex);
    }

    private String getConteudo(String fileName) {
        PDDocument pdfDocument = null;
        try {
            PDFParser parser = new PDFParser(getClass().getResourceAsStream("/PDFs/" + fileName));
            parser.parse();
            pdfDocument = parser.getPDDocument();
            return new PDFTextStripper().getText(pdfDocument);
        } catch (Throwable e) {
            return "ERRO: Um erro ocorreu enquanto tentava obter o conteúdo do PDF: " + e.getMessage();
        } finally {
            if (pdfDocument != null) {
                try {
                    pdfDocument.close();
                } catch (IOException e) {
                    return "ERRO: Não foi possível fechar o PDF." + e.getMessage();
                }
            }
        }
    }
}
