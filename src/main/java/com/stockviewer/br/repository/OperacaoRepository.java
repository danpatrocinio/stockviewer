package com.stockviewer.br.repository;

import com.stockviewer.br.model.Operacao;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

public interface OperacaoRepository extends CrudRepository<Operacao, Long> {
}
