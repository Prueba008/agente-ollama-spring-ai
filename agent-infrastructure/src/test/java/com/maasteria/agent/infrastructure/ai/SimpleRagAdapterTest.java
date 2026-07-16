package com.maasteria.agent.infrastructure.ai;

import com.maasteria.agent.domain.model.AgentQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleRagAdapterTest {

    private SimpleRagAdapter ragAdapter;
    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = new StandardEnvironment();
        ragAdapter = new SimpleRagAdapter(environment);
    }

    @Test
    void ingestaYRecuperaDocumentos() {
        ragAdapter.ingestDocument("Contenido de prueba del documento", "documento-test.md");
        ragAdapter.ingestDocument("Otro contenido importante", "otro-doc.txt");

        AgentQuestion question = new AgentQuestion("conv-test", "contenido documento importante");
        
        List<String> context = ragAdapter.retrieveRelevantContext(question);
        
        assertEquals(2, context.size());
        assertTrue(context.get(0).contains("[documento-test.md]"));
        assertTrue(context.get(0).contains("Contenido de prueba del documento"));
        assertTrue(context.get(1).contains("[otro-doc.txt]"));
        assertTrue(context.get(1).contains("Otro contenido importante"));
    }

    @Test
    void recuperaDocumentosLimitadoPorTopK() {
        ragAdapter.ingestDocument("Documento 1", "doc1.md");
        ragAdapter.ingestDocument("Documento 2", "doc2.md");
        ragAdapter.ingestDocument("Documento 3", "doc3.md");
        ragAdapter.ingestDocument("Documento 4", "doc4.md");
        ragAdapter.ingestDocument("Documento 5", "doc5.md");

        AgentQuestion question = new AgentQuestion("conv-test", "Documento");
        
        List<String> context = ragAdapter.retrieveRelevantContext(question);
        
        // Default top-k is 4
        assertEquals(4, context.size());
    }

    @Test
    void devuelveListaVaciaCuandoNoHayDocumentos() {
        AgentQuestion question = new AgentQuestion("conv-test", "¿Qué hay?");
        
        List<String> context = ragAdapter.retrieveRelevantContext(question);
        
        assertTrue(context.isEmpty());
    }

    @Test
    void formateaDocumentosCorrectamente() {
        ragAdapter.ingestDocument("Mi contenido", "mi-archivo.md");
        
        AgentQuestion question = new AgentQuestion("conv-test", "contenido");
        List<String> context = ragAdapter.retrieveRelevantContext(question);
        
        assertEquals(1, context.size());
        assertEquals("[mi-archivo.md] Mi contenido", context.get(0));
    }
}
