#!/bin/bash

function deploy() {
    echo "Cleaning old versions..."
    docker compose down
    
    echo "Deploying new version..."
    docker compose build --no-cache
    docker compose up -d
}

function remove() {
    echo "Removing all containers and networks..."
    docker compose down -v
}

case "$1" in
    deploy)
        deploy
        ;;
    remove)
        remove
        ;;
    *)
        echo "Usage: $0 {deploy|remove}"
        exit 1
esac
