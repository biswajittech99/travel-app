#!/bin/bash

export MYSQL_URL="$(grep '^MYSQL_URL=' .env | cut -d '=' -f2-)"
export MYSQL_USERNAME="$(grep '^MYSQL_USERNAME=' .env | cut -d '=' -f2-)"
export MYSQL_PASSWORD="$(grep '^MYSQL_PASSWORD=' .env | cut -d '=' -f2-)"

export AVIATIONSTACK_BASE_URL="$(grep '^AVIATIONSTACK_BASE_URL=' .env | cut -d '=' -f2-)"
export AVIATIONSTACK_API_KEY="$(grep '^AVIATIONSTACK_API_KEY=' .env | cut -d '=' -f2-)"

export SERPAPI_BASE_URL="$(grep '^SERPAPI_BASE_URL=' .env | cut -d '=' -f2-)"
export SERPAPI_API_KEY="$(grep '^SERPAPI_API_KEY=' .env | cut -d '=' -f2-)"

echo "Travel-app environment loaded."
