package br.com.cotiinformatica.trabalhista_ai.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "solicitacoes_email")
public class SolicitacaoEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String destinatario;

    public String assunto;

    @Column(columnDefinition = "TEXT")
    public String mensagem;

    public String status = "PENDENTE";

    public LocalDateTime criadoEm = LocalDateTime.now();

    public LocalDateTime decididoEm;

    public String aprovadoPor;

    @Column(columnDefinition = "TEXT")
    public String justificativa;

    @Version
    public Long versao;
}
