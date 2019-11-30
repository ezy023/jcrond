package com.ezy.crond;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class CronTimeTest {

    @Test
    public void testParseAllNumbers() {
        String crontab = "1 2 3 4 5";
        CronTime cronTime = CronTime.parse(crontab);

        assertEquals(1, (int) cronTime.getMinutes().getValues().get(0));
        assertEquals(CronTime.FieldType.NUMBER, cronTime.getMinutes().getType());
        assertEquals(2, (int) cronTime.getHours().getValues().get(0));
        assertEquals(CronTime.FieldType.NUMBER, cronTime.getHours().getType());
        assertEquals(3, (int) cronTime.getDaysOfMonth().getValues().get(0));
        assertEquals(CronTime.FieldType.NUMBER, cronTime.getDaysOfMonth().getType());
        assertEquals(4, (int) cronTime.getMonths().getValues().get(0));
        assertEquals(CronTime.FieldType.NUMBER, cronTime.getMonths().getType());
        assertEquals(5, (int) cronTime.getDaysOfWeek().getValues().get(0));
        assertEquals(CronTime.FieldType.NUMBER, cronTime.getDaysOfWeek().getType());
    }

    @Test
    public void testParseWithWildcard() {
        String crontab = "1 2 3 4 *";

        CronTime cronTime = CronTime.parse(crontab);

        assertEquals(CronTime.FieldType.NUMBER, cronTime.getMonths().getType());
        assertEquals(CronTime.FieldType.WILDCARD, cronTime.getDaysOfWeek().getType());
    }

    @Test
    public void testParseRange() {
        String crontab = "1-14 2 3 4 5";

        CronTime cronTime = CronTime.parse(crontab);

        assertEquals(CronTime.FieldType.RANGE, cronTime.getMinutes().getType());
        assertEquals(2, (int) cronTime.getMinutes().getValues().size());
        assertEquals(1, (int) cronTime.getMinutes().getValues().get(0));  // Begin range
        assertEquals(14, (int) cronTime.getMinutes().getValues().get(1)); // End range

        assertEquals(CronTime.FieldType.NUMBER, cronTime.getHours().getType());
        assertEquals(CronTime.FieldType.NUMBER, cronTime.getMonths().getType());
    }

    @Test
    public void testParseList() {
        String crontab = "1,3,5,8,13 * * * *";

        CronTime cronTime = CronTime.parse(crontab);

        assertEquals(CronTime.FieldType.LIST, cronTime.getMinutes().getType());
        assertEquals(5, (int) cronTime.getMinutes().getValues().size());
        assertEquals(1, (int) cronTime.getMinutes().getValues().get(0));
        assertEquals(3, (int) cronTime.getMinutes().getValues().get(1));
        assertEquals(5, (int) cronTime.getMinutes().getValues().get(2));
        assertEquals(8, (int) cronTime.getMinutes().getValues().get(3));
        assertEquals(13, (int) cronTime.getMinutes().getValues().get(4));

        assertEquals(CronTime.FieldType.WILDCARD, cronTime.getHours().getType());
        assertEquals(CronTime.FieldType.WILDCARD, cronTime.getMonths().getType());
    }

    @Test
    public void testParseInterval() {
        String crontab = "*/5 * * * *";

        CronTime cronTime = CronTime.parse(crontab);

        assertEquals(CronTime.FieldType.INTERVAL, cronTime.getMinutes().getType());
        assertEquals(1, (int) cronTime.getMinutes().getValues().size());
        assertEquals(5, (int) cronTime.getMinutes().getValues().get(0));
    }

    @Test
    public void testParseMultipleTypes() {
        String crontab = "*/2 * 10,11,12 3-6 11";

        CronTime cronTime = CronTime.parse(crontab);

        assertEquals(CronTime.FieldType.INTERVAL, cronTime.getMinutes().getType());
        assertEquals(2, (int) cronTime.getMinutes().getValues().get(0));

        assertEquals(CronTime.FieldType.WILDCARD, cronTime.getHours().getType());

        assertEquals(CronTime.FieldType.LIST, cronTime.getDaysOfMonth().getType());
        assertEquals(3, (int) cronTime.getDaysOfMonth().getValues().size());
        assertEquals(10, (int) cronTime.getDaysOfMonth().getValues().get(0));
        assertEquals(11, (int) cronTime.getDaysOfMonth().getValues().get(1));
        assertEquals(12, (int) cronTime.getDaysOfMonth().getValues().get(2));

        assertEquals(CronTime.FieldType.RANGE, cronTime.getMonths().getType());
        assertEquals(2, (int) cronTime.getMonths().getValues().size());
        assertEquals(3, (int) cronTime.getMonths().getValues().get(0));
        assertEquals(6, (int) cronTime.getMonths().getValues().get(1));

        assertEquals(CronTime.FieldType.NUMBER, cronTime.getDaysOfWeek().getType());
        assertEquals(11, (int) cronTime.getDaysOfWeek().getValues().get(0));
    }
}
