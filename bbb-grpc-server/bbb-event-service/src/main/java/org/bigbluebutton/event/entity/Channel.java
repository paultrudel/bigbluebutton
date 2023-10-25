package org.bigbluebutton.event.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Event> event;
}
