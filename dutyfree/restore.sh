#!/bin/bash
# restore.sh - Script de restauration de sauvegarde

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
BACKUP_DIR="./backups/database"
LOG_FILE="./logs/restore_$(date +%Y%m%d_%H%M%S).log"

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

# Banner
echo -e "${GREEN}"
cat << "EOF"
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║          DUTY FREE - DATABASE RESTORE SCRIPT              ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

# Load environment
if [ -f .env ]; then
    set -a
    source .env
    set +a
else
    error ".env file not found"
fi

# Check if backup directory exists
if [ ! -d "$BACKUP_DIR" ]; then
    error "Backup directory not found: $BACKUP_DIR"
fi

# List available backups
echo ""
info "Available backups:"
echo ""
ls -lht "$BACKUP_DIR"/*.sql.gz | nl
echo ""

# Select backup
read -p "$(echo -e ${YELLOW}Enter backup number to restore [or path]:${NC} )" BACKUP_CHOICE

if [[ "$BACKUP_CHOICE" =~ ^[0-9]+$ ]]; then
    BACKUP_FILE=$(ls -t "$BACKUP_DIR"/*.sql.gz | sed -n "${BACKUP_CHOICE}p")
else
    BACKUP_FILE="$BACKUP_CHOICE"
fi

if [ ! -f "$BACKUP_FILE" ]; then
    error "Backup file not found: $BACKUP_FILE"
fi

info "Selected backup: $BACKUP_FILE"

# Confirm restoration
echo ""
warning "⚠️  WARNING: This will REPLACE the current database!"
warning "⚠️  All existing data will be LOST!"
echo ""
read -p "$(echo -e ${RED}Type 'YES' to confirm restoration:${NC} )" CONFIRM

if [ "$CONFIRM" != "YES" ]; then
    log "Restoration cancelled by user"
    exit 0
fi

# Create pre-restore backup
log "Creating pre-restore backup..."
PRE_RESTORE_BACKUP="$BACKUP_DIR/pre_restore_$(date +%Y%m%d_%H%M%S).sql"
docker-compose exec -T postgres pg_dump -U "$DB_USER" "$DB_NAME" > "$PRE_RESTORE_BACKUP"
gzip "$PRE_RESTORE_BACKUP"
log "✓ Pre-restore backup created: ${PRE_RESTORE_BACKUP}.gz"

# Stop application
log "Stopping application..."
docker-compose stop app
log "✓ Application stopped"

# Decompress backup if needed
TEMP_FILE="/tmp/restore_temp.sql"
if [[ "$BACKUP_FILE" == *.gz ]]; then
    log "Decompressing backup..."
    gunzip -c "$BACKUP_FILE" > "$TEMP_FILE"
else
    cp "$BACKUP_FILE" "$TEMP_FILE"
fi

# Drop and recreate database
log "Recreating database..."
docker-compose exec -T postgres psql -U "$DB_USER" -c "DROP DATABASE IF EXISTS $DB_NAME;"
docker-compose exec -T postgres psql -U "$DB_USER" -c "CREATE DATABASE $DB_NAME;"
log "✓ Database recreated"

# Restore database
log "Restoring database..."
docker-compose exec -T postgres psql -U "$DB_USER" -d "$DB_NAME" < "$TEMP_FILE"

if [ $? -eq 0 ]; then
    log "✓ Database restored successfully"
else
    error "Database restoration failed"
fi

# Clean up
rm -f "$TEMP_FILE"

# Start application
log "Starting application..."
docker-compose start app
log "✓ Application started"

# Wait for application
log "Waiting for application to be ready..."
sleep 10

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
    error "Application failed to start"
fi

# Summary
echo ""
echo -e "${GREEN}"
cat << "EOF"
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║          DATABASE RESTORED SUCCESSFULLY                   ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

log "Restoration completed successfully!"
log "Restored from: $BACKUP_FILE"
log "Pre-restore backup: ${PRE_RESTORE_BACKUP}.gz"

exit 0