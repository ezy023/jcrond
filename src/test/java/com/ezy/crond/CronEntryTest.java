package com.ezy.crond;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;

import org.junit.Test;

import static org.junit.Assert.*;

public class CronEntryTest {

    @Test
    public void testParseCronEntry() {
        String entry = "5 0 * * * echo \"Hello World\"";
        CronEntry cronEntry = CronEntry.parse(entry);

        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 1),
                                                   LocalTime.of(0, 5),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 2),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        assertEquals(expectedNextExecution, cronEntry.getCronTime().nextExecution(fixedTime));
        assertEquals("echo \"Hello World\"", cronEntry.getCommand());
    }
}
