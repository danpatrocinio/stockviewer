package com.stockviewer.br;

import com.stockviewer.br.model.Ativo;
import com.stockviewer.br.model.AtivoCarteira;
import com.stockviewer.br.model.CarteiraConsolidada;
import com.stockviewer.br.model.Operacao;
import com.stockviewer.br.model.enums.ClasseAtivo;
import com.stockviewer.br.model.enums.ConfigColuna;
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

    private void buscarAtivos(AtivoRepository ativoRepository) throws GeneralSecurityException, IOException {
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
        final BigDecimal dolar = ativoRepository.findByTicker("USDBRL").getCotacao();
        for (Operacao operacao : LeitorGoogleSheets.getOperacoes()) {
            count++;
            ativoSaved = ativoRepository.findByTicker(operacao.getAtivo().getTicker());
            if (ativoSaved == null) {
                continue;
            }

            if (ClasseAtivo.isAtivoDolarizado(ativoSaved.getTicker()))
                operacao.setValorUnitario(operacao.getValorUnitario().multiply(dolar).setScale(2, RoundingMode.UP));

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
        BigDecimal vlTotalSelicOrDI = BigDecimal.ZERO;
        BigDecimal vlTotalLci = BigDecimal.ZERO;
        BigDecimal vlTotalBTC = BigDecimal.ZERO;
        BigDecimal vlMercadoAtivo;
        ClasseAtivo classe = null;
        if (this.isLogCarteira())
            System.out.println("  # | ATIVO  |       QTD | PREÇO MÉDIO |     COTACAO |       CUSTO | VALOR MERCADO | RETORNO");
        for (Object[] row : carteiraRepository.consolidaCarteira()) {
            ativoCarteira = new AtivoCarteira();
            ativo = ativoRepository.findByTicker(getStr(row[1]));
            ativoCarteira.setAtivo(ativo);
            if (ClasseAtivo.LCI.equals(ativo.getClasseAtivo())) {
                ativoCarteira.setQuantidade(BigDecimal.ONE);
                ativoCarteira.setPrecoMedio(getBigDecimal(row[4]));
                row[5] = getBigDecimal(row[5]).divide(getBigDecimal(row[2]), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
            } else {
                ativoCarteira.setQuantidade(getBigDecimal(row[2]));
                ativoCarteira.setPrecoMedio(getBigDecimal(row[3]).setScale(4, RoundingMode.HALF_UP));
            }
            ativoCarteira.setCotacao(ativo.getCotacao());
            carteira.getAtivos().add(ativoCarteira);
            carteira.setValorCusto(carteira.getValorCusto().add(ativoCarteira.getPrecoMedio().multiply(getBigDecimal(row[2]))));
            vlMercadoAtivo = ativoCarteira.getAtivo().getCotacao().multiply(ativoCarteira.getQuantidade());
            carteira.setValorMercado(carteira.getValorMercado().add(vlMercadoAtivo));
            if (this.isLogCarteira()) {
                if (!ativoCarteira.getAtivo().getClasseAtivo().equals(classe)) {
                    classe = ativoCarteira.getAtivo().getClasseAtivo();
                    System.out.println();
                }
                showCarteira(++i, row, ativoCarteira);
            }
            if (ativo.getClasseAtivo() != null) {
                if (ClasseAtivo.SELIC_CDI_DI.equals(ativo.getClasseAtivo())) {
                    vlTotalSelicOrDI = vlTotalSelicOrDI.add(vlMercadoAtivo);
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
                    .subtract(new BigDecimal(1)).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
            System.out.printf("\n%s  %s  %s   %s   %s   %s | %s | %s%n",
                            ConfigColuna.COLUNA_BASE3.mountColuna(" "),
                    ConfigColuna.ATIVO.mountColuna(" "),
                    ConfigColuna.QTD.mountColuna(" "),
                    ConfigColuna.PRECO_MEDIO.mountColuna(" "),
                    ConfigColuna.COTACAO.mountColuna(" "),
                    ConfigColuna.CUSTO.mountColuna(carteira.getValorCusto().setScale(2, RoundingMode.HALF_UP)),
                    ConfigColuna.VL_MERCADO.mountColuna(carteira.getValorMercado().setScale(2, RoundingMode.HALF_UP)),
                    ConfigColuna.RETORNO.mountColuna(retorno.toString().concat(" %")));

            System.out.println();
            log(carteira.getAtivos().size() + " ativos em carteira");
        }

        if (this.isLogClasses()) {
            final BigDecimal totalRendaFixa = vlTotalSelicOrDI.add(vlTotalLci);
            if (vlTotalBTC.compareTo(BigDecimal.ZERO) > 0)
                System.out.printf("  BTC        | %s | %s | %s%n",
                        ConfigColuna.VL_TOTAL_CLASSE.mountColuna(vlTotalBTC.setScale(2, RoundingMode.HALF_UP)),
                        ConfigColuna.PERCENTUAL_CLASSE.mountColuna(aplicaPercentual(vlTotalBTC, carteira.getValorMercado()).toString().concat(" %")),
                        relevancia(vlTotalBTC));
            if (vlTotalETFIVVB11.compareTo(BigDecimal.ZERO) > 0)
                System.out.printf("  EXTERIOR   | %s |  %s | %s%n",
                        ConfigColuna.VL_TOTAL_CLASSE.mountColuna(vlTotalETFIVVB11.setScale(2, RoundingMode.HALF_UP)),
                        ConfigColuna.PERCENTUAL_CLASSE.mountColuna(aplicaPercentual(vlTotalETFIVVB11, carteira.getValorMercado()).toString().concat(" %")),
                        relevancia(vlTotalETFIVVB11));
            if (totalRendaFixa.compareTo(BigDecimal.ZERO) > 0)
                System.out.printf("  RENDA FIXA | %s | %s | %s%n",
                        ConfigColuna.VL_TOTAL_CLASSE.mountColuna(totalRendaFixa.setScale(2, RoundingMode.HALF_UP)),
                        ConfigColuna.PERCENTUAL_CLASSE.mountColuna(aplicaPercentual(totalRendaFixa, carteira.getValorMercado()).toString().concat(" %")),
                        relevancia(totalRendaFixa));
            if (vlTotalAcoes.compareTo(BigDecimal.ZERO) > 0)
                System.out.printf("  ACOES BR   | %s | %s | %s%n",
                        ConfigColuna.VL_TOTAL_CLASSE.mountColuna(vlTotalAcoes.setScale(2, RoundingMode.HALF_UP)),
                        ConfigColuna.PERCENTUAL_CLASSE.mountColuna(aplicaPercentual(vlTotalAcoes, carteira.getValorMercado()).toString().concat(" %")),
                        relevancia(vlTotalAcoes));
            if (vlTotalFii.compareTo(BigDecimal.ZERO) > 0)
                System.out.printf("  FUNDO IMOB | %s | %s | %s%n",
                        ConfigColuna.VL_TOTAL_CLASSE.mountColuna(vlTotalFii.setScale(2, RoundingMode.HALF_UP)),
                        ConfigColuna.PERCENTUAL_CLASSE.mountColuna(aplicaPercentual(vlTotalFii, carteira.getValorMercado()).toString().concat(" %")),
                        relevancia(vlTotalFii));
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
        System.out.println("  # |            MÊS |     APORTE |");
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
            System.out.printf("%s |%s |%s |%s %n", "   ",
                    ConfigColuna.MES_APORTE.mountColuna(aporteMes.getKey()),
                    ConfigColuna.VL_APORTE.mountColuna(aporteMes.getValue().setScale(2, RoundingMode.HALF_UP)),
                    relevancia(getBigDecimal(aporteMes.getValue())));
        }
        System.out.println();

        log("meses operados: " + i + " | não operados: " + naoOperados + " | valor total: " + totalAportes.setScale(2, RoundingMode.HALF_UP) + " | média mensal: " + totalAportes.divide(new BigDecimal(aporteMap.size()), RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_UP));
    }

    private void showCarteira(int i, Object[] row, AtivoCarteira ativoCarteira) {

        BigDecimal retorno = ativoCarteira.getCotacao().divide(ativoCarteira.getPrecoMedio(), RoundingMode.HALF_UP)
                .subtract(new BigDecimal(1)).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);

        String  sQtd = ativoCarteira.getAtivo().getClasseAtivo().equals(ClasseAtivo.BTC) ?
                ativoCarteira.getQuantidade().setScale(8, RoundingMode.HALF_UP).toString() :
                ativoCarteira.getQuantidade().setScale(4, RoundingMode.HALF_UP).toString();
        if (sQtd.endsWith(".0000"))
            sQtd = ativoCarteira.getQuantidade().setScale(4, RoundingMode.HALF_UP).toString().replace(".0000", "");

        System.out.printf("%s |%s |%s | %s | %s | %s | %s | %s | %s%n",
                ConfigColuna.COLUNA_BASE3.mountColuna(i),
                ConfigColuna.ATIVO.mountColuna(ativoCarteira.getAtivo().getTicker()),
                ConfigColuna.QTD.mountColuna(sQtd),
                ConfigColuna.PRECO_MEDIO.mountColuna(ativoCarteira.getPrecoMedio().setScale(2, RoundingMode.HALF_UP)),
                ConfigColuna.COTACAO.mountColuna(ativoCarteira.getCotacao().setScale(2, RoundingMode.HALF_UP)),
                ConfigColuna.CUSTO.mountColuna(getBigDecimal(row[4]).setScale(2, RoundingMode.HALF_UP)),
                ConfigColuna.VL_MERCADO.mountColuna(getBigDecimal(row[5]).setScale(2, RoundingMode.HALF_UP)),
                ConfigColuna.RETORNO.mountColuna(retorno.toString().concat(" %")),
                relevancia(getBigDecimal(row[5])));
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
