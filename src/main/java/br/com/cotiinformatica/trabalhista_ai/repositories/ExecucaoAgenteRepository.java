package br.com.cotiinformatica.trabalhista_ai.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.cotiinformatica.trabalhista_ai.entities.ExecucaoAgente;

public interface ExecucaoAgenteRepository
        extends JpaRepository<ExecucaoAgente, UUID> {

    List<ExecucaoAgente> findByUsuarioId(String usuarioId);
}