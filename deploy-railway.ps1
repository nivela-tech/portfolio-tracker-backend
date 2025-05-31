# Deploy to Railway script

# Check if Railway CLI is installed
if (!(Get-Command railway -ErrorAction SilentlyContinue)) {
    Write-Host "Railway CLI is not installed. Please install it first: https://docs.railway.app/develop/cli" -ForegroundColor Red
    exit 1
}

# Check if user is logged in to Railway
$railwayStatus = railway status
if ($LASTEXITCODE -ne 0) {
    Write-Host "You're not logged in to Railway. Please run 'railway login' first." -ForegroundColor Yellow
    railway login
}

# Set working directory
Set-Location -Path $PSScriptRoot

# Build the application
Write-Host "Building the application with Gradle..." -ForegroundColor Cyan
./gradlew clean build -x test

# Check if build succeeded
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed. Please fix the errors before deploying." -ForegroundColor Red
    exit 1
}

# Deploy to Railway
Write-Host "Deploying to Railway..." -ForegroundColor Cyan
railway up

# Check if deployment succeeded
if ($LASTEXITCODE -eq 0) {
    Write-Host "Deployment successful! Your application is now running on Railway." -ForegroundColor Green
    
    # Get the deployment URL
    Write-Host "Getting application URL..." -ForegroundColor Cyan
    railway status
    
    Write-Host "Don't forget to set the environment variables in the Railway dashboard:" -ForegroundColor Yellow
    Write-Host "  - FRONTEND_URL: https://www.agnifolio.com" -ForegroundColor Yellow
    Write-Host "  - GOOGLE_CLIENT_ID: Your Google OAuth2 client ID" -ForegroundColor Yellow
    Write-Host "  - GOOGLE_CLIENT_SECRET: Your Google OAuth2 client secret" -ForegroundColor Yellow
    Write-Host "  - SPRING_PROFILES_ACTIVE: prod" -ForegroundColor Yellow
} else {
    Write-Host "Deployment failed. Please check the errors." -ForegroundColor Red
    exit 1
}
