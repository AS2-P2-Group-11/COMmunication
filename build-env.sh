#!/bin/bash
docker build --tag p2_11_api:1.0 .
docker build --tag p2_11_agent:1.0 ./Agent/
docker-compose up -d
sleep 10
python3 db_initialize.py