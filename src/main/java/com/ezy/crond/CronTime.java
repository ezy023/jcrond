package com.ezy.crond;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
        boolean overflow;
        int max, min;
        final FieldType type;
        List<Integer> values = new ArrayList<>(2);

        public Field(FieldType type) {
            this.type = type;
        }

        public Field(FieldType type, int max, int min) {
            this.type = type;
            this.max = max;
            this.min = min;
        }

        public FieldType getType() { return type; }
        public List<Integer> getValues() { return values; }
        public void addValue(int val) {
            values.add(val);
        }

        public int getMax() { return max; }
        public int getMin() { return min; }

        public void setOverflow(boolean overflow) { this.overflow = overflow; }
        public boolean getOverflow() { return overflow; }

        // TODO Unit test this method separately from nextExecution
        public int getNext(int current, int min, int max) {
            /* Here we are already being given an int current that has been adjusted for overflow */
            /* also need to do max checking for current */
            switch(type) {
            case WILDCARD:
                if (current > max) {
                    setOverflow(true);
                    return min;
                }
                return current;
            case NUMBER:
                if (current > values.get(0)) {
                    setOverflow(true);
                }
                return values.get(0);
            case RANGE:
                if (current >= values.get(0) && current <= values.get(1)) {
                    return current;
                }
                setOverflow(true);
                return values.get(0);
            case LIST:
                if (values.contains(current)) {
                    return current;
                }
                for(Integer val : values) {
                    if (current < val) {
                        return val;
                    }
                }
                /* No values qualified which means the current is beyond the range of the list, have to overflow */
                setOverflow(true);
                return values.get(0);
            case INTERVAL:
                if (current % values.get(0) == 0) {
                    return current;
                }
                while(current++ <= max) {
                    if (current % values.get(0) == 0) {
                        return current;
                    }
                }
                setOverflow(true);
                current = min;
                while(current <= max) {
                    if (current % values.get(0) == 0) {
                        return current;
                    }
                    current++;
                }
            default:
                break;
            }
            // TODO Something wrong here?
            return values.get(0);
        }

        @Override
        public String toString() {
            return "type: " + type.name() +
                " values: " + values;
        }
    }

    static class MinuteField extends Field {
        MinuteField(FieldType type, int max, int min) { super(type, max, min); }

        static MinuteField fromField(Field field) {
            return new MinuteField(field.getType(), 0, 59);
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

    ZonedDateTime nextMinute(ZonedDateTime current) {
        /* Always add one to the minute to avoid a job executing multiple times in one minute */
        int next = getMinutes().getNext(current.getMinute() + 1, 0, 59);
        return current.withMinute(next);
    }

    ZonedDateTime nextHour(ZonedDateTime current) {
        int nextBase = current.getHour();
        if (getMinutes().getOverflow()) {
            nextBase += 1;
        }
        int next = getHours().getNext(nextBase, 0, 23);
        return current.withHour(next);
    }

    /* TODO add check and logic if one of the day specifications is a wildcard */
    /* This will return the next day in terms of day of the month */
    ZonedDateTime nextDay(ZonedDateTime current) {
        int nextBaseDOW = current.getDayOfWeek().getValue();
        int nextBaseDOM = current.getDayOfMonth();
        if (getHours().getOverflow()) {
            nextBaseDOW += 1;
            nextBaseDOM += 1;
        }
        int nextDOW = getDaysOfWeek().getNext(nextBaseDOW, 1, 7);
        int dowDiff = dayOfWeekDiff(nextBaseDOW, nextDOW);
        /* still need to handle if this overflows */
        int nextDOWinDOM = nextBaseDOM + dowDiff;

        int nextDOM = getDaysOfMonth().getNext(nextBaseDOM, 1, current.getMonth().length(isLeapYear(current.getYear())));

        /* Logic for if one of the day fields is a wildcard, restrict to the specified field */
        int next = Math.min(nextDOWinDOM, nextDOM);
        if (getDaysOfWeek().getType() == FieldType.WILDCARD && getDaysOfMonth().getType() == FieldType.WILDCARD) {
            next = Math.min(nextDOWinDOM, nextDOM);
        } else if (getDaysOfWeek().getType() == FieldType.WILDCARD) {
            next = nextDOM;
        } else if (getDaysOfMonth().getType() == FieldType.WILDCARD) {
            next = nextDOWinDOM;
        }

        System.out.println(String.format("Next DOM: %d. Next DOW: %d", nextDOM, nextDOW));
        System.out.println(String.format("nextDOWinDOM: %d. nextDOM: %d", nextDOWinDOM, nextDOM));
        return current.withDayOfMonth(next);
    }

    int dayOfWeekDiff(int current, int next) {
        if (current < next) {
            return next - current;
        } else {
            int toAdd = next == 7 ? 0 : next;
            return 7 - current + toAdd;
        }
    }


    ZonedDateTime nextMonth(ZonedDateTime current) {
        int nextBase = current.getMonthValue();
        if (getDaysOfMonth().getOverflow()) {
            nextBase += 1;
        }
        int next = getMonths().getNext(nextBase, 1, 12);

        return current.withMonth(next);
    }

    ZonedDateTime nextYear(ZonedDateTime current) {
        int next = getMonths().getOverflow() ? current.getYear() + 1 : current.getYear();
        return current.withYear(next);
    }

    boolean isLeapYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
    }

    /* need get next unit methods
       in the get next unit, take a zdt, check if there is overflow from the unit beloww
       if there is, add on to the unit from the zdt and pass it to the get next int method
       if there is no overflow the just pass the unit to the get next int.
       For minutes always add one to the current value
    */

    public ZonedDateTime nextExecution(ZonedDateTime currentTime) {
        // Reset all overflow switches
        ZonedDateTime next = nextMinute(currentTime);
        next = nextHour(next);
        next = nextDay(next);
        next = nextMonth(next);
        next = nextYear(next);
        return next;
    }
}
