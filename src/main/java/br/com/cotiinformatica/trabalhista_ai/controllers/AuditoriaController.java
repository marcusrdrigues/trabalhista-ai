
package br.com.cotiinformatica.trabalhista_ai.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cotiinformatica.trabalhista_ai.entities.ExecucaoAgente;
import br.com.cotiinformatica.trabalhista_ai.repositories.ExecucaoAgenteRepository;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final ExecucaoAgenteRepository repository;

    public AuditoriaController(ExecucaoAgenteRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<ExecucaoAgente> consultar(
            @PathVariable String usuarioId) {

        return repository.findByUsuarioId(usuarioId);
    }
}
