#!/bin/bash
# setup.sh - Script de configuration initiale

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}"
cat << "EOF"
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║          DUTY FREE - INITIAL SETUP SCRIPT                 ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

# Create directories
echo -e "${BLUE}Creating necessary directories...${NC}"
mkdir -p logs
mkdir -p backups/database
mkdir -p backups/files
mkdir -p uploads
mkdir -p ssl

# Create .env from example
if [ ! -f .env ]; then
    echo -e "${YELLOW}Creating .env file from .env.example...${NC}"
    cp .env.example .env
    echo -e "${GREEN}✓ .env file created${NC}"
    echo -e "${YELLOW}⚠  Please edit .env file with your configuration${NC}"
else
    echo -e "${GREEN}✓ .env file already exists${NC}"
fi

# Generate JWT secret
if ! grep -q "JWT_SECRET=" .env || grep -q "JWT_SECRET=your_very_long" .env; then
    echo -e "${BLUE}Generating JWT secret...${NC}"
    JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
    sed -i "s|JWT_SECRET=.*|JWT_SECRET=$JWT_SECRET|" .env
    echo -e "${GREEN}✓ JWT secret generated${NC}"
fi

# Make scripts executable
echo -e "${BLUE}Making scripts executable...${NC}"
chmod +x deploy.sh
chmod +x backup.sh
chmod +x restore.sh
chmod +x monitor.sh
chmod +x health-check.sh
echo -e "${GREEN}✓ Scripts are now executable${NC}"

# Setup git hooks (optional)
if [ -d .git ]; then
    echo -e "${BLUE}Setting up git hooks...${NC}"
    echo "#!/bin/bash" > .git/hooks/pre-commit
    echo "mvn clean test" >> .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    echo -e "${GREEN}✓ Git hooks configured${NC}"
fi

echo ""
echo -e "${GREEN}Setup completed successfully!${NC}"
echo ""
echo "Next steps:"
echo "1. Edit .env file with your configuration"
echo "2. Run: make dev (for development)"
echo "3. Run: make prod (for production)"
echo ""