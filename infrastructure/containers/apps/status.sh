#!/usr/bin/env bash

SERVICES=$(docker compose config --services)

for SERVICE in $SERVICES; do
    STATUS=$(docker compose ps --filter "status=running" --services | grep -w "$SERVICE")
    if [ -n "$STATUS" ]; then
        echo "$SERVICE up"
    else
        echo "$SERVICE down"
    fi
done
