package br.com.cotiinformatica.trabalhista_ai.dtos;

import java.util.List;

public record AtendimentoResultado(
        String assunto,
        String resposta,
        String fundamentacao,
        List<String> fontes,
        boolean informacaoEncontrada,
        boolean encaminharEspecialista
) {}
