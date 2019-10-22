package com.stockviewer.br.controller;

import com.stockviewer.br.model.Operacao;
import com.stockviewer.br.repository.OperacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/operacoes")
public class OperacaoController {

    @Autowired
    private OperacaoRepository repository;

    @GetMapping
    public Iterable<Operacao> list() {
        return repository.findAll();
    }

}
