package br.com.cotiinformatica.trabalhista_ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import br.com.cotiinformatica.trabalhista_ai.entities.SolicitacaoEmail;
import br.com.cotiinformatica.trabalhista_ai.repositories.SolicitacaoEmailRepository;

@Component
public class EmailTools {

    private final SolicitacaoEmailRepository repository;

    public EmailTools(SolicitacaoEmailRepository repository) {
        this.repository = repository;
    }

    @Tool(description = """
            Cria uma solicitação de envio de e-mail
            que obrigatoriamente necessita de aprovação
            humana antes do envio.
            Esta ferramenta NUNCA envia o e-mail.
            """)
    public String solicitarEnvioEmail(
            @ToolParam(description = "E-mail de destino")
            String destinatario,

            @ToolParam(description = "Assunto do e-mail")
            String assunto,

            @ToolParam(description = "Corpo do e-mail")
            String mensagem) {

        SolicitacaoEmail solicitacao = new SolicitacaoEmail();

        solicitacao.destinatario = destinatario;
        solicitacao.assunto = assunto;
        solicitacao.mensagem = mensagem;

        repository.save(solicitacao);

        return "Solicitação " + solicitacao.id
                + " criada com status PENDENTE. "
                + "O e-mail ainda não foi enviado.";
    }
}
