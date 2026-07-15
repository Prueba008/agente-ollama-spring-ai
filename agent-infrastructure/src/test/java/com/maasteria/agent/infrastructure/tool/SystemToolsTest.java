package com.maasteria.agent.infrastructure.tool;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemToolsTest {

    @Test
    void devuelveLaHoraEnLaZonaSolicitada() {
        Clock clock = new FakeClock(Instant.parse("2026-07-15T12:34:56Z"));
        SystemTools tools = new SystemTools(clock);

        String result = tools.getSystemTime("America/Argentina/Buenos_Aires");

        ZonedDateTime expected = ZonedDateTime.ofInstant(clock.instant(), ZoneId.of("America/Argentina/Buenos_Aires"));
        assertEquals(expected.format(DateTimeFormatter.ISO_ZONED_DATE_TIME), result);
    }

    private static final class FakeClock extends Clock {
        private final Instant instant;
        private final ZoneId zone;

        private FakeClock(Instant instant) {
            this(instant, ZoneId.of("UTC"));
        }

        private FakeClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new FakeClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
