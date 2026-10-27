# Vanish - Phase 4: Server-Side Integration & Execution

**Course Learning Outcomes (CLOs) Addressed:** `CLO1` (Core Setup), `CLO2` (Reliability & Exceptions), and `CLO3` (Web Application Flow, Servlets, JSP, Session Tracking & Deployment).

---

## 1. Overview & Syllabus Deliverables

Phase 4 integrates the Phase 1 entities, Phase 2 services/cryptography, and Phase 3 web interfaces into a complete, full-stack server-side Java web application:

1. **Process Registration and Login Using Servlets (GET/POST)**:
   - `RegisterServlet` (`/register`): Handles GET (serves `register.html`) and POST (extracts parameters, validates server-side, calls `UserService.register()`, sets flash redirect message).
   - `LoginServlet` (`/login`): Handles GET (serves `login.html`) and POST (authenticates credentials via `UserService.authenticate()`).
2. **Validate User Credentials on Server Side**:
   - Enforces `ValidationUtil.validateUsername()` and `ValidationUtil.validatePassword()`.
   - Catches domain exceptions (`InvalidInputException`, `AuthenticationException`, `UserAlreadyExistsException`) and surfaces clear error states without crashing.
3. **Implement Session Tracking Using `HttpSession`**:
   - Establishes `HttpSession session = req.getSession(true);` upon login.
   - Binds `user` object and `username` to session scope.
   - Configures a 30-minute session timeout in `web.xml` (`<session-timeout>30</session-timeout>`) and programmatic invalidation.
4. **Create JSP Page for User Dashboard (`dashboard.jsp`)**:
   - Displays sender details, masked salted SHA-256 hash preview, and registration timestamp.
   - Iterates through the list of notes created by the logged-in user retrieved from `NoteService.getNotesByOwner()`.
   - Color-coded badges for status: `UNREAD` (green), `BURNED` (red), `EXPIRED` (gray).
   - Shows views left, creation stamp, expiry stamp, and read timestamp.
   - Features 1-click clipboard copy for shareable links and embedded quick note composer.
5. **Restrict Dashboard Access Without Login**:
   - `AuthFilter` (`javax.servlet.Filter`): Intercepts requests to `/dashboard`, `/dashboard.jsp`, and `/compose`.
   - Checks `session != null && session.getAttribute("user") != null`.
   - Redirects unauthenticated requests to `/login?error=Access+restricted.+Please+login+first.`.
6. **Implement Logout and Session Invalidation**:
   - `LogoutServlet` (`/logout`): Calls `session.removeAttribute("user")` and `session.invalidate()`.
   - `NoCacheFilter`: Sends `Cache-Control: no-cache, no-store, must-revalidate`, `Pragma: no-cache`, `Expires: 0` headers across all responses. Prevents browser history / back button from revealing private notes after logout.
7. **Deploy and Execute on Web Server**:
   - Standard Java EE Web Application layout with `web.xml` and pre-built `vanish.war`.
   - Also includes `com.vanish.server.VanishServer` (embedded zero-setup server) so anyone can run the complete web application with a single command!

---

## 2. Directory Structure

```
phase4/
├── build.bat               # Compiles all sources & packages vanish.war
├── run.bat                 # Launches standalone server & opens browser
├── vanish.war              # Complete WAR archive ready for Tomcat deployment
├── bin/                    # Compiled class files
├── src/com/vanish/
│   ├── model/             # User, Note, NoteStatus entities
│   ├── service/           # UserService, NoteService interfaces & implementations
│   ├── store/             # Thread-safe Singleton MemoryStore
│   ├── util/              # CryptoUtil (AES-256 + SHA-256), TokenGenerator, ValidationUtil
│   ├── exception/         # Domain exceptions (Authentication, Burned, Expired, etc.)
│   ├── web/               # Servlets & Filters (Register, Login, Dashboard, Compose, Read, Logout, AuthFilter, NoCacheFilter)
│   ├── server/            # VanishServer embedded HTTP server
│   └── test/              # Phase4Test automated verification test suite
└── web/
    ├── index.html          # Landing portal
    ├── register.html       # Client registration form
    ├── login.html          # Authentication portal
    ├── compose.html        # Secret note composer
    ├── dashboard.jsp       # Authenticated JSP notes ledger
    ├── read.jsp            # Recipient secret revelation screen
    ├── burned.jsp          # Burn-on-read dead link screen
    ├── expired.jsp         # Expired link screen
    ├── error.jsp           # 404 / error notice
    ├── css/style.css       # Master stylesheet
    ├── js/validation.js    # Client-side validation module
    ├── js/compose.js       # Composer logic & JSON modal
    └── WEB-INF/
        ├── web.xml         # Deployment descriptor
        ├── classes/        # Deployed classes
        └── lib/            # javax.servlet-api-4.0.1.jar
```

---

## 3. How to Run Phase 4

### Option A: Standalone Embedded Server (Zero Setup)
Simply double-click `run.bat` or run:
```bash
cd phase4
java -cp "web/WEB-INF/lib/javax.servlet-api-4.0.1.jar;bin" com.vanish.server.VanishServer
```
Then open your browser to: **`http://localhost:8080/`**

### Option B: Deploy to Apache Tomcat 9
1. Copy `phase4/vanish.war` into your Tomcat `webapps/` directory.
2. Start Tomcat (`bin/startup.bat` or service).
3. Access: **`http://localhost:8080/vanish/`**

### Option C: Run Automated Integration Tests
```bash
cd phase4
java -cp "web/WEB-INF/lib/javax.servlet-api-4.0.1.jar;bin" com.vanish.test.Phase4Test
```
*(Runs 21 test cases verifying 100% of syllabus requirements).*
