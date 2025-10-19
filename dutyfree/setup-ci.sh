#!/bin/bash
# setup-ci.sh - Configure CI/CD

echo "CI/CD Setup Script"
echo "=================="
echo ""

# GitHub Actions
if [ -d .github ]; then
    echo "GitHub Actions detected"
    echo "Please configure the following secrets in GitHub:"
    echo "  - DOCKER_USERNAME"
    echo "  - DOCKER_PASSWORD"
    echo "  - SONAR_TOKEN"
    echo "  - SSH_PRIVATE_KEY_STAGING"
    echo "  - SSH_PRIVATE_KEY_PROD"
    echo "  - SLACK_WEBHOOK"
fi

# GitLab CI
if [ -f .gitlab-ci.yml ]; then
    echo "GitLab CI detected"
    echo "Please configure the following variables in GitLab:"
    echo "  - CI_REGISTRY_USER"
    echo "  - CI_REGISTRY_PASSWORD"
    echo "  - SONAR_TOKEN"
    echo "  - SSH_PRIVATE_KEY_STAGING"
    echo "  - SSH_PRIVATE_KEY_PROD"
fi

# Jenkins
if [ -f Jenkinsfile ]; then
    echo "Jenkins detected"
    echo "Please configure the following credentials in Jenkins:"
    echo "  - docker-credentials"
    echo "  - ssh-staging-credentials"
    echo "  - ssh-prod-credentials"
    echo "  - sonarqube-token"
fi

echo ""
echo "Setup instructions completed!"