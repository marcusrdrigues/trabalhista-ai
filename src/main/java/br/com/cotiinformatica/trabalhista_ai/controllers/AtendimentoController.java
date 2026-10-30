package br.com.cotiinformatica.trabalhista_ai.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cotiinformatica.trabalhista_ai.dtos.AtendimentoResultado;
import br.com.cotiinformatica.trabalhista_ai.dtos.PerguntaRequest;
import br.com.cotiinformatica.trabalhista_ai.services.AtendimentoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/atendimentos")
public class AtendimentoController {

    private final AtendimentoService service;

    public AtendimentoController(AtendimentoService service) {
        this.service = service;
    }

    @PostMapping
    public AtendimentoResultado executar(
            @Valid @RequestBody PerguntaRequest request) {

        return service.executar(request);
    }
}
