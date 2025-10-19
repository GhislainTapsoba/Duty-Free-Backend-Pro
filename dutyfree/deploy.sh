#!/bin/bash
# deploy.sh - Script de déploiement production

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="duty-free-backend"
DOCKER_COMPOSE_FILE="docker-compose.yml"
DOCKER_COMPOSE_PROD_FILE="docker-compose.prod.yml"
BACKUP_DIR="./backups"
LOG_FILE="./logs/deploy_$(date +%Y%m%d_%H%M%S).log"

# Functions
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
    exit 1
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1" | tee -a "$LOG_FILE"
}

# Create necessary directories
mkdir -p "$BACKUP_DIR"
mkdir -p "$(dirname "$LOG_FILE")"

# Banner
echo -e "${GREEN}"
cat << "EOF"
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║     DUTY FREE MANAGEMENT SYSTEM - DEPLOYMENT SCRIPT      ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    warning "Running as root. It's recommended to use a non-root user."
fi

# Check prerequisites
log "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    error "Docker is not installed. Please install Docker first."
fi

if ! command -v docker-compose &> /dev/null; then
    error "Docker Compose is not installed. Please install Docker Compose first."
fi

log "✓ Prerequisites check passed"

# Check .env file
if [ ! -f .env ]; then
    error ".env file not found. Please create it from .env.example"
fi

log "✓ Environment file found"

# Load environment variables
set -a
source .env
set +a

# Confirm deployment
echo ""
info "Deployment Configuration:"
info "  Application: $APP_NAME"
info "  Database: $DB_NAME"
info "  Environment: ${SPRING_PROFILES_ACTIVE:-prod}"
echo ""

read -p "$(echo -e ${YELLOW}Do you want to proceed with deployment? [y/N]:${NC} )" -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log "Deployment cancelled by user"
    exit 0
fi

# Backup database before deployment
log "Creating database backup..."
BACKUP_FILE="$BACKUP_DIR/db_backup_$(date +%Y%m%d_%H%M%S).sql"

if docker-compose ps postgres | grep -q "Up"; then
    docker-compose exec -T postgres pg_dump -U "$DB_USER" "$DB_NAME" > "$BACKUP_FILE" 2>&1
    if [ $? -eq 0 ]; then
        log "✓ Database backup created: $BACKUP_FILE"
        gzip "$BACKUP_FILE"
        log "✓ Backup compressed: ${BACKUP_FILE}.gz"
    else
        warning "Database backup failed, but continuing with deployment"
    fi
else
    info "Database container not running, skipping backup"
fi

# Pull latest code (if using git)
if [ -d .git ]; then
    log "Pulling latest code from repository..."
    git pull origin main || warning "Git pull failed"
    log "✓ Code updated"
fi

# Build new Docker images
log "Building Docker images..."
docker-compose -f "$DOCKER_COMPOSE_FILE" -f "$DOCKER_COMPOSE_PROD_FILE" build --no-cache
if [ $? -eq 0 ]; then
    log "✓ Docker images built successfully"
else
    error "Docker build failed"
fi

# Stop old containers
log "Stopping old containers..."
docker-compose -f "$DOCKER_COMPOSE_FILE" -f "$DOCKER_COMPOSE_PROD_FILE" down
log "✓ Old containers stopped"

# Start new containers
log "Starting new containers..."
docker-compose -f "$DOCKER_COMPOSE_FILE" -f "$DOCKER_COMPOSE_PROD_FILE" up -d
if [ $? -eq 0 ]; then
    log "✓ New containers started"
else
    error "Failed to start containers"
fi

# Wait for application to be ready
log "Waiting for application to be ready..."
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        log "✓ Application is healthy"
        break
    fi
    ATTEMPT=$((ATTEMPT+1))
    echo -n "."
    sleep 5
done

echo ""

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    error "Application failed to start within expected time"
fi

# Run database migrations
log "Running database migrations..."
docker-compose exec -T app mvn flyway:migrate || warning "Database migration failed"
log "✓ Database migrations completed"

# Check container status
log "Checking container status..."
docker-compose -f "$DOCKER_COMPOSE_FILE" -f "$DOCKER_COMPOSE_PROD_FILE" ps

# Clean up old images
log "Cleaning up old Docker images..."
docker image prune -f
log "✓ Cleanup completed"

# Display logs
echo ""
info "Recent application logs:"
docker-compose logs --tail=50 app

# Summary
echo ""
echo -e "${GREEN}"
cat << "EOF"
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║              DEPLOYMENT COMPLETED SUCCESSFULLY            ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

log "Deployment completed successfully!"
log "Application URL: http://localhost:8080"
log "Swagger UI: http://localhost:8080/swagger-ui.html"
log "Health Check: http://localhost:8080/actuator/health"
log "Log file: $LOG_FILE"

# Send notification (optional)
# curl -X POST https://your-webhook-url \
#   -H 'Content-Type: application/json' \
#   -d "{\"text\":\"Deployment completed successfully for $APP_NAME\"}"

exit 0