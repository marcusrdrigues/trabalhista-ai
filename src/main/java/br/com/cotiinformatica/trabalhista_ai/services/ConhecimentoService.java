package br.com.cotiinformatica.trabalhista_ai.services;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ConhecimentoService {

    private final VectorStore vectorStore;

    public ConhecimentoService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void cadastrar(String texto, String fonte) {

        // Quebra simples para demonstração.
        // Em produção, utilizar splitters por tokens
        // e preservar referências a artigos/documentos.

        int tamanho = 1000;

        for (int inicio = 0; inicio < texto.length(); inicio += tamanho) {

            String chunk = texto.substring(
                    inicio,
                    Math.min(inicio + tamanho, texto.length())
            );

            Document documento = new Document(
                    chunk,
                    Map.of(
                            "fonte", fonte,
                            "dominio", "trabalhista"
                    )
            );

            vectorStore.add(List.of(documento));
        }
    }

    public List<Document> pesquisar(String pergunta) {
        return vectorStore.similaritySearch(pergunta);
    }
}