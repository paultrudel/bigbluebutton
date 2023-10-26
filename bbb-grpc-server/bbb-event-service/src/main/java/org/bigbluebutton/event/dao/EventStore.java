package org.bigbluebutton.event.dao;

import org.bigbluebutton.event.entity.Channel;
import org.bigbluebutton.event.entity.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class EventStore {

    private final static Logger LOGGER = LoggerFactory.getLogger(EventStore.class);

    private final Connection connection;
    private final ChannelStore channelStore;


    public EventStore(Connection connection, ChannelStore channelStore) {
        this.connection = connection;
        this.channelStore = channelStore;
    }

    public void insertEvent(Event event) {
        try {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO event(timestamp, channel_id, type, data) VALUES(?, ?, ?, ?)""");
            statement.setObject(1, Event.millisToTimestamp(System.currentTimeMillis()));
            statement.setInt(2, event.getChannelId());
            statement.setString(3, event.getType());
            statement.setString(4, event.getData());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("An error occurred while inserting {}", event);
            LOGGER.error("{}", e.getMessage());
        }
    }

    public Optional<Event> findEventByTimestamp(long timestamp) {
        Optional<Event> result = Optional.empty();

        try {
            PreparedStatement statement = connection.prepareStatement("""
                    SELECT * FROM event WHERE timestamp = ?""");
            statement.setObject(1, Event.millisToTimestamp(timestamp));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Integer channelId = resultSet.getInt(Event.Column.CHANNEL_ID.getLabel());
                Optional<Channel> c = channelStore.findChannelById(channelId);
                Channel channel = c.orElse(null);
                Event event = Event.builder()
                        .timestamp(resultSet.getObject(Event.Column.TIMESTAMP.getLabel(), OffsetDateTime.class))
                        .type(resultSet.getString(Event.Column.TYPE.getLabel()))
                        .data(resultSet.getString(Event.Column.DATA.getLabel()))
                        .channelId(channelId)
                        .channel(channel)
                        .build();
                result = Optional.of(event);
            }
        } catch(Exception e) {
            LOGGER.error("Could not find event with timestamp {}", timestamp);
            LOGGER.error("{}", e.getMessage());
        }

        return result;
    }

    public List<Event> findEventsByType(String[] types) {
        List<Event> events = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement("""
                    SELECT * FROM event WHERE type = ANY (?) ORDER BY timestamp DESC""");
            Array typeArray = statement.getConnection().createArrayOf("TEXT", types);
            statement.setArray(1, typeArray);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Integer channelId = resultSet.getInt(Event.Column.CHANNEL_ID.getLabel());
                Optional<Channel> c = channelStore.findChannelById(channelId);
                Channel channel = c.orElse(null);
                Event event = Event.builder()
                        .timestamp(resultSet.getObject(Event.Column.TIMESTAMP.getLabel(), OffsetDateTime.class))
                        .type(resultSet.getString(Event.Column.TYPE.getLabel()))
                        .data(resultSet.getString(Event.Column.DATA.getLabel()))
                        .channelId(channelId)
                        .channel(channel)
                        .build();
                events.add(event);
            }
        } catch(Exception e) {
            LOGGER.error("Error occurred while tyring to find events with the provided type(s)");
            LOGGER.error("{}", e.getMessage());
        }

        return events;
    }

    public List<Event> findEventsByChannel(String[] channelNames) {
        List<Event> events = new ArrayList<>();
        Map<Integer, Channel> channels = findChannelsFromNames(channelNames);

        if (channels.isEmpty()) {
            LOGGER.info("No channels could be found with the provided name(s)");
            return events;
        }

        try {
            Object[] channelIds = channels.values().stream().map(Channel::getId).toArray();
            PreparedStatement statement = connection.prepareStatement("""
                    SELECT * FROM event WHERE channel_id = ANY (?) ORDER BY timestamp DESC""");
            Array channelIdArray = statement.getConnection().createArrayOf("SERIAL", channelIds);
            statement.setArray(1, channelIdArray);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Integer channelId = resultSet.getInt(Event.Column.CHANNEL_ID.getLabel());
                Event event = Event.builder()
                        .timestamp(resultSet.getObject(Event.Column.TIMESTAMP.getLabel(), OffsetDateTime.class))
                        .type(resultSet.getString(Event.Column.TYPE.getLabel()))
                        .data(resultSet.getString(Event.Column.DATA.getLabel()))
                        .channelId(channelId)
                        .channel(channels.get(channelId))
                        .build();
                events.add(event);
            }
        } catch(Exception e) {
            LOGGER.error("An error occurred while trying to get events from the provided channels");
            LOGGER.error("{}", e.getMessage());
        }

        return events;
    }

    public List<Event> findEventsByTypeAndChannel(String[] types, String[] channelNames) {
        List<Event> events = new ArrayList<>();
        Map<Integer, Channel> channels = findChannelsFromNames(channelNames);

        try {
            PreparedStatement statement;

            if (channels.isEmpty()) {
                statement = connection.prepareStatement("""
                    SELECT * FROM event WHERE type = ANY (?) ORDER BY timestamp DESC""");
                Array typeArray = statement.getConnection().createArrayOf("TEXT", types);
                statement.setArray(1, typeArray);
            } else {
                statement = connection.prepareStatement("""
                        SELECT * FROM event WHERE channel_id = ANY (?) AND type = ANY (?) ORDER BY timestamp DESC""");
                Object[] channelIds = channels.values().stream().map(Channel::getId).toArray();
                Array channelIdArray = statement.getConnection().createArrayOf("SERIAL", channelIds);
                statement.setArray(1, channelIdArray);
                Array typeArray = statement.getConnection().createArrayOf("TEXT", types);
                statement.setArray(2, typeArray);
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Integer channelId = resultSet.getInt(Event.Column.CHANNEL_ID.getLabel());
                Event event = Event.builder()
                        .timestamp(resultSet.getObject(Event.Column.TIMESTAMP.getLabel(), OffsetDateTime.class))
                        .type(resultSet.getString(Event.Column.TYPE.getLabel()))
                        .data(resultSet.getString(Event.Column.DATA.getLabel()))
                        .channelId(channelId)
                        .channel(channels.get(channelId))
                        .build();
                events.add(event);
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred while searching events by channel and type");
            LOGGER.error("{}", e.getMessage());
        }

        return events;
    }

    public List<Event> findEventsBetweenTimestamps(long start, long end) {
        List<Event> events = new ArrayList<>();

        try {
            OffsetDateTime startTimestamp = Event.millisToTimestamp(start);
            long endMillis = (end != 0) ? end : System.currentTimeMillis();
            OffsetDateTime endTimestamp = Event.millisToTimestamp(endMillis);

            PreparedStatement statement = connection.prepareStatement("""
                    SELECT * FROM event WHERE timestamp >= ? AND timestamp < ? ORDER BY timestamp DESC""");
            statement.setObject(1, startTimestamp);
            statement.setObject(2, endTimestamp);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Integer channelId = resultSet.getInt(Event.Column.CHANNEL_ID.getLabel());
                Optional<Channel> c = channelStore.findChannelById(channelId);
                Channel channel = c.orElse(null);
                Event event = Event.builder()
                        .timestamp(resultSet.getObject(Event.Column.TIMESTAMP.getLabel(), OffsetDateTime.class))
                        .type(resultSet.getString(Event.Column.TYPE.getLabel()))
                        .data(resultSet.getString(Event.Column.DATA.getLabel()))
                        .channelId(channelId)
                        .channel(channel)
                        .build();
                events.add(event);
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred while searching for events between {} and {}", start, end);
            LOGGER.error("{}", e.getMessage());
        }

        return events;
    }

    private Map<Integer, Channel> findChannelsFromNames(String[] channelNames) {
        Map<Integer, Channel> channels = new HashMap<>();

        for (String channelName: channelNames) {
            try {
                Optional<Channel> result = channelStore.findChannelByName(channelName);
                Channel channel = result.orElse(null);

                if(Objects.isNull(channel)) {
                    LOGGER.info("No channel with the name {} could be found", channelName);
                } else {
                    channels.put(channel.getId(), channel);
                }
            } catch (Exception e) {
                LOGGER.error("An error occurred while searching for channel with name {}", channelName);
                LOGGER.error("{}", e.getMessage());
            }
        }

        return channels;
    }
}
