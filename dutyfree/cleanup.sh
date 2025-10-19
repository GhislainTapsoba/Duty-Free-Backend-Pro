#!/bin/bash
# cleanup.sh - Script de nettoyage

set -e

echo "Duty Free - Cleanup Script"
echo "=========================="
echo ""

read -p "This will remove all containers, volumes, and images. Continue? [y/N]: " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Cleanup cancelled"
    exit 0
fi

echo "Stopping containers..."
docker-compose down -v

echo "Removing unused images..."
docker image prune -af

echo "Removing unused volumes..."
docker volume prune -f

echo "Removing unused networks..."
docker network prune -f

echo ""
echo "Cleanup completed!"