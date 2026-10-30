package br.com.cotiinformatica.trabalhista_ai.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "execucoes_agente")
public class ExecucaoAgente {

    @Id
    public UUID id = UUID.randomUUID();

    public String usuarioId;
    public String conversaId;
    public String modelo;

    public Integer tokensEntrada;
    public Integer tokensSaida;

    @Column(precision = 18, scale = 10)
    public BigDecimal custoEstimadoUsd;

    public Long duracaoMs;

    public String status;

    @Column(columnDefinition = "TEXT")
    public String erro;

    public LocalDateTime criadoEm = LocalDateTime.now();
}
