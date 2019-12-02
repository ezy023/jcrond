package com.ezy.crond;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;

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

    @Test
    public void testNextExecutionHourly() {
        String crontab = "5 * * * *";
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 1),
                                                   LocalTime.MIDNIGHT,
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 1),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);

        assertEquals(expectedNextExecution, nextExecution);
        assertEquals(0, nextExecution.getHour());
        assertEquals(5, nextExecution.getMinute());
    }

    @Test
    public void testNextExecutionHourlyNextDay() {
        String crontab = "5 * * * *";
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 1),
                                                   LocalTime.of(23, 6),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 2),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);

        assertEquals(expectedNextExecution, nextExecution);
        assertEquals(2, nextExecution.getDayOfMonth());
        assertEquals(0, nextExecution.getHour());
        assertEquals(5, nextExecution.getMinute());
    }

    @Test
    public void testNextExecutionNextMonthNextYear() {
        String crontab = "5 * * * *";
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 31),
                                                   LocalTime.of(23, 6),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2020, 1, 1),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);

        assertEquals(expectedNextExecution, nextExecution);
        assertEquals(2020, nextExecution.getYear());
        assertEquals(1, nextExecution.getMonthValue());
        assertEquals(1, nextExecution.getDayOfMonth());
        assertEquals(0, nextExecution.getHour());
        assertEquals(5, nextExecution.getMinute());
    }

    @Test
    public void testNextExcutionWithRangeValue() {
        String crontab = "5 2-6 * * *";
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 1),
                                                   LocalTime.of(3, 6),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 1),
                                                               LocalTime.of(4, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);

        assertEquals(expectedNextExecution, nextExecution);
    }

    @Test
    public void testNextExcutionWithListValue() {
        String crontab = "5 * 1,3,5,8 * *";
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 1),
                                                   LocalTime.of(23, 6),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 3),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);

        assertEquals(expectedNextExecution, nextExecution);
    }

    @Test
    public void testNextExcutionWithIntervalValue() {
        String crontab = "5 */5 * * *";
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 1),
                                                   LocalTime.of(5, 6),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 1),
                                                               LocalTime.of(10, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);

        assertEquals(expectedNextExecution, nextExecution);
    }

    @Test
    public void testNextExcutionWithIntervalValueWithOverflow() {
        String crontab = "5 */5 * * *"; // Execute at 5 after the hour every 5th hour
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 1),
                                                   LocalTime.of(23, 5),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 2),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);

        assertEquals(expectedNextExecution, nextExecution);
    }

    @Test
    public void testNextExcutionWithIntervalValueWithOverflowWithNonZeroMinValue() {
        String crontab = "5 0 * */2 *"; // Every day at 0:05 of even months
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 31),
                                                   LocalTime.of(23, 5),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2020, 2, 1),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);

        assertEquals(expectedNextExecution, nextExecution);
    }
    // TODO test next day with intersection of day of month and day of week
    // need to test when both overflow, when either overflow and when neither overflow
    @Test
    public void testNextExecutionDayOfWeekBeforeNextDayOfMonth() {
        String crontab = "5 0 6 * 3"; // Execute on the 6th of every month and every Wednesday
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 1), // Sunday
                                                   LocalTime.of(0, 5),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 4),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);

        assertEquals(expectedNextExecution, nextExecution);
    }

    @Test
    public void testNextExecutionDayOfMonthOverflowDayOfWeekNoOverflow() {
        String crontab = "5 0 30 * 4"; // Execute on the 30th and every Thursday
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 31), // Tuesday
                                                   LocalTime.of(0, 5),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2020, 1, 2),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);
        assertEquals(expectedNextExecution, nextExecution);
    }

    @Test
    public void testNextExecutionDayOfWeekOverflowDayOfMonthNoOverflow() {
        String crontab = "5 0 * * 4"; // Execute every Thursday
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 6), // Friday
                                                   LocalTime.of(0, 5),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 12),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);
        assertEquals(expectedNextExecution, nextExecution);
    }

    @Test
    public void testNextExecutionDayOfWeekOverflowsMonth() {
        String crontab = "5 0 8 * 1"; // Execute every Monday and the 8th of the month
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 31), // Tuesday
                                                   LocalTime.of(0, 5),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2020, 1, 6),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);
        assertEquals(expectedNextExecution, nextExecution);
    }

    @Test
    public void testNextExecutionDayOfWeekOverflowsMonthButNotDayOfMonth() {
        String crontab = "5 0 8,31 * 7"; // Execute every Sunday and the 8th and 31st of the month
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 30), // Monday
                                                   LocalTime.of(0, 5),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 31),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);
        assertEquals(expectedNextExecution, nextExecution);
    }

    @Test
    public void testDaysOfMonthOverflowButNotDaysOfWeek() {
        String crontab = "5 0 2,10,20 * 2"; // Execute every Tuesday and the 2nd, 10th, and 20th of the month
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 30), // Monday
                                                   LocalTime.of(0, 5),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2019, 12, 31),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);
        assertEquals(expectedNextExecution, nextExecution);
    }

    @Test
    public void testNextExecutionMonthsOverflow() {
        String crontab = "5 0 * * *";
        CronTime cronTime = CronTime.parse(crontab);
        ZonedDateTime fixedTime = ZonedDateTime.of(LocalDate.of(2019, 12, 31),
                                                   LocalTime.of(0, 5),
                                                   ZoneOffset.UTC);
        ZonedDateTime expectedNextExecution = ZonedDateTime.of(LocalDate.of(2020, 1, 1),
                                                               LocalTime.of(0, 5),
                                                               ZoneOffset.UTC);
        ZonedDateTime nextExecution = cronTime.nextExecution(fixedTime);
        assertEquals(expectedNextExecution, nextExecution);
    }
}
