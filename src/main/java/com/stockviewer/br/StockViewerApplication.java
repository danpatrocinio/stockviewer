package com.stockviewer.br;

import com.stockviewer.br.model.Ativo;
import com.stockviewer.br.model.AtivoCarteira;
import com.stockviewer.br.model.CarteiraConsolidada;
import com.stockviewer.br.model.Operacao;
import com.stockviewer.br.model.enums.ClasseAtivo;
import com.stockviewer.br.model.enums.Logs;
import com.stockviewer.br.model.enums.TipoOperacao;
import com.stockviewer.br.repository.AtivoRepository;
import com.stockviewer.br.repository.CarteiraConsolidadaRepository;
import com.stockviewer.br.repository.OperacaoRepository;
import com.stockviewer.br.utils.LeitorGoogleSheets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.stockviewer.br.utils.StockViewerUtils.*;

@SpringBootApplication
public class StockViewerApplication {

    private static final Logger log = LoggerFactory.getLogger(StockViewerApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(StockViewerApplication.class, args);
    }

    private Map<Logs, Boolean> loggind;

    @Bean
    public CommandLineRunner populateApiData(OperacaoRepository operacaoRepository, AtivoRepository ativoRepository, CarteiraConsolidadaRepository carteiraRepository) {
        return (args) -> {
            loggind = Logs.getLogs(args);
            log(String.format("Exibir carteira: %b | Exibir classes: %b | Exibir aportes: %b", this.isLogCarteira(), this.isLogClasses(), this.isLogAportes()));
            buscarAtivos(ativoRepository);
            buscarOperacoes(operacaoRepository, ativoRepository);
            consolidarCarteira(ativoRepository, carteiraRepository);
            consolidaAportes(operacaoRepository);
        };
    }

    private void buscarAtivos(AtivoRepository ativoRepository) throws ParseException, GeneralSecurityException, IOException {
        log("Carregando ativos do Google Sheets");
        int count = 0;
        for (Ativo ativo : LeitorGoogleSheets.getLinhasAtivos()) {
            salvarAtivo(ativoRepository, ativo);
            ++count;
        }
        log(count + " ativos carregados");
    }

    private Ativo salvarAtivo(AtivoRepository ativoRepository, Ativo ativo) {
        Ativo ativoSaved;
        ativoSaved = new Ativo();
        ativoSaved.setTicker(ativo.getTicker());
        ativoSaved.setNome(ativo.getNome());
        ativoSaved.setCotacao(ativo.getCotacao());
        ativoSaved.setClasseAtivo(ativo.getClasseAtivo());
        ativoRepository.save(ativoSaved);
        return ativoSaved;
    }

    private void buscarOperacoes(OperacaoRepository operacaoRepository, AtivoRepository ativoRepository) throws IOException, GeneralSecurityException, ParseException {
        log("Carregando operacoes do Google Sheets");

        Ativo ativoSaved;
        int count = 0;
        for (Operacao operacao : LeitorGoogleSheets.getOperacoes()) {
            count++;
            ativoSaved = ativoRepository.findByTicker(operacao.getAtivo().getTicker());
            if (ativoSaved == null) {
                ativoSaved = salvarAtivo(ativoRepository, operacao.getAtivo());
            }
            operacao.setAtivo(ativoSaved);
            operacao.setTipo(operacao.getTipo());
            operacaoRepository.save(operacao);
        }

        log(count + " operações carregadas");
    }

    private void consolidarCarteira(AtivoRepository ativoRepository, CarteiraConsolidadaRepository carteiraRepository) {
        log("Consolidando carteira");

        CarteiraConsolidada carteira = new CarteiraConsolidada();
        carteira.setAtivos(new ArrayList<>());
        carteira.setValorCusto(BigDecimal.ZERO);
        carteira.setValorMercado(BigDecimal.ZERO);
        AtivoCarteira ativoCarteira;
        int i = 0;
        Ativo ativo;
        BigDecimal vlTotalFii = BigDecimal.ZERO;
        BigDecimal vlTotalAcoes = BigDecimal.ZERO;
        BigDecimal vlTotalETFIVVB11 = BigDecimal.ZERO;
        BigDecimal vlTotalSelic = BigDecimal.ZERO;
        BigDecimal vlTotalLci = BigDecimal.ZERO;
        BigDecimal vlTotalBTC = BigDecimal.ZERO;
        BigDecimal vlMercadoAtivo;
        if (this.isLogCarteira())
            System.out.println("  # | ATIVO  |       QTD | PREÇO MÉDIO |     COTACAO |       CUSTO | VALOR DE MERCADO | RETORNO");
        for (Object[] row : carteiraRepository.consolidaCarteira()) {
            ativoCarteira = new AtivoCarteira();
            ativo = ativoRepository.findByTicker(getStr(row[1]));
            ativoCarteira.setAtivo(ativo);
            if (ClasseAtivo.LCI.equals(ativo.getClasseAtivo())) {
                ativoCarteira.setQuantidade(BigDecimal.ONE);
                ativoCarteira.setPrecoMedio(getBigDecimal(row[4]).setScale(2, RoundingMode.HALF_UP));
                row[5] = getBigDecimal(row[5]).divide(getBigDecimal(row[2]), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
            } else {
                ativoCarteira.setQuantidade(getBigDecimal(row[2]));
                ativoCarteira.setPrecoMedio(getBigDecimal(row[3]));
            }
            ativoCarteira.setCotacao(ativo.getCotacao());
            carteira.getAtivos().add(ativoCarteira);
            carteira.setValorCusto(carteira.getValorCusto().add(ativoCarteira.getPrecoMedio().multiply(getBigDecimal(row[2]))));
            vlMercadoAtivo = ativoCarteira.getAtivo().getCotacao().multiply(ativoCarteira.getQuantidade());
            carteira.setValorMercado(carteira.getValorMercado().add(vlMercadoAtivo));
            if (this.isLogCarteira())
                showCarteira(++i, row, ativoCarteira);
            if (ativo.getClasseAtivo() != null) {
                if (ClasseAtivo.SELIC.equals(ativo.getClasseAtivo())) {
                    vlTotalSelic = vlTotalSelic.add(vlMercadoAtivo);
                } else if (ClasseAtivo.LCI.equals(ativo.getClasseAtivo())) {
                    vlTotalLci = vlTotalLci.add(vlMercadoAtivo);
                } else if (ClasseAtivo.FII.equals(ativo.getClasseAtivo())) {
                    vlTotalFii = vlTotalFii.add(vlMercadoAtivo);
                } else if (ClasseAtivo.ACOES.equals(ativo.getClasseAtivo())) {
                    vlTotalAcoes = vlTotalAcoes.add(vlMercadoAtivo);
                } else if (ClasseAtivo.ETF_IVVB11.equals(ativo.getClasseAtivo())) {
                    vlTotalETFIVVB11 = vlTotalETFIVVB11.add(vlMercadoAtivo);
                } else if (ClasseAtivo.BTC.equals(ativo.getClasseAtivo())) {
                    vlTotalBTC = vlTotalBTC.add(vlMercadoAtivo);
                }
            }
        }

        carteiraRepository.save(carteira);

        if (this.isLogCarteira()) {
            final BigDecimal retorno = carteira.getValorMercado().divide(carteira.getValorCusto(), RoundingMode.HALF_UP)
                    .subtract(new BigDecimal(1)).multiply(new BigDecimal(100));
            System.out.println(String.format("%s |%s |%s | %s | %s | %s | %s | %s",
                    mountStr(" ", 3),
                    mountStr(" ", 7),
                    mountStr(" ", 10),
                    mountStr(" ", 11),
                    mountStr(" ", 11),
                    mountStr(carteira.getValorCusto().setScale(2, RoundingMode.HALF_UP), 11),
                    mountStr(carteira.getValorMercado().setScale(2, RoundingMode.HALF_UP), 16),
                    mountStr(retorno.setScale(2, RoundingMode.HALF_UP).toString().concat(" %"), 6)));

            System.out.println();
            log(carteira.getAtivos().size() + " ativos em carteira");
        }

        if (this.isLogClasses()) {
            final BigDecimal totalRendaFixa = vlTotalSelic.add(vlTotalLci);
            if (vlTotalBTC.compareTo(BigDecimal.ZERO) > 0)
                System.out.println(String.format("  BTC    | %s | %s", mountStr(vlTotalBTC.setScale(2, RoundingMode.HALF_UP), 10), mountStr(aplicaPercentual(vlTotalBTC, carteira.getValorMercado()).toString().concat("%"), 5)));
            if (vlTotalETFIVVB11.compareTo(BigDecimal.ZERO) > 0)
                System.out.println(String.format("  IVVB11 | %s | %s", mountStr(vlTotalETFIVVB11.setScale(2, RoundingMode.HALF_UP), 10), mountStr(aplicaPercentual(vlTotalETFIVVB11, carteira.getValorMercado()).toString().concat("%"), 5)));
            if (totalRendaFixa.compareTo(BigDecimal.ZERO) > 0)
                System.out.println(String.format("  R.FIXA | %s | %s", mountStr(totalRendaFixa.setScale(2, RoundingMode.HALF_UP), 10), mountStr(aplicaPercentual(totalRendaFixa, carteira.getValorMercado()).toString().concat("%"), 5)));
            if (vlTotalAcoes.compareTo(BigDecimal.ZERO) > 0)
                System.out.println(String.format("  ACOES  | %s | %s", mountStr(vlTotalAcoes.setScale(2, RoundingMode.HALF_UP), 10), mountStr(aplicaPercentual(vlTotalAcoes, carteira.getValorMercado()).toString().concat("%"), 5)));
            if (vlTotalFii.compareTo(BigDecimal.ZERO) > 0)
                System.out.println(String.format("  FIIs   | %s | %s", mountStr(vlTotalFii.setScale(2, RoundingMode.HALF_UP), 10), mountStr(aplicaPercentual(vlTotalFii, carteira.getValorMercado()).toString().concat("%"), 5)));
            System.out.println();
        }
    }

    private void consolidaAportes(OperacaoRepository operacaoRepository) {

        if (!this.isLogAportes())
            return;

        log("Aportes mensais");

        String mesAno;
        BigDecimal aporte;
        LinkedHashMap<String, BigDecimal> aporteMap = getInitialMap();
        System.out.println("  # |            MÊS |     APORTE  |");
        for (Operacao operacao : operacaoRepository.findByOrderByDataAsc()) {
            BigDecimal vlOperacao = operacao.getValorUnitario().multiply(operacao.getQuantidade());
            mesAno = getMesAno(operacao.getData());
            aporte = aporteMap.get(mesAno);
            aporte = TipoOperacao.COMPRA.equals(operacao.getTipo()) ? aporte.add(vlOperacao) : aporte.subtract(vlOperacao);
            aporteMap.put(mesAno, aporte);
        }

        int i = 0;
        int naoOperados = 0;
        BigDecimal totalAportes = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> aporteMes : aporteMap.entrySet()) {
            if (aporteMes.getValue().compareTo(BigDecimal.ZERO) > 0) {
                ++i;
            } else {
                ++naoOperados;
            }
            totalAportes = totalAportes.add(aporteMes.getValue());
            System.out.println(String.format("%s |%s |%s |%s ", "   ", mountStr(aporteMes.getKey(), 15), mountStr(aporteMes.getValue().setScale(2, RoundingMode.HALF_UP), 11),
                    relevancia(getBigDecimal(aporteMes.getValue()))));
        }
        System.out.println();

        log("meses operados: " + i + " | não operados: " + naoOperados + " | valor total: " + totalAportes.setScale(2, RoundingMode.HALF_UP) + " | média mensal: " + totalAportes.divide(new BigDecimal(aporteMap.size()), RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_UP));
    }

    private void showCarteira(int i, Object[] row, AtivoCarteira ativoCarteira) {

        BigDecimal retorno = ativoCarteira.getCotacao().divide(ativoCarteira.getPrecoMedio(), RoundingMode.HALF_UP)
                .subtract(new BigDecimal(1)).multiply(new BigDecimal(100));

        System.out.println(String.format("%s |%s |%s | %s | %s | %s | %s | %s | %s",
                mountStr(i, 3),
                mountStr(ativoCarteira.getAtivo().getTicker(), 7),
                (ativoCarteira.getAtivo().getClasseAtivo().equals(ClasseAtivo.BTC) ?
                        mountStr(ativoCarteira.getQuantidade().setScale(8, RoundingMode.HALF_UP), 10) :
                mountStr(ativoCarteira.getQuantidade().setScale(0, RoundingMode.UNNECESSARY), 10)),
                mountStr(ativoCarteira.getPrecoMedio(), 11),
                mountStr(ativoCarteira.getCotacao(), 11),
                mountStr(getBigDecimal(row[4]).setScale(2, RoundingMode.HALF_UP), 11),
                mountStr(getBigDecimal(row[5]).setScale(2, RoundingMode.HALF_UP), 16),
                mountStr(retorno.toString().replace(".00", " %"), 6),
                relevancia(getBigDecimal(row[5]))));
    }

    private boolean isLogCarteira() {
        return loggind.containsKey(Logs.TUDO) || loggind.containsKey(Logs.CARTEIRA);
    }
    private boolean isLogClasses() {
        return loggind.containsKey(Logs.TUDO) || loggind.containsKey(Logs.CLASSE);
    }
    private boolean isLogAportes() {
        return loggind.containsKey(Logs.TUDO) || loggind.containsKey(Logs.APORTE);
    }

    private void log(String msg) {
        log.info(String.format("\n\n\t\t\t [ %s ]\n", msg));
    }
}
