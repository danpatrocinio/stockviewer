package com.stockviewer.br.repository;

import com.stockviewer.br.model.CarteiraConsolidada;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "carteira", path = "carteira")
public interface CarteiraConsolidadaRepository extends PagingAndSortingRepository<CarteiraConsolidada, Long> {
}
