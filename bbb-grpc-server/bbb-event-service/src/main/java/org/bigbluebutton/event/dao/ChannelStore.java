package org.bigbluebutton.event.dao;

import org.bigbluebutton.event.entity.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

@Service
public class ChannelStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelStore.class);

    private final Connection connection;

    public ChannelStore(Connection connection) {
        this.connection = connection;
    }

    public Optional<Channel> findChannelById(Integer id) {
        Optional<Channel> result = Optional.empty();

        try {
            PreparedStatement statement = connection.prepareStatement("""
                    SELECT * FROM channel WHERE id = ?""");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Channel channel = Channel.builder()
                        .id(resultSet.getInt(Channel.Column.ID.getLabel()))
                        .name(resultSet.getString(Channel.Column.NAME.getLabel()))
                        .build();
                result = Optional.of(channel);
            }
        } catch (Exception e) {
            LOGGER.error("Could not get channel with ID {}", id);
            LOGGER.error("{}", e.toString());
        }

        return result;
    }

    public Optional<Channel> findChannelByName(String name) {
        Optional<Channel> result = Optional.empty();

        try {
            PreparedStatement statement = connection.prepareStatement("""
                    SELECT * FROM channel WHERE name = ?""");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Channel channel = Channel.builder()
                        .id(resultSet.getInt(Channel.Column.ID.getLabel()))
                        .name(resultSet.getString(Channel.Column.NAME.getLabel()))
                        .build();
                result = Optional.of(channel);
            }
        } catch (Exception e) {
            LOGGER.error("Could not get channel with name {}", name);
            LOGGER.error("{}", e.toString());
        }

        return result;
    }
}
