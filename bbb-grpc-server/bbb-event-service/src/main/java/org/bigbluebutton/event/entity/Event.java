package org.bigbluebutton.event.entity;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Builder
@Getter
public class Event {

    private OffsetDateTime timestamp;

    private String type;

    private String data;

    private Integer channelId;

    private Channel channel;

    @Getter
    public enum Column {
        TIMESTAMP("timestamp"),
        TYPE("type"),
        DATA("data"),
        CHANNEL_ID("channel_id");

        private String label;

        Column(String label) { this.label = label; }
    }

    public long timestampToMills() {
        Instant instant = timestamp.toInstant();
        return instant.toEpochMilli();
    }

    public static OffsetDateTime millisToTimestamp(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
