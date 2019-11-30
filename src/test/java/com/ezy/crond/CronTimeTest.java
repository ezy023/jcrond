package com.ezy.crond;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class CronTimeTest {

    @Test
    public void testParseAllNumbers() {
        String crontab = "1 2 3 4 5";
        CronTime entry = CronTime.parse(crontab);

        assertEquals(1, (int) entry.getMinutes().getValues().get(0));
        assertEquals(CronTime.FieldType.NUMBER, entry.getMinutes().getType());
        assertEquals(2, (int) entry.getHours().getValues().get(0));
        assertEquals(CronTime.FieldType.NUMBER, entry.getHours().getType());
        assertEquals(3, (int) entry.getDaysOfMonth().getValues().get(0));
        assertEquals(CronTime.FieldType.NUMBER, entry.getDaysOfMonth().getType());
        assertEquals(4, (int) entry.getMonths().getValues().get(0));
        assertEquals(CronTime.FieldType.NUMBER, entry.getMonths().getType());
        assertEquals(5, (int) entry.getDaysOfWeek().getValues().get(0));
        assertEquals(CronTime.FieldType.NUMBER, entry.getDaysOfWeek().getType());
    }

    @Test
    public void testParseWithWildcard() {
        String crontab = "1 2 3 4 *";

        CronTime entry = CronTime.parse(crontab);

        assertEquals(CronTime.FieldType.NUMBER, entry.getMonths().getType());
        assertEquals(CronTime.FieldType.WILDCARD, entry.getDaysOfWeek().getType());
    }

    @Test
    public void testParseRange() {
        String crontab = "1-14 2 3 4 5";

        CronTime entry = CronTime.parse(crontab);

        assertEquals(CronTime.FieldType.RANGE, entry.getMinutes().getType());
        assertEquals(2, (int) entry.getMinutes().getValues().size());
        assertEquals(1, (int) entry.getMinutes().getValues().get(0));  // Begin range
        assertEquals(14, (int) entry.getMinutes().getValues().get(1)); // End range

        assertEquals(CronTime.FieldType.NUMBER, entry.getHours().getType());
        assertEquals(CronTime.FieldType.NUMBER, entry.getMonths().getType());
    }

    @Test
    public void testParseList() {
        String crontab = "1,3,5,8,13 * * * *";

        CronTime entry = CronTime.parse(crontab);

        assertEquals(CronTime.FieldType.LIST, entry.getMinutes().getType());
        assertEquals(5, (int) entry.getMinutes().getValues().size());
        assertEquals(1, (int) entry.getMinutes().getValues().get(0));
        assertEquals(3, (int) entry.getMinutes().getValues().get(1));
        assertEquals(5, (int) entry.getMinutes().getValues().get(2));
        assertEquals(8, (int) entry.getMinutes().getValues().get(3));
        assertEquals(13, (int) entry.getMinutes().getValues().get(4));

        assertEquals(CronTime.FieldType.WILDCARD, entry.getHours().getType());
        assertEquals(CronTime.FieldType.WILDCARD, entry.getMonths().getType());
    }

    @Test
    public void testParseInterval() {
        String crontab = "*/5 * * * *";

        CronTime entry = CronTime.parse(crontab);

        assertEquals(CronTime.FieldType.INTERVAL, entry.getMinutes().getType());
        assertEquals(1, (int) entry.getMinutes().getValues().size());
        assertEquals(5, (int) entry.getMinutes().getValues().get(0));
    }

    @Test
    public void testParseMultipleTypes() {
        String crontab = "*/2 * 10,11,12 3-6 11";

        CronTime entry = CronTime.parse(crontab);

        assertEquals(CronTime.FieldType.INTERVAL, entry.getMinutes().getType());
        assertEquals(2, (int) entry.getMinutes().getValues().get(0));

        assertEquals(CronTime.FieldType.WILDCARD, entry.getHours().getType());

        assertEquals(CronTime.FieldType.LIST, entry.getDaysOfMonth().getType());
        assertEquals(3, (int) entry.getDaysOfMonth().getValues().size());
        assertEquals(10, (int) entry.getDaysOfMonth().getValues().get(0));
        assertEquals(11, (int) entry.getDaysOfMonth().getValues().get(1));
        assertEquals(12, (int) entry.getDaysOfMonth().getValues().get(2));

        assertEquals(CronTime.FieldType.RANGE, entry.getMonths().getType());
        assertEquals(2, (int) entry.getMonths().getValues().size());
        assertEquals(3, (int) entry.getMonths().getValues().get(0));
        assertEquals(6, (int) entry.getMonths().getValues().get(1));

        assertEquals(CronTime.FieldType.NUMBER, entry.getDaysOfWeek().getType());
        assertEquals(11, (int) entry.getDaysOfWeek().getValues().get(0));
    }
}
