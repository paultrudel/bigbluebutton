package org.bigbluebutton.event.repository;

import org.bigbluebutton.event.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Integer> {
}
