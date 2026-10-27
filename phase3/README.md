# Vanish - Phase 3: Web Interface & Client Interaction

**Course Learning Outcome (CLO) Addressed:** `CLO2` (Structured Web Programming, Client-Side Reliability & JSON Data Modeling).

---

## 1. Overview & Syllabus Deliverables

Phase 3 transforms the console-based business logic of Phases 1 & 2 into a modern, responsive web application interface:

1. **Design Registration and Login Forms Using HTML/CSS**:
   - `register.html`: Clean account registration form with responsive glassmorphic cards and dark-mode styling.
   - `login.html`: Secure login portal with demo account credentials.
   - `compose.html`: Secret note creation form with character countdown and expiry selectors.
   - `css/style.css`: Unified CSS design system featuring dark palette, neon accents, floating focus states, and responsive layouts.
2. **Apply JavaScript Validation (Empty Fields, Format Checks)**:
   - `js/validation.js`:
     - Username format check: 3–20 characters, alphanumeric and underscore, no spaces (`/^[a-zA-Z0-9_]{3,20}$/`).
     - Strong password validation: Minimum 8 characters, at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special symbol.
     - Live password strength meter: Calculates strength percentage (0–100%) and updates dynamic checklist items in real time.
     - Password confirmation: Strict equality verification.
     - Secret note text: 1 to 500 characters with live character counter.
     - Non-intrusive inline validation feedback (`is-valid` and `is-invalid` states).
3. **Basic Interaction Handling (Form Submission Flow)**:
   - Event interception (`e.preventDefault()`) on submit.
   - Inline feedback preventing submission of malformed inputs and focusing the first invalid field.
   - One-click copy for shareable secret links (`navigator.clipboard.writeText`).
   - Dynamic toast notifications for status updates.
4. **Represent User Data Format Using JSON (Simple Structure)**:
   - Standardized JSON representations for registration and note creation payloads.
   - Modal inspectors on every page allowing instant inspection of client-side JSON serialization.
   - Formal schema in `json/user_schema.json`, sample payloads in `json/user_sample.json` and `json/note_sample.json`.

---

## 2. Directory Structure

```
phase3/
├── index.html               # Phase 3 overview, curriculum mapping & navigation
├── register.html            # Registration form with live JS validation & checklist
├── login.html               # Login form with prefilled demo credentials
├── compose.html             # Secret note creation form with live character counter
├── dashboard_preview.html   # Client-side preview of sender notes ledger
├── read_preview.html        # Recipient secret revelation & burn-on-read simulation
├── css/
│   └── style.css            # Master stylesheet with dark theme & glassmorphism
├── js/
│   ├── validation.js        # Central client-side validation rules & JSON generators
│   └── compose.js           # Live counter, JSON modal & copy-to-clipboard handler
└── json/
    ├── user_schema.json     # JSON schema for user data format
    ├── user_sample.json     # Sample user registration JSON
    └── note_sample.json     # Sample note creation & response JSON
```

---

## 3. How to Run / View Phase 3

Phase 3 is static HTML5, CSS3, and modern JavaScript. You can view it in any modern browser:

- Double-click `index.html` (or `register.html` / `login.html`) to open directly in Chrome, Edge, or Firefox.
- Or serve locally via any HTTP server:
  ```bash
  # Using Python (if available):
  python -m http.server 8000
  
  # Or simply open index.html directly in browser
  start index.html
  ```
