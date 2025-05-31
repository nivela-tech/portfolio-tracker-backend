#!/bin/bash

# Deploy to Railway script

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "Railway CLI is not installed. Please install it first: https://docs.railway.app/develop/cli"
    exit 1
fi

# Check if user is logged in to Railway
railway status
if [ $? -ne 0 ]; then
    echo "You're not logged in to Railway. Please run 'railway login' first."
    railway login
fi

# Set working directory
cd "$(dirname "$0")"

# Build the application
echo "Building the application with Gradle..."
./gradlew clean build -x test

# Check if build succeeded
if [ $? -ne 0 ]; then
    echo "Build failed. Please fix the errors before deploying."
    exit 1
fi

# Deploy to Railway
echo "Deploying to Railway..."
railway up

# Check if deployment succeeded
if [ $? -eq 0 ]; then
    echo "Deployment successful! Your application is now running on Railway."
    
    # Get the deployment URL
    echo "Getting application URL..."
    railway status
    
    echo "Don't forget to set the environment variables in the Railway dashboard:"
    echo "  - FRONTEND_URL: https://www.agnifolio.com"
    echo "  - GOOGLE_CLIENT_ID: Your Google OAuth2 client ID"
    echo "  - GOOGLE_CLIENT_SECRET: Your Google OAuth2 client secret"
    echo "  - SPRING_PROFILES_ACTIVE: prod"
else
    echo "Deployment failed. Please check the errors."
    exit 1
fi
