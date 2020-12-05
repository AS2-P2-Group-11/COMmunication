#!/bin/sh

set -e


while !  wget 127.0.0.1:9000;
  do sleep 1
done
sleep 10

#exec $cmd