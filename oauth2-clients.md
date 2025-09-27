# OAuth2 Client Configuration

This document contains the OAuth2 client credentials for development and testing.

## Production Client

**Client ID:** `prospecto`
**Client Secret:** `prospecto-secret`
**Redirect URIs:**
- `http://localhost:8080/api/oauth2/athlete-tracker/callback`
- `http://localhost:3001/auth/callback`
- `https://localhost:3001/auth/callback`

**Scopes:** `athlete:read,performance:read,workouts:read,assessments:read`
**Grant Types:** `authorization_code,refresh_token`
**Authentication:** `client_secret_basic`
**Requires Consent:** Yes
**PKCE Required:** No

## Development Client

**Client ID:** `prospecto-dev`
**Client Secret:** `dev-secret`
**Redirect URIs:**
- `http://localhost:8080/api/oauth2/athlete-tracker/callback`
- `http://localhost:3001/auth/callback`
- `http://localhost:3001/auth/callback`

**Scopes:** `athlete:read,performance:read,workouts:read,assessments:read,athlete:contact`
**Grant Types:** `authorization_code,refresh_token,client_credentials`
**Authentication:** `client_secret_basic`
**Requires Consent:** No (for easier development)
**PKCE Required:** No

## OAuth2 Endpoints

**Authorization:** `http://localhost:8081/api/oauth2/authorize`
**Token:** `http://localhost:8081/api/oauth2/token`
**JWKs:** `http://localhost:8081/api/oauth2/jwks`
**User Info:** `http://localhost:8081/api/userinfo`
**Token Introspection:** `http://localhost:8081/api/oauth2/introspect`

## Frontend Integration

**Login URL:** `http://localhost:3001/oauth2/login` (configurable via `OAUTH2_FRONTEND_LOGIN_URL`)

The OAuth2 server redirects unauthenticated users to the OAuth2 login page with a `returnUrl` parameter containing the original OAuth2 authorization request. This is separate from the regular application login at `/login`.

## OAuth2 Authorization Flow

### Step 1: Authorization Request
```
GET http://localhost:8081/api/oauth2/authorize?response_type=code&client_id=prospecto&redirect_uri=http://localhost:8080/api/oauth2/athlete-tracker/callback&scope=athlete:read%20performance:read&state=xyz123
```

### Step 2: Login Redirect (New Flow)
If user is not authenticated, the server redirects to:
```
http://localhost:3001/oauth2/login?returnUrl=http%3A//localhost%3A8081/api/oauth2/authorize%3Fresponse_type%3Dcode%26client_id%3Dprospecto%26redirect_uri%3Dhttp%253A//localhost%253A8080/api/oauth2/athlete-tracker/callback%26scope%3Dathlete%253Aread%2520performance%253Aread%26state%3Dxyz123
```

### Step 3: OAuth2 Login
User logs in via the dedicated OAuth2 login page (`/oauth2/login`), which is separate from regular app login.

### Step 4: Return to OAuth2 Flow
After successful authentication, the OAuth2 login page redirects back to the `returnUrl` (OAuth2 authorization endpoint).

### Step 5: Authorization Completion
OAuth2 server proceeds with consent (if required) and authorization code generation.

## Frontend Implementation Requirements

**The OAuth2 login page (`/oauth2/login`) automatically:**

1. **Parses the `returnUrl` parameter** from the query string
2. **Authenticates the user** using session-based authentication (not JWT tokens)
3. **After successful authentication**, redirects the user back to the `returnUrl`
4. **Sets session cookies** so the OAuth2 server recognizes the authenticated user

**Implementation Details:**
- Uses `/api/auth/oauth2/login` endpoint for authentication
- Session-based authentication (compatible with OAuth2 flow)
- Separate from regular app login (`/login`) which uses JWT tokens
- Automatically redirects back to OAuth2 authorization endpoint after login

## Environment Configuration

**Backend Configuration:**
```yaml
app:
  oauth2:
    frontend-login-url: http://localhost:3001/oauth2/login
```

**Environment Variable:**
```bash
export OAUTH2_FRONTEND_LOGIN_URL=http://localhost:3001/oauth2/login
```

## Example Token Exchange

```bash
curl -X POST http://localhost:8081/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "prospecto:prospecto-secret" \
  -d "grant_type=authorization_code&code=AUTHORIZATION_CODE&redirect_uri=http://localhost:8080/api/oauth2/athlete-tracker/callback"
```

**Note:** In production, use proper secrets and HTTPS endpoints.