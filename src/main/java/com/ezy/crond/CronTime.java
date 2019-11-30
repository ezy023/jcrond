package com.ezy.crond;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CronTime { // CronTime since it's not an actual entry, no command to execute
    private final Field minutes;
    private final Field hours;
    private final Field daysOfMonth;
    private final Field months;
    private final Field daysOfWeek;
    /* daysOfMonth and daysOfWeek are treated as an OR clause */

    enum FieldType {
        NUMBER,    /* normal single digit */
        WILDCARD,  /* '*' */
        LIST,      /* list of comma-separated digits '1,2,3,4' */
        RANGE,     /* range of numbers from low to high '2-6' */
        INTERVAL,  /* interval specifying execution every nth time '*\/10' */
        ERROR      /* invalid specification, numbers outside of bounds of field */
    }

    static class Field {
        private FieldType type;
        private List<Integer> values = new ArrayList<>(2);

        public Field(FieldType type) {
            this.type = type;
        }
        public FieldType getType() { return type; }
        public List<Integer> getValues() { return values; }
        public void addValue(int val) {
            values.add(val);
        }

        @Override
        public String toString() {
            return "type: " + type.name() +
                " values: " + values;
        }
    }

    CronTime(Field minutes,
              Field hours,
              Field daysOfMonth,
              Field months,
              Field daysOfWeek) {
        this.minutes = minutes;
        this.hours = hours;
        this.daysOfMonth = daysOfMonth;
        this.months = months;
        this.daysOfWeek = daysOfWeek;
    }


    public static CronTime parse(String crontab) {
        String[] parts = crontab.split(" ");
        if (parts.length < 5 || parts.length > 5) {
            throw new IllegalArgumentException("Cron time entry specification must contain only 5 parts: " +
                                               "'minute hour day_of_month month day_of_week'");
        }

        Field[] fields = new Field[5];
        for (int i = 0; i < parts.length; i++) {
            fields[i] = parseField(parts[i]);
        }
        return new CronTime(fields[0], fields[1], fields[2], fields[3], fields[4]);
    }

    static Field parseField(String field) {
        if (field.equalsIgnoreCase("*")) {
            return new Field(FieldType.WILDCARD);
        }
        if (field.contains("*/")) {
            String[] parts = field.split("\\*/"); /* Have to escape the '*' because it has meaning in regex  */
            /* The split results in an empty string as the first element in the 'parts' array */
            if (parts.length != 2) {
                System.out.println("INTERVAL is invalid");
                // IllegalArgument
            }
            Field f = new Field(FieldType.INTERVAL);
            f.addValue(Integer.valueOf(parts[1]));
            return f;
        }
        if (field.contains("-")) {
            String[] parts = field.split("-");
            if (parts.length != 2) {
                System.out.println("RANGE is invalid");
                // IllegalArgument
            }
            Field f = new Field(FieldType.RANGE);
            f.addValue(Integer.valueOf(parts[0]));  /* Beginning of range */
            f.addValue(Integer.valueOf(parts[1]));  /* End of range */
            return f;
        }
        if (field.contains(",")) {
            String[] parts = field.split(",");
            Field f = new Field(FieldType.LIST);
            for(int i = 0; i < parts.length; i++) {
                f.addValue(Integer.valueOf(parts[i]));
            }
            return f;
        }

        Field f = new Field(FieldType.NUMBER);
        f.addValue(Integer.valueOf(field));
        return f;
    }


    public Field getMinutes() { return minutes; }
    public Field getHours() { return hours; }
    public Field getDaysOfMonth() { return daysOfMonth; }
    public Field getMonths() { return months; }
    public Field getDaysOfWeek() { return daysOfWeek; }
}
