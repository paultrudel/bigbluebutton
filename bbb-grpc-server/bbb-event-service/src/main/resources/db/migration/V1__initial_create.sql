CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS channel (
    id smallserial PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS event (
    timestamp TIMESTAMPTZ NOT NULL,
    channel_id smallserial NOT NULL,
    type text NOT NULL,
    data text NOT NULL,
    CONSTRAINT fk_event_channel FOREIGN KEY(channel_id) REFERENCES channel(id)
);

SELECT create_hypertable('event', 'timestamp');
SELECT add_retention_policy('event', INTERVAL '30 minutes');

INSERT INTO channel(name) VALUES('analytics-channel');
INSERT INTO channel(name) VALUES('MeetingManagerChannel');
INSERT INTO channel(name) VALUES('OutgoingMessageChannel');
INSERT INTO channel(name) VALUES('IncomingJsonMsgChannel');
INSERT INTO channel(name) VALUES('OutBbbMsgChannel');
INSERT INTO channel(name) VALUES('RecordServiceMessageChannel');
INSERT INTO channel(name) VALUES('to-html5-redis-channel');
INSERT INTO channel(name) VALUES('from-akka-apps-channel');
INSERT INTO channel(name) VALUES('to-akka-apps-channel');
INSERT INTO channel(name) VALUES('from-client-channel');
INSERT INTO channel(name) VALUES('to-client-channel');
INSERT INTO channel(name) VALUES('to-akka-apps-json-channel');
INSERT INTO channel(name) VALUES('to-akka-apps-redis-channel');
INSERT INTO channel(name) VALUES('from-akka-apps-chat-redis-channel');
INSERT INTO channel(name) VALUES('from-akka-apps-pres-redis-channel');
INSERT INTO channel(name) VALUES('from-bbb-web-redis-channel');
INSERT INTO channel(name) VALUES('from-akka-apps-redis-channel')