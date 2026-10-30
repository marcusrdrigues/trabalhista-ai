package br.com.cotiinformatica.trabalhista_ai.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.cotiinformatica.trabalhista_ai.entities.ExecucaoAgente;
import br.com.cotiinformatica.trabalhista_ai.repositories.ExecucaoAgenteRepository;

@Service
public class AuditoriaService {

    private final ExecucaoAgenteRepository repository;

    @Value("${app.pricing.input-per-million}")
    private BigDecimal precoEntrada;

    @Value("${app.pricing.output-per-million}")
    private BigDecimal precoSaida;

    public AuditoriaService(ExecucaoAgenteRepository repository) {
        this.repository = repository;
    }

    public ExecucaoAgente registrar(
            String usuarioId,
            String conversaId,
            ChatResponse response,
            long duracaoMs) {

        ExecucaoAgente execucao = new ExecucaoAgente();

        execucao.usuarioId = usuarioId;
        execucao.conversaId = conversaId;
        execucao.duracaoMs = duracaoMs;
        execucao.status = "SUCESSO";

        if (response.getMetadata() != null) {

            execucao.modelo =
                    response.getMetadata().getModel();

            var usage = response.getMetadata().getUsage();

            if (usage != null) {

                long entrada = usage.getPromptTokens() == null
                        ? 0L : usage.getPromptTokens();

                long saida = usage.getCompletionTokens() == null
                        ? 0L : usage.getCompletionTokens();

                execucao.tokensEntrada =
                        Math.toIntExact(entrada);

                execucao.tokensSaida =
                        Math.toIntExact(saida);

                BigDecimal custoEntrada =
                        BigDecimal.valueOf(entrada)
                                .multiply(precoEntrada);

                BigDecimal custoSaida =
                        BigDecimal.valueOf(saida)
                                .multiply(precoSaida);

                execucao.custoEstimadoUsd =
                        custoEntrada.add(custoSaida)
                                .divide(
                                        BigDecimal.valueOf(1_000_000),
                                        10,
                                        RoundingMode.HALF_UP
                                );
            }
        }

        return repository.save(execucao);
    }

    public void registrarErro(
            String usuarioId,
            String conversaId,
            long duracaoMs,
            Exception exception) {

        ExecucaoAgente execucao = new ExecucaoAgente();

        execucao.usuarioId = usuarioId;
        execucao.conversaId = conversaId;
        execucao.duracaoMs = duracaoMs;
        execucao.status = "ERRO";
        execucao.erro = exception.getMessage();

        repository.save(execucao);
    }
}
