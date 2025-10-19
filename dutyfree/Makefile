# Makefile

.PHONY: help build start stop restart logs clean dev prod test

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build Docker images
	docker-compose build

start: ## Start all services
	docker-compose up -d

stop: ## Stop all services
	docker-compose down

restart: ## Restart all services
	docker-compose restart

logs: ## View logs
	docker-compose logs -f

logs-app: ## View application logs
	docker-compose logs -f app

clean: ## Remove containers and volumes
	docker-compose down -v
	docker system prune -f

dev: ## Start development environment
	docker-compose -f docker-compose.yml -f docker-compose.dev.yml up

dev-build: ## Build and start development environment
	docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build

prod: ## Start production environment
	docker-compose up -d

prod-build: ## Build and start production environment
	docker-compose up -d --build

test: ## Run tests
	mvn test

package: ## Package application
	mvn clean package -DskipTests

install: ## Install dependencies
	mvn clean install

db-migrate: ## Run database migrations
	mvn flyway:migrate

db-clean: ## Clean database
	mvn flyway:clean

db-info: ## Database migration info
	mvn flyway:info

shell-app: ## Shell into app container
	docker-compose exec app sh

shell-db: ## Shell into database container
	docker-compose exec postgres psql -U postgres -d dutyfree_db

backup-db: ## Backup database
	docker-compose exec postgres pg_dump -U postgres dutyfree_db > backup_$(shell date +%Y%m%d_%H%M%S).sql

restore-db: ## Restore database (usage: make restore-db FILE=backup.sql)
	docker-compose exec -T postgres psql -U postgres -d dutyfree_db < $(FILE)

status: ## Show services status
	docker-compose ps

stats: ## Show resource usage
	docker stats

health: ## Check application health
	curl -f http://localhost:8080/actuator/health || exit 1