#!/bin/bash
# monitor.sh - Script de monitoring de l'application

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
CHECK_INTERVAL=60  # seconds
ALERT_EMAIL="admin@dutyfree.com"
LOG_FILE="./logs/monitor.log"

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

send_alert() {
    local MESSAGE=$1
    warning "ALERT: $MESSAGE"
    
    # Send email (requires mailutils to be installed)
    # echo "$MESSAGE" | mail -s "Duty Free Alert" "$ALERT_EMAIL"
    
    # Send to webhook (uncomment and configure)
    # curl -X POST https://your-webhook-url \
    #   -H 'Content-Type: application/json' \
    #   -d "{\"text\":\"🚨 $MESSAGE\"}"
}

check_application() {
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

check_database() {
    if docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

check_redis() {
    if docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

check_disk_space() {
    DISK_USAGE=$(df -h / | awk 'NR==2 {print $5}' | sed 's/%//')
    if [ "$DISK_USAGE" -gt 85 ]; then
        return 1
    else
        return 0
    fi
}

check_memory() {
    MEM_USAGE=$(free | grep Mem | awk '{print int($3/$2 * 100)}')
    if [ "$MEM_USAGE" -gt 90 ]; then
        return 1
    else
        return 0
    fi
}

# Banner
clear
echo -e "${BLUE}"
cat << "EOF"
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║          DUTY FREE - MONITORING DASHBOARD                 ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

info "Monitoring started. Press Ctrl+C to stop."
echo ""

# Monitoring loop
while true; do
    clear
    echo -e "${BLUE}=== Duty Free Monitoring Dashboard ===${NC}"
    echo "Last check: $(date +'%Y-%m-%d %H:%M:%S')"
    echo ""
    
    # Application Health
    echo -n "Application: "
    if check_application; then
        echo -e "${GREEN}✓ Healthy${NC}"
    else
        echo -e "${RED}✗ Down${NC}"
        send_alert "Application is down!"
    fi
    
    # Database Health
    echo -n "Database:    "
    if check_database; then
        echo -e "${GREEN}✓ Healthy${NC}"
    else
        echo -e "${RED}✗ Down${NC}"
        send_alert "Database is down!"
    fi
    
    # Redis Health
    echo -n "Redis:       "
    if check_redis; then
        echo -e "${GREEN}✓ Healthy${NC}"
    else
        echo -e "${RED}✗ Down${NC}"
        send_alert "Redis is down!"
    fi
    
    echo ""
    echo -e "${BLUE}=== System Resources ===${NC}"
    
    # Disk Space
    DISK_USAGE=$(df -h / | awk 'NR==2 {print $5}')
    echo -n "Disk Usage:  "
    if check_disk_space; then
        echo -e "${GREEN}$DISK_USAGE${NC}"
    else
        echo -e "${RED}$DISK_USAGE (Critical!)${NC}"
        send_alert "Disk space is critical: $DISK_USAGE"
    fi
    
    # Memory Usage
    MEM_USAGE=$(free -h | awk 'NR==2 {print $3 "/" $2}')
    MEM_PERCENT=$(free | grep Mem | awk '{print int($3/$2 * 100)}')
    echo -n "Memory:      "
    if check_memory; then
        echo -e "${GREEN}${MEM_USAGE} (${MEM_PERCENT}%)${NC}"
    else
        echo -e "${RED}${MEM_USAGE} (${MEM_PERCENT}% - Critical!)${NC}"
        send_alert "Memory usage is critical: ${MEM_PERCENT}%"
    fi
    
    # CPU Usage
    CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
    echo "CPU Usage:   ${CPU_USAGE}%"
    
    echo ""
    echo -e "${BLUE}=== Docker Containers ===${NC}"
    docker-compose ps
    
    echo ""
    echo -e "${BLUE}=== Recent Application Logs ===${NC}"
    docker-compose logs --tail=10 app
    
    echo ""
    echo "Next check in ${CHECK_INTERVAL} seconds..."
    
    sleep "$CHECK_INTERVAL"
done