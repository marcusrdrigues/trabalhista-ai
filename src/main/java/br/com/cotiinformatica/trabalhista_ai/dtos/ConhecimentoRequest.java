package br.com.cotiinformatica.trabalhista_ai.dtos;

import jakarta.validation.constraints.NotBlank;

public record ConhecimentoRequest(
        @NotBlank String texto,
        @NotBlank String fonte
) {}
