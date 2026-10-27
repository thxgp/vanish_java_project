# Vanish

A one-time secret note handoff. You send someone a password, an OTP, an API key, or confidential details through a link that works **exactly once** and then destroys itself from memory.

The sender creates an account to manage their secret notes and track their read status. The recipient needs no account at all.

---

## How It Works

1. **Register / Login**: Sender registers and logs in securely. Passwords are never stored in raw text; they are protected by unique cryptographic salts and SHA-256 hashing.
2. **Compose a Secret Note**: Sender enters secret text (up to 500 characters), sets self-destruction expiry (e.g., 10 minutes), and specifies view limit (1 view for standard burn).
3. **AES-256 Encryption in Memory**: The server encrypts the secret with AES-256-GCM in volatile RAM and returns a cryptographically secure URL-safe token, e.g. `/read?t=k7Qm2xR9`.
4. **Recipient Accesses Link**: The recipient opens the link and sees the secret decrypted on-the-fly.
5. **Burn-on-Read**: The view counter hits zero, the ciphertext is **permanently purged from memory**, and the link is dead. Refreshing the page displays *"This note has been burned"*.
6. **Sender Dashboard**: The sender's dashboard displays real-time status of each note (`UNREAD`, `BURNED`, `EXPIRED`) with exact read timestamps.
7. **Logout & Session Invalidation**: Secure logout invalidates the `HttpSession` and enforces strict `No-Cache` headers, preventing back-button access.

---

## Pure In-Memory Architecture (Zero Database)

There is **no database / JDBC** anywhere in this project by design. Notes and accounts live in memory in a thread-safe `ConcurrentHashMap` managed by a Singleton `MemoryStore`. Short-lived secrets have no business being written to a hard drive that outlives them.

---

## 4-Phase Curriculum & CLO Alignment Matrix

| Phase | Title & Syllabus Content | CLOs Addressed | Status |
|:---:|:---|:---:|:---:|
| **1** | **Core Application Setup & Design**<br>&bull; User & Note entity classes<br>&bull; Constructors & methods<br>&bull; Basic string handling & input validation | **CLO1** | `COMPLETED` |
| **2** | **Structured Programming & Reliability**<br>&bull; Package organization (`model`, `service`, `store`, `util`, `exception`)<br>&bull; Service interfaces (`UserService`, `NoteService`)<br>&bull; Custom exception handling (`InvalidInputException`, `NoteBurnedException`, etc.)<br>&bull; Salted SHA-256 hashing & AES-256-GCM memory encryption | **CLO2** | `COMPLETED` |
| **3** | **Web Interface & Client Interaction**<br>&bull; Registration & login forms using HTML5/CSS3<br>&bull; Client-side JavaScript validation (regex, live password strength meter)<br>&bull; Form submission flow & 1-click clipboard actions<br>&bull; User data representation using JSON schema & payloads | **CLO2** | `COMPLETED` |
| **4** | **Server-Side Integration & Execution**<br>&bull; Servlets for registration, login, compose, read, logout (GET/POST)<br>&bull; Server-side credential validation<br>&bull; `HttpSession` tracking & 30-minute timeout<br>&bull; JSP dashboard with dynamic notes ledger (`dashboard.jsp`)<br>&bull; Access restriction without login (`AuthFilter`)<br>&bull; Session invalidation & back-button cache prevention (`NoCacheFilter`)<br>&bull; Web server deployment (`vanish.war` & embedded server) | **CLO1<br>CLO2<br>CLO3** | `COMPLETED` |

---

## Quick Start (Interactive Menu)

You can launch and test any phase using the included interactive launcher:
```bat
run_all_phases.bat
```

---

## Phase Details & How to Run

### Phase 1: Core Setup (Console)
Located in `phase1/final/`:
```bash
cd phase1/final
javac VanishPhase1.java
java VanishPhase1
```

### Phase 2: Structured Programming & Security (Console)
Located in `phase2/final/`:
```bash
cd phase2/final
# Run scripted automated walkthrough:
java -cp bin com.vanish.VanishPhase2

# Or run interactive CLI:
java -cp bin com.vanish.VanishCLI
```

### Phase 3: Web Interface & Client Validation
Located in `phase3/`:
- Open `phase3/index.html`, `phase3/register.html`, or `phase3/login.html` directly in any web browser.
- Live features include:
  - Dynamic password strength meter with checklist.
  - Live character countdown for secret notes (max 500 chars).
  - 1-click JSON modal inspector to view standardized user and note payloads (`json/user_schema.json`).
  - Interactive clipboard copy and toast notifications.

### Phase 4: Full-Stack Server, Servlets & JSP
Located in `phase4/`:

**Option A — Instant Standalone Server (Zero External Setup Required):**
```bash
cd phase4
run.bat
```
*(Automatically compiles, starts the server on port 8080, and opens `http://localhost:8080/` in your browser).*

**Option B — Deploy to Apache Tomcat 9:**
1. Grab the pre-built WAR archive: `phase4/vanish.war`.
2. Copy `vanish.war` to your Tomcat `webapps/` folder.
3. Access: `http://localhost:8080/vanish/`.

**Option C — Run Automated Verification Tests:**
```bash
cd phase4
java -cp "web/WEB-INF/lib/javax.servlet-api-4.0.1.jar;bin" com.vanish.test.Phase4Test
```
*(Executes 21 end-to-end tests validating 100% of the syllabus requirements).*

---

## Default Test Credentials

For quick evaluation, pre-configured credentials are provided:
- **Username:** `student`
- **Password:** `JavaPass@123`
