package br.com.cotiinformatica.trabalhista_ai.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.cotiinformatica.trabalhista_ai.entities.SolicitacaoEmail;

public interface SolicitacaoEmailRepository
        extends JpaRepository<SolicitacaoEmail, Long> {

    List<SolicitacaoEmail> findByStatus(String status);
}
