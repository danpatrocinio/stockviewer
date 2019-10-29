package com.stockviewer.br.repository;

import com.stockviewer.br.model.CarteiraConsolidada;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Collection;

@RepositoryRestResource(collectionResourceRel = "carteira", path = "carteira")
public interface CarteiraConsolidadaRepository extends PagingAndSortingRepository<CarteiraConsolidada, Long> {


    @Query(
         value = "SELECT * FROM (" +
                 "             select operacao.id_ativo, ticker," +
                 "             sum(case when (tipo = 0 or tipo = 3) then quantidade else (-1 * quantidade ) end) as quantidade," +
                 "             ROUND(case when (sum(case when (tipo = 0 or tipo = 3) then quantidade else (-1 * quantidade ) end) > 0) then " +
                 "                    (sum(case when (tipo = 0 or tipo = 3) then (valor_unitario * quantidade) else (-1 * valor_unitario * quantidade ) end) / sum(case when (tipo = 0 or tipo = 3) then quantidade else (-1 * quantidade ) end)) end, 2) as preco_medio," +
                 "             sum(case when (tipo = 0 or tipo = 3) then (valor_unitario * quantidade) else (-1 * valor_unitario * quantidade ) end) as total_custo," +
                 "             sum(case when (tipo = 0 or tipo = 3) then (ativo.cotacao * quantidade) else (-1 * ativo.cotacao * quantidade ) end) as total_mercado" +
                 "            from operacao" +
                 "            join ativo on (ativo.id_ativo=operacao.id_ativo)" +
                 "            group by operacao.id_ativo)" +
                 "        WHERE total_mercado > 0", nativeQuery = true
    )
    Collection<Object[]> consolidaCarteira();

}
