package com.stockviewer.br.repository;

import com.stockviewer.br.model.Ativo;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "ativos", path = "ativos")
public interface AtivoRepository extends PagingAndSortingRepository<Ativo, Long> {

    Ativo findByTicker(String ticker);

}
