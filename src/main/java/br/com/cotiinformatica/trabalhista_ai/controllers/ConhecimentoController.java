package br.com.cotiinformatica.trabalhista_ai.controllers;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.cotiinformatica.trabalhista_ai.dtos.ConhecimentoRequest;
import br.com.cotiinformatica.trabalhista_ai.services.ConhecimentoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/conhecimentos")
public class ConhecimentoController {

    private final ConhecimentoService service;

    public ConhecimentoController(ConhecimentoService service) {
        this.service = service;
    }

    @PostMapping
    public String cadastrar(
            @Valid @RequestBody ConhecimentoRequest request) {

        service.cadastrar(
                request.texto(),
                request.fonte()
        );

        return "Conhecimento armazenado.";
    }

    @GetMapping("/pesquisar")
    public List<String> pesquisar(@RequestParam String pergunta) {

        return service.pesquisar(pergunta)
                .stream()
                .map(Document::getText)
                .toList();
    }
}
