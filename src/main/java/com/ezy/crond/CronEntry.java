package com.ezy.crond;

import java.util.Arrays;

public class CronEntry {
    private CronTime cronTime;
    private String command;

    protected CronEntry(CronTime cronTime, String command) {
        this.cronTime = cronTime;
        this.command = command;
    }

    public static CronEntry parse(String entry) {
        entry = entry.replaceAll("\\s+", " ");
        String parts[] = entry.split(" ");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Malformed crontab entry, must contain 5 time specifiers and a command");
        }
        String timeEntry = String.join(" ", Arrays.copyOfRange(parts, 0, 5));
        String command = String.join(" ", Arrays.copyOfRange(parts, 5, parts.length));
        return new CronEntry(CronTime.parse(timeEntry), command);
    }

    public CronTime getCronTime() { return cronTime; }
    public String getCommand() { return command; }
}
