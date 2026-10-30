package br.com.cotiinformatica.trabalhista_ai.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import br.com.cotiinformatica.trabalhista_ai.entities.SolicitacaoEmail;
import br.com.cotiinformatica.trabalhista_ai.repositories.SolicitacaoEmailRepository;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SolicitacaoEmailRepository repository;

    @Value("${app.mail.from}")
    private String remetente;

    public EmailService(
            JavaMailSender mailSender,
            SolicitacaoEmailRepository repository) {

        this.mailSender = mailSender;
        this.repository = repository;
    }

    public List<SolicitacaoEmail> pendentes() {
        return repository.findByStatus("PENDENTE");
    }

    public synchronized SolicitacaoEmail aprovar(
            Long id, String responsavel) {

        SolicitacaoEmail s = repository.findById(id)
                .orElseThrow();

        if (!s.status.equals("PENDENTE")) {
            throw new IllegalStateException(
                    "Solicitação não está pendente.");
        }

        SimpleMailMessage email = new SimpleMailMessage();

        email.setFrom(remetente);
        email.setTo(s.destinatario);
        email.setSubject(s.assunto);
        email.setText(s.mensagem);

        mailSender.send(email);

        s.status = "ENVIADO";
        s.aprovadoPor = responsavel;
        s.decididoEm = LocalDateTime.now();

        return repository.save(s);
    }

    public synchronized SolicitacaoEmail rejeitar(
            Long id, String responsavel) {

        SolicitacaoEmail s = repository.findById(id)
                .orElseThrow();

        if (!s.status.equals("PENDENTE")) {
            throw new IllegalStateException(
                    "Solicitação não está pendente.");
        }

        s.status = "REJEITADO";
        s.aprovadoPor = responsavel;
        s.decididoEm = LocalDateTime.now();

        return repository.save(s);
    }
}
