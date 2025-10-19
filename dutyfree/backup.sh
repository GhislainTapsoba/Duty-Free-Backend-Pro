#!/bin/bash
# backup.sh - Script de sauvegarde automatique

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Configuration
BACKUP_DIR="./backups"
DB_BACKUP_DIR="$BACKUP_DIR/database"
FILES_BACKUP_DIR="$BACKUP_DIR/files"
RETENTION_DAYS=30
LOG_FILE="./logs/backup_$(date +%Y%m%d_%H%M%S).log"

# Load environment
if [ -f .env ]; then
    set -a
    source .env
    set +a
else
    echo -e "${RED}Error: .env file not found${NC}"
    exit 1
fi

# Create directories
mkdir -p "$DB_BACKUP_DIR"
mkdir -p "$FILES_BACKUP_DIR"
mkdir -p "$(dirname "$LOG_FILE")"

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
    exit 1
}

# Banner
echo -e "${GREEN}"
cat << "EOF"
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║          DUTY FREE - AUTOMATED BACKUP SCRIPT              ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

log "Starting backup process..."

# Database backup
log "Backing up database..."
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DB_BACKUP_FILE="$DB_BACKUP_DIR/dutyfree_db_${TIMESTAMP}.sql"

if docker-compose ps postgres | grep -q "Up"; then
    docker-compose exec -T postgres pg_dump -U "$DB_USER" "$DB_NAME" > "$DB_BACKUP_FILE"
    
    if [ $? -eq 0 ]; then
        log "✓ Database backup created: $DB_BACKUP_FILE"
        
        # Compress backup
        gzip "$DB_BACKUP_FILE"
        log "✓ Database backup compressed: ${DB_BACKUP_FILE}.gz"
        
        # Calculate size
        BACKUP_SIZE=$(du -h "${DB_BACKUP_FILE}.gz" | cut -f1)
        log "  Backup size: $BACKUP_SIZE"
    else
        error "Database backup failed"
    fi
else
    error "Database container is not running"
fi

# Files backup (uploads, logs)
log "Backing up application files..."
FILES_BACKUP_FILE="$FILES_BACKUP_DIR/dutyfree_files_${TIMESTAMP}.tar.gz"

tar -czf "$FILES_BACKUP_FILE" \
    --exclude='*/target/*' \
    --exclude='*/node_modules/*' \
    --exclude='*/.git/*' \
    ./uploads ./logs 2>/dev/null || true

if [ $? -eq 0 ]; then
    log "✓ Files backup created: $FILES_BACKUP_FILE"
    FILES_SIZE=$(du -h "$FILES_BACKUP_FILE" | cut -f1)
    log "  Files backup size: $FILES_SIZE"
else
    log "⚠ Files backup completed with warnings"
fi

# Clean old backups
log "Cleaning old backups (older than $RETENTION_DAYS days)..."
find "$BACKUP_DIR" -name "*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete
find "$BACKUP_DIR" -name "*.tar.gz" -type f -mtime +$RETENTION_DAYS -delete
log "✓ Old backups cleaned"

# Backup to remote storage (optional - uncomment and configure)
# if [ ! -z "$REMOTE_BACKUP_HOST" ]; then
#     log "Uploading backup to remote storage..."
#     rsync -avz --progress "$DB_BACKUP_FILE.gz" \
#         "$REMOTE_BACKUP_USER@$REMOTE_BACKUP_HOST:$REMOTE_BACKUP_PATH/"
#     log "✓ Backup uploaded to remote storage"
# fi

# List recent backups
log "Recent backups:"
ls -lh "$DB_BACKUP_DIR" | tail -5
echo ""
ls -lh "$FILES_BACKUP_DIR" | tail -5

# Summary
TOTAL_BACKUPS=$(find "$BACKUP_DIR" -name "*.gz" -type f | wc -l)
TOTAL_SIZE=$(du -sh "$BACKUP_DIR" | cut -f1)

echo ""
log "Backup Summary:"
log "  Total backups: $TOTAL_BACKUPS"
log "  Total size: $TOTAL_SIZE"
log "  Location: $BACKUP_DIR"

echo -e "${GREEN}"
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║          BACKUP COMPLETED SUCCESSFULLY                    ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

exit 0