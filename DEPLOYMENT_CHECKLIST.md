# Deployment Checklist for Railway

This document provides a step-by-step checklist for deploying the Portfolio Tracker application to Railway.

## Prerequisites

1. [Railway CLI](https://docs.railway.app/develop/cli) installed and logged in
2. A Railway account with access to the project
3. PostgreSQL add-on enabled in your Railway project

## Environment Variables

Set the following environment variables in your Railway project:

- `FRONTEND_URL`: https://firefolio.up.railway.app
- `GOOGLE_CLIENT_ID`: Your Google OAuth2 client ID
- `GOOGLE_CLIENT_SECRET`: Your Google OAuth2 client secret
- `SPRING_PROFILES_ACTIVE`: prod

Railway automatically sets these variables for the PostgreSQL database:
- `DATABASE_URL`
- `PGDATABASE`
- `PGHOST`
- `PGPASSWORD`
- `PGPORT`
- `PGUSER`

## Deployment Steps

1. **Update Google OAuth2 Configuration**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Navigate to your project's OAuth consent screen
   - Add the production URL as an authorized domain: `firefolio.up.railway.app`
   - Under Credentials > OAuth 2.0 Client IDs, add the following authorized redirect URI:
     - `https://firefolio.up.railway.app/login/oauth2/code/google`

2. **Deploy to Railway**
   - Option 1: CLI deployment
     ```powershell
     cd c:\Users\Ami\ws\portfolio-tracker-backend
     railway up
     ```
   
   - Option 2: GitHub integration
     - Push changes to GitHub
     - Railway will automatically build and deploy

3. **Verify Database Migration**
   - The application will automatically run Liquibase migrations on startup
   - Check for successful migration in the application logs:
     ```
     Railway logs <service-name>
     ```

4. **Test the Application**
   - Open `https://firefolio.up.railway.app` in your browser
   - Test login functionality with Google OAuth
   - Verify that the frontend can access the backend API endpoints

## Troubleshooting

- **Database Connection Issues**
  - Check the Railway logs for connection errors
  - Verify that `DATABASE_URL` is correctly formatted and parsed by `DataSourceConfig`
  
- **OAuth2 Authentication Errors**
  - Verify that the redirect URIs are correctly set in Google Cloud Console
  - Check that `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are correctly set in Railway environment variables

- **CORS Issues**
  - Ensure `FRONTEND_URL` is set correctly in Railway environment variables
  - Check browser console for CORS-related errors
