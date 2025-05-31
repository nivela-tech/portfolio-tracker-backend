# Google OAuth2 Configuration for Production

This guide provides step-by-step instructions for setting up Google OAuth2 for your production environment.

## Prerequisites

- A Google Cloud Platform account
- A project created in the Google Cloud Console

## Step 1: Update OAuth Consent Screen

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project
3. Navigate to **APIs & Services > OAuth consent screen**
4. Under **Authorized domains**, add `www.agnifolio.com`
5. Save changes

## Step 2: Create a New OAuth Client for Production

1. Navigate to **APIs & Services > Credentials**
2. Click on **+ CREATE CREDENTIALS** at the top of the page
3. Select **OAuth client ID**
4. For Application type, select **Web application**
5. Give it a descriptive name like "Firefolio Production"
6. Under **Authorized JavaScript origins**, add:
   ```
   https://www.agnifolio.com
   ```
7. Under **Authorized redirect URIs**, add:
   ```
   https://www.agnifolio.com/login/oauth2/code/google
   ```
8. Click **Create**
9. A popup will show your new client ID and client secret - save these securely for the next step

## Step 3: Update Environment Variables in Railway

1. Go to your Railway project dashboard
2. Navigate to the **Variables** tab
3. Add the following environment variables:
   ```
   GOOGLE_CLIENT_ID=your-production-google-client-id
   GOOGLE_CLIENT_SECRET=your-production-google-client-secret
   FRONTEND_URL=https://www.agnifolio.com
   SPRING_PROFILES_ACTIVE=prod
   ```
   
   > **Important**: Make sure to use the new production OAuth client credentials you just created, not your development credentials.

4. Deploy or restart your service for the changes to take effect

## Testing

After updating the configuration:

1. Open `https://www.agnifolio.com` in your browser
2. Click on "Login with Google"
3. You should be redirected to Google's authentication page
4. After successful authentication, you should be redirected back to your application

## Troubleshooting

### Error: "redirect_uri_mismatch"

- Verify that the exact redirect URI is added to the Google Cloud Console
- Check for any typos in the URL
- Ensure the protocol (https) is correct

### Error: "invalid_client"

- Verify that the client ID and client secret are correctly set in the environment variables
- Check that you're using the correct credentials for the production environment

### Error: "access_denied"

- Verify that the user has granted the requested permissions
- Check that the scopes requested match the ones configured in the Google Cloud Console
