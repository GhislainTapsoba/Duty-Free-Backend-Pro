#!/bin/bash
# health-check.sh - Script de vérification de santé rapide

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "Duty Free - Health Check"
echo "========================"
echo ""

# Check Application
echo -n "Application Health: "
if curl -sf http://localhost:8080/actuator/health | grep -q "UP"; then
    echo -e "${GREEN}✓ Healthy${NC}"
else
    echo -e "${RED}✗ Unhealthy${NC}"
    exit 1
fi

# Check Database
echo -n "Database:           "
if docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Healthy${NC}"
else
    echo -e "${RED}✗ Unhealthy${NC}"
    exit 1
fi

# Check Redis
echo -n "Redis:              "
if docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Healthy${NC}"
else
    echo -e "${RED}✗ Unhealthy${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}All systems operational${NC}"
exit 0