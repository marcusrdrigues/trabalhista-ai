package br.com.cotiinformatica.trabalhista_ai.controllers;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cotiinformatica.trabalhista_ai.tools.EmailTools;

@RestController
@RequestMapping("/api/agente/acoes")
public class AcaoAgenteController {

    private final ChatClient chatClient;
    private final EmailTools emailTools;

    public AcaoAgenteController(
            ChatClient chatClient,
            EmailTools emailTools) {

        this.chatClient = chatClient;
        this.emailTools = emailTools;
    }

    @PostMapping
    public Map<String, String> executar(
            @RequestBody Map<String, String> request) {

        String comando = request.get("comando");
        String conversaId = request.get("conversaId");

        if (comando == null || comando.isBlank()
                || conversaId == null || conversaId.isBlank()) {
            throw new IllegalArgumentException(
                    "comando e conversaId são obrigatórios");
        }

        String resposta = chatClient.prompt()
                .system("""
                        Você é um assistente de RH.
                        Quando solicitarem o envio de e-mail,
                        utilize a ferramenta para registrar
                        uma solicitação de aprovação.

                        Nunca diga que o e-mail foi enviado
                        quando estiver pendente.
                        """)
                .user(comando)
                .tools(emailTools)
                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        conversaId
                ))
                .call()
                .content();

        return Map.of("resultado", resposta);
    }
}
