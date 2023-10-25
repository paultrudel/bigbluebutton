package org.bigbluebutton.event.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.Instant;

@Entity
@Data
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private OffsetDateTime timestamp;

    private String type;

    private String data;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "channel_id", referencedColumnName = "id")
    private Channel channel;

    public long timestampToNanos() {
        Instant instant = timestamp.toInstant();
        long epochSeconds = instant.getEpochSecond();
        long nanoseconds = instant.getNano();
        return epochSeconds * 1_000_000_000L + nanoseconds;
    }
}
