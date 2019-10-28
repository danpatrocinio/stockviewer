package com.stockviewer.br.repository;

import com.stockviewer.br.model.Operacao;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "operacoes", path = "operacoes")
public interface OperacaoRepository extends PagingAndSortingRepository<Operacao, Long> {

    List<Operacao> findByOrderByDataAsc();

}
