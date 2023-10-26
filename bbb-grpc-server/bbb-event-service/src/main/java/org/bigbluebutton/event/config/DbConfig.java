package org.bigbluebutton.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
@ComponentScan("org.bigbluebutton.event")
@PropertySource("classpath:application.yaml")
public class DbConfig {

    @Bean
    Connection connection(DbConfigProperties properties) throws SQLException {
        return DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword());
    }

}
