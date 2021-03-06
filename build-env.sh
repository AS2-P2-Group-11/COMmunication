#!/bin/bash
docker build --tag p2_11_api:1.0 .
docker build --tag p2_11_agent:1.0 ./Agent/
docker-compose up -d
sleep 10
docker-compose restart api
sleep 10
docker-compose restart agent
python3 db_initialize.py
echo "Agent is ready to work"