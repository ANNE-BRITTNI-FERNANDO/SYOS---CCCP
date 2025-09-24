package com.syos.shared.valueobjects;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Timestamp value object that ensures consistent date/time handling.
 * 
 * This class follows the Value Object pattern and provides immutable
 * timestamp handling throughout the SYOS system with consistent formatting.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public final class Timestamp {
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final LocalDateTime value;
    
    /**
     * Creates a new Timestamp value object.
     * 
     * @param value the timestamp as LocalDateTime
     * @throws IllegalArgumentException if the timestamp is null
     */
    public Timestamp(LocalDateTime value) {
        this.value = Objects.requireNonNull(value, "Timestamp cannot be null");
    }
    
    /**
     * Creates a Timestamp representing the current moment.
     * 
     * @return new Timestamp instance with current time
     */
    public static Timestamp now() {
        return new Timestamp(LocalDateTime.now());
    }
    
    /**
     * Creates a Timestamp from a LocalDateTime.
     * 
     * @param dateTime the LocalDateTime
     * @return new Timestamp instance
     * @throws IllegalArgumentException if dateTime is null
     */
    public static Timestamp of(LocalDateTime dateTime) {
        return new Timestamp(dateTime);
    }
    
    /**
     * Creates a Timestamp from date/time components.
     * 
     * @param year the year
     * @param month the month (1-12)
     * @param day the day of month (1-31)
     * @param hour the hour (0-23)
     * @param minute the minute (0-59)
     * @param second the second (0-59)
     * @return new Timestamp instance
     */
    public static Timestamp of(int year, int month, int day, int hour, int minute, int second) {
        return new Timestamp(LocalDateTime.of(year, month, day, hour, minute, second));
    }
    
    /**
     * Parses a timestamp from a string using the default format (yyyy-MM-dd HH:mm:ss).
     * 
     * @param timestampString the timestamp string
     * @return new Timestamp instance
     * @throws IllegalArgumentException if the string is invalid or null
     */
    public static Timestamp parse(String timestampString) {
        if (timestampString == null) {
            throw new IllegalArgumentException("Timestamp string cannot be null");
        }
        
        try {
            LocalDateTime parsed = LocalDateTime.parse(timestampString.trim(), DEFAULT_FORMATTER);
            return new Timestamp(parsed);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timestamp format. Expected: yyyy-MM-dd HH:mm:ss", e);
        }
    }
    
    /**
     * Gets the timestamp value.
     * 
     * @return the LocalDateTime value
     */
    public LocalDateTime getValue() {
        return value;
    }
    
    /**
     * Checks if this timestamp is before another timestamp.
     * 
     * @param other the other timestamp to compare
     * @return true if this timestamp is before the other
     * @throws IllegalArgumentException if other is null
     */
    public boolean isBefore(Timestamp other) {
        Objects.requireNonNull(other, "Other timestamp cannot be null");
        return this.value.isBefore(other.value);
    }
    
    /**
     * Checks if this timestamp is after another timestamp.
     * 
     * @param other the other timestamp to compare
     * @return true if this timestamp is after the other
     * @throws IllegalArgumentException if other is null
     */
    public boolean isAfter(Timestamp other) {
        Objects.requireNonNull(other, "Other timestamp cannot be null");
        return this.value.isAfter(other.value);
    }
    
    /**
     * Checks if this timestamp is equal to another timestamp.
     * 
     * @param other the other timestamp to compare
     * @return true if this timestamp equals the other
     * @throws IllegalArgumentException if other is null
     */
    public boolean isEqual(Timestamp other) {
        Objects.requireNonNull(other, "Other timestamp cannot be null");
        return this.value.isEqual(other.value);
    }
    
    /**
     * Adds the specified number of days to this timestamp.
     * 
     * @param days the number of days to add
     * @return new Timestamp instance with added days
     */
    public Timestamp plusDays(long days) {
        return new Timestamp(this.value.plusDays(days));
    }
    
    /**
     * Adds the specified number of hours to this timestamp.
     * 
     * @param hours the number of hours to add
     * @return new Timestamp instance with added hours
     */
    public Timestamp plusHours(long hours) {
        return new Timestamp(this.value.plusHours(hours));
    }
    
    /**
     * Adds the specified number of minutes to this timestamp.
     * 
     * @param minutes the number of minutes to add
     * @return new Timestamp instance with added minutes
     */
    public Timestamp plusMinutes(long minutes) {
        return new Timestamp(this.value.plusMinutes(minutes));
    }
    
    /**
     * Subtracts the specified number of days from this timestamp.
     * 
     * @param days the number of days to subtract
     * @return new Timestamp instance with subtracted days
     */
    public Timestamp minusDays(long days) {
        return new Timestamp(this.value.minusDays(days));
    }
    
    /**
     * Subtracts the specified number of hours from this timestamp.
     * 
     * @param hours the number of hours to subtract
     * @return new Timestamp instance with subtracted hours
     */
    public Timestamp minusHours(long hours) {
        return new Timestamp(this.value.minusHours(hours));
    }
    
    /**
     * Subtracts the specified number of minutes from this timestamp.
     * 
     * @param minutes the number of minutes to subtract
     * @return new Timestamp instance with subtracted minutes
     */
    public Timestamp minusMinutes(long minutes) {
        return new Timestamp(this.value.minusMinutes(minutes));
    }
    
    /**
     * Formats this timestamp using the default format.
     * 
     * @return formatted timestamp string (yyyy-MM-dd HH:mm:ss)
     */
    public String format() {
        return value.format(DEFAULT_FORMATTER);
    }
    
    /**
     * Formats this timestamp using a custom formatter.
     * 
     * @param formatter the DateTimeFormatter to use
     * @return formatted timestamp string
     * @throws IllegalArgumentException if formatter is null
     */
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "Formatter cannot be null");
        return value.format(formatter);
    }
    
    /**
     * Gets the year component.
     * 
     * @return the year
     */
    public int getYear() {
        return value.getYear();
    }
    
    /**
     * Gets the month component.
     * 
     * @return the month (1-12)
     */
    public int getMonth() {
        return value.getMonthValue();
    }
    
    /**
     * Gets the day component.
     * 
     * @return the day of month (1-31)
     */
    public int getDay() {
        return value.getDayOfMonth();
    }
    
    /**
     * Gets the hour component.
     * 
     * @return the hour (0-23)
     */
    public int getHour() {
        return value.getHour();
    }
    
    /**
     * Gets the minute component.
     * 
     * @return the minute (0-59)
     */
    public int getMinute() {
        return value.getMinute();
    }
    
    /**
     * Gets the second component.
     * 
     * @return the second (0-59)
     */
    public int getSecond() {
        return value.getSecond();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timestamp timestamp = (Timestamp) o;
        return Objects.equals(value, timestamp.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return format();
    }
}