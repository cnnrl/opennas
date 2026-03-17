# OpenNAS

## Project Description
__OpenNAS__ is a self-hosted personal network attached storage and media server built with Spring
Boot. It is designed to run on a local machine or homelab to be accessed on the LAN or through VPN.
It is not currently designed for public cloud services.  
  
There are currently two main services provided by __OpenNAS__: An encrypted file store, and music
streaming over HTTP. The current encryption algorithm for files at rest is AES-256-GCM. Music is
unencrypted when stored for lower overhead when streaming. All streaming sessions are secured using a
stream token to allow authentication without custom HTTP headers. JAudioTagger is used to find the metadata
within the music files. Metadata for files, music, and users are stored in SQL.  
  
Every request is logged in `json` format. This is done using an AOP-based system. A file cleaning
service is also included for the file service that compares any dangling metadata entries against
the filesystem. Any file within the file system but not present in the database, or any database entry not present
in the filesystem is deleted.  
  
## Requirements
- Java 21
- Git
- Bash or a compatible shell (for some helper/test scripts)

## Tech Stack
- Spring Boot 4 (Gradle)
- Spring Web MVC
- Spring Security 7 
- Spring Data JPA
- Spring AOP
- Spring Scheduling
- H2 Database
- JWT (`jjwt 0.12.6`)
- JAudioTagger
- AES/GCM Encryption (`javax.crypto`)

## Getting Started
### 1. Clone the repository
```bash
git clone <your-repo-url>
cd opennas
```

### 2. Set required secrets
The app reads secrets from environment variables (not committed to git).

Required:
- `JWT_SECRET`
- `CRYPT_SECRET`

Option A: use `.env` (recommended for local dev)
```bash
cp .env.example .env
# Edit .env and replace placeholders with strong Base64 values
```

Option B: export directly
```bash
export JWT_SECRET="<base64-value>"
export CRYPT_SECRET="<base64-value>"
```

Generate Base64 values (example):
```bash
openssl rand -base64 32
openssl rand -base64 32
```

If using `.env` in bash for commands other than `bootRun`:
```bash
set -a
source .env
set +a
```

Note:
- `./gradlew bootRun` already loads variables from `.env` (via Gradle task config).

### 3. Run the application
```bash
./gradlew bootRun
```

The app starts on the default Spring Boot port:
- `http://localhost:8080`

## Building
```bash
./gradlew clean build
```

## API Reference

All protected routes require an `Authorization: Bearer <token>` header unless otherwise noted.

### Auth

- POST `/register` — None — Register a new user. Returns a JWT.
- POST `/login` — None — Login with credentials. Returns a JWT.

### Files

- POST `/upload` — JWT — Upload a file. Returns file metadata.
- GET `/files` — JWT — List all files owned by the authenticated user.
- GET `/files/{id}` — JWT — View/stream a file inline.
- GET `/download/{id}` — JWT — Download a file as an attachment.
- DELETE `/delete/{id}` — JWT + ADMIN — Delete a file by ID. Returns a delete confirmation ticket.

### Music

- POST `/music/upload` — JWT + ADMIN — Upload a song. Extracts metadata automatically from audio tags.
- GET `/music` — JWT — List all songs with metadata.
- GET `/music/art/{id}` — JWT — Get cover art for a song. Returns a default image if none exists.
- GET `/stream/token/{id}` — JWT — Get a short-lived stream token for a song.
- GET `/stream/{id}?token={token}` — JWT + Stream Token — Stream a song. Supports `Range` requests and also works without a `Range` header.

## File Storage Structure

The application stores all data under `~/opennas/` (relative to the user running the app):

- `files/{owner}/{id}/{filename}` — Encrypted user files (AES/GCM)
- `music/{id}/{filename}` — Audio files (unencrypted)
- `music/{id}/cover.{jpg|png}` — Album art (if present in audio tags)
- `logs/{date}.json` — Audit logs (JSON lines, daily rotation)
- `temp/{id}/{filename}` — Temporary upload staging (cleaned after upload)

Notes:
- User files are encrypted at rest using AES-256-GCM. Each file has a unique IV prepended.
- Music files are stored unencrypted due to streaming performance requirements.
- Audit logs record every request including IP address, user, action, and file.
- A scheduled cleaner runs periodically to remove orphaned files not tracked in the database.

## Testing
### Unit/integration tests (Gradle)
```bash
./gradlew test
```

### Integration script
There is an additional script-based integration test:
```bash
bash src/test/integration/song-stream-test.sh
```

Notes:
- Make sure the app is running before script-based endpoint tests.
- Ensure `JWT_SECRET` and `CRYPT_SECRET` are available to the running app.

## Configuration
Main config file:
- `src/main/resources/application.properties`

Security-sensitive values are environment-driven:
- `jwt.secret=${JWT_SECRET}`
- `crypt.secret=${CRYPT_SECRET}`

## Security and Secret Hygiene
- Never commit `.env` or other local secret files.
- `.env.example` is safe to commit and serves as a template.
- A local pre-commit hook exists at `.githooks/pre-commit` to block obvious secret leaks.
- CI runs a secret scan workflow:
  - `.github/workflows/secret-scan.yml`

## Development Notes
- Gradle wrapper is included (`./gradlew`), so no local Gradle install is required.
- Java toolchain is configured for Java 21 in `build.gradle`.

## Troubleshooting
### Hook blocks a commit
Review staged files and remove secret-containing files from staging:
```bash
git status
git restore --staged <file>
```

### CI secret scan fails
- Confirm `.gitleaks.toml` is committed.
- Verify no real secrets were added to tracked files.
