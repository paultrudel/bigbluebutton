#!/bin/bash

export LANGUAGE="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"

echo "Restarting database bbb_event_service"
sudo -u postgres psql -c "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE datname = 'bbb_event_service'"
sudo -u postgres psql -c "drop database if exists bbb_event_service with (force)"
sudo -u postgres psql -c "create database bbb_event_service WITH TEMPLATE template0 LC_COLLATE 'C.UTF-8'"
sudo -u postgres psql -c "alter database bbb_event_service set timezone to 'UTC'"