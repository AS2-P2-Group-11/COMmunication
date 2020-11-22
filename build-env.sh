#!/bin/bash
docker build --tag p2_11_api:1.0 .
docker-compose up -d
sleep 5
docker-compose restart api
sleep 2
python3 db_initialize.py