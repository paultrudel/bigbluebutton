package org.bigbluebutton.event.repository;

import org.bigbluebutton.event.entity.Channel;
import org.bigbluebutton.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, OffsetDateTime> {

}
