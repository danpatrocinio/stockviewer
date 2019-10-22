package com.stockviewer.br.repository;

import com.stockviewer.br.model.Ativo;
import org.springframework.data.repository.CrudRepository;

public interface AtivoRepository extends CrudRepository<Ativo, Long> {

    Ativo findByTicker(String ticker);

}
