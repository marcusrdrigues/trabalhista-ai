package br.com.cotiinformatica.trabalhista_ai.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.cotiinformatica.trabalhista_ai.entities.SolicitacaoEmail;
import br.com.cotiinformatica.trabalhista_ai.services.EmailService;

@RestController
@RequestMapping("/api/aprovacoes")
public class AprovacaoController {

    private final EmailService service;

    public AprovacaoController(EmailService service) {
        this.service = service;
    }

    @GetMapping("/pendentes")
    public List<SolicitacaoEmail> pendentes() {
        return service.pendentes();
    }

    @PostMapping("/{id}/aprovar")
    public SolicitacaoEmail aprovar(
            @PathVariable Long id,
            @RequestParam String responsavel) {

        return service.aprovar(id, responsavel);
    }

    @PostMapping("/{id}/rejeitar")
    public SolicitacaoEmail rejeitar(
            @PathVariable Long id,
            @RequestParam String responsavel) {

        return service.rejeitar(id, responsavel);
    }
}
