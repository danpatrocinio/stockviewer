package com.stockviewer.br.controller;

import com.stockviewer.br.model.Ativo;
import com.stockviewer.br.repository.AtivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/ativos")
public class AtivoController {

    @Autowired
    private AtivoRepository repository;

    @GetMapping
    public Iterable<Ativo> list() {
        return repository.findAll();
    }

}
