package com.maasteria.agent;

import com.maasteria.agent.infrastructure.tool.SystemTools;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.zone.ZoneRulesException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SystemToolsTest {

    private final SystemTools tools = new SystemTools(
            Clock.fixed(Instant.parse("2026-07-15T18:30:00Z"), ZoneOffset.UTC));

    @Test
    void devuelveHoraEnZonaSolicitada() {
        assertEquals(
                "2026-07-15T15:30:00-03:00[America/Argentina/Buenos_Aires]",
                tools.getSystemTime("America/Argentina/Buenos_Aires"));
    }

    @Test
    void rechazaZonaHorariaInvalida() {
        assertThrows(ZoneRulesException.class, () -> tools.getSystemTime("Zona/Inexistente"));
    }
}
