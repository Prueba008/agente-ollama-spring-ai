package com.maasteria.agent;

import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.SourceReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentAnswerTest {

    @Test
    void normalizaColeccionesNulas() {
        AgentAnswer answer = new AgentAnswer("ok", null, null, false, 0.0, null);

        assertEquals(List.of(), answer.sources());
        assertEquals(List.of(), answer.toolsUsed());
        assertEquals(List.of(), answer.warnings());
    }

    @Test
    void creaCopiasInmutablesDeLasColecciones() {
        List<SourceReference> sources = new ArrayList<>();
        sources.add(new SourceReference("doc", "ref-1"));

        AgentAnswer answer = new AgentAnswer("ok", sources, List.of("tool"), true, 0.8, List.of());
        sources.clear();

        assertEquals(1, answer.sources().size());
        assertThrows(UnsupportedOperationException.class,
                () -> answer.sources().add(new SourceReference("otro", "ref-2")));
    }

    @Test
    void rechazaConfianzaFueraDelRango() {
        assertThrows(IllegalArgumentException.class,
                () -> new AgentAnswer("ok", null, null, false, -0.01, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AgentAnswer("ok", null, null, false, 1.01, null));
    }
}
