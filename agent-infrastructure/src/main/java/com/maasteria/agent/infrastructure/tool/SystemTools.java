package com.maasteria.agent.infrastructure.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class SystemTools {

    private final Clock clock;

    public SystemTools(Clock clock) {
        this.clock = clock;
    }

    @Tool(description = "Devuelve la fecha y hora actual del servidor en la zona horaria solicitada")
    public String getSystemTime(
            @ToolParam(description = "Zona horaria IANA, por ejemplo America/Argentina/Buenos_Aires") String zoneId) {
        return ZonedDateTime.now(clock.withZone(java.time.ZoneId.of(zoneId)))
                .format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
}
