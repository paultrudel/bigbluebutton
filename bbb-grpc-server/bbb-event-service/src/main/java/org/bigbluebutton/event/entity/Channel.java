package org.bigbluebutton.event.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class Channel {

    private Integer id;
    private String name;

    @Getter
    public enum Column {
        ID("id"),
        NAME("name");

        private String label;

        Column(String label) { this.label = label; }

    }

    private Set<Event> events;
}
