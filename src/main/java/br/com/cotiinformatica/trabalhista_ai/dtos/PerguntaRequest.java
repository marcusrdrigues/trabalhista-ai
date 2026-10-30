
package br.com.cotiinformatica.trabalhista_ai.dtos;

import jakarta.validation.constraints.NotBlank;

public record PerguntaRequest(
        @NotBlank String usuarioId,
        @NotBlank String conversaId,
        @NotBlank String pergunta
) {}
