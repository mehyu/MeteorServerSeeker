package de.damcraft.serverseeker.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class TimeUtil {
    public static long toEpochSeconds(String iso8601) {
        if (iso8601 == null || iso8601.isEmpty()) return 0;
        try {
            return OffsetDateTime.parse(iso8601).toEpochSecond();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(iso8601).toEpochSecond(ZoneOffset.UTC);
            } catch (Exception e2) {
                return 0;
            }
        }
    }

    public static String format(String iso8601) {
        if (iso8601 == null || iso8601.isEmpty()) return "Unknown";
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .format(Instant.ofEpochSecond(toEpochSeconds(iso8601)).atZone(ZoneId.systemDefault()));
    }
}
