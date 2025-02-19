package com.example.partystarter.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;

public enum EventFilter {
    ME("me");  // Future: Add ALL

    private final String value;

    EventFilter(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EventFilter fromString(String value) {
        if (value == null) {
            return null;
        }
        
        return Arrays.stream(EventFilter.values())
                .filter(filter -> filter.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown event filter: " + value));
    }

    @Component
    public static class StringToEventFilterConverter implements Converter<String, EventFilter> {
        @Override
        public EventFilter convert(String source) {
            return EventFilter.fromString(source);
        }
    }
}