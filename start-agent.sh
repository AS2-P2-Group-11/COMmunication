#!/bin/bash
docker-compose up -d
sleep 10
docker-compose restart api
sleep 10
docker-compose restart agent
echo "Agent is ready to work"