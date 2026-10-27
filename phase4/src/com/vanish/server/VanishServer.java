package com.vanish.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vanish.exception.AuthenticationException;
import com.vanish.exception.InvalidInputException;
import com.vanish.exception.NoteBurnedException;
import com.vanish.exception.NoteExpiredException;
import com.vanish.exception.NoteNotFoundException;
import com.vanish.exception.NotePendingException;
import com.vanish.exception.UserAlreadyExistsException;
import com.vanish.model.Note;
import com.vanish.model.NoteStatus;
import com.vanish.model.User;
import com.vanish.service.NoteService;
import com.vanish.service.UserService;
import com.vanish.service.impl.NoteServiceImpl;
import com.vanish.service.impl.UserServiceImpl;
import com.vanish.service.AdminService;
import com.vanish.service.impl.AdminServiceImpl;
import com.vanish.util.AdminBootstrap;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * ==========================================================================
 *  VANISH - Phase 4 Embedded Web Server (CLO1, CLO2, CLO3)
 *
 *  Provides a standalone, zero-external-dependency HTTP server that executes
 *  the full Phase 4 server-side architecture:
 *    - Process registration & login with server-side validation
 *    - Session tracking with cookies and in-memory session manager
 *    - Access control (restricting dashboard without active login)
 *    - Burn-on-read secret retrieval and in-memory destruction
 *    - Logout and complete session invalidation
 *    - Dynamic HTML/JSP templating
 * ==========================================================================
 */
public class VanishServer {

    private static int activePort = 8080;
    private static final UserService userService = new UserServiceImpl();
    private static final NoteService noteService = new NoteServiceImpl();
    private static final AdminService adminService = new AdminServiceImpl();
    private static final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();

    private static Path webRoot = Paths.get("web");

    static class SessionData {
        final String sessionId;
        final User user;
        LocalDateTime lastAccess;

        SessionData(String sessionId, User user) {
            this.sessionId = sessionId;
            this.user = user;
            this.lastAccess = LocalDateTime.now();
        }

        boolean isExpired() {
            // 30 minutes session timeout
            return LocalDateTime.now().isAfter(lastAccess.plusMinutes(30));
        }

        void touch() {
            this.lastAccess = LocalDateTime.now();
        }
    }

    public static void main(String[] args) throws IOException {
        // Resolve web root
        if (!Files.exists(webRoot)) {
            Path alt = Paths.get("phase4/web");
            if (Files.exists(alt)) {
                webRoot = alt;
            } else {
                webRoot = Paths.get("../web");
            }
        }

        // Preload default demo user: student / JavaPass@123
        try {
            User demo = userService.register("student", "JavaPass@123", "JavaPass@123");
            Note n1 = noteService.createNote("student", "ATM PIN: 4821", 60, 1);
            Note n2 = noteService.createNote("student", "WiFi Password: SuperSecretWifi@2026", 120, 2);
            System.out.println("[BOOTSTRAP] Demo user 'student' loaded with notes: " + n1.getToken() + ", " + n2.getToken());
        } catch (Exception ignored) {
        }
        AdminBootstrap.seed(userService);

        int targetPort = 8080;
        if (args.length > 0) {
            try {
                targetPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        HttpServer server = null;
        for (int p = targetPort; p <= targetPort + 10; p++) {
            try {
                server = HttpServer.create(new InetSocketAddress(p), 0);
                activePort = p;
                break;
            } catch (java.net.BindException e) {
                // Try next port if current is occupied
            }
        }

        if (server == null) {
            throw new IOException("Could not bind server to ports " + targetPort + " through " + (targetPort + 10));
        }

        server.setExecutor(Executors.newCachedThreadPool());

        // Handlers
        server.createContext("/", new StaticAndAppHandler());

        System.out.println("==============================================================");
        System.out.println("   VANISH - Phase 4 Server Running (CLO1, CLO2, CLO3)");
        System.out.println("   Access Application at: http://localhost:" + activePort + "/");
        System.out.println("   Pre-configured account: student / JavaPass@123");
        System.out.println("   Admin account: " + (System.getenv("VANISH_ADMIN_USER") == null && System.getenv("VANISH_ADMIN_PASSWORD") == null
                ? "admin / Admin@12345 (default)"
                : (System.getenv("VANISH_ADMIN_USER") != null ? System.getenv("VANISH_ADMIN_USER") : "admin") + " (password from VANISH_ADMIN_PASSWORD)"));
        System.out.println("   Web Root: " + webRoot.toAbsolutePath());
        System.out.println("==============================================================");

        server.start();
    }

    private static class StaticAndAppHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod().toUpperCase();

            // Set No-Cache headers globally
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.getResponseHeaders().set("Pragma", "no-cache");
            exchange.getResponseHeaders().set("Expires", "0");

            SessionData session = getSession(exchange);

            try {
                if (path.equals("/") || path.equals("/index.html")) {
                    serveFile(exchange, webRoot.resolve("index.html"), "text/html");
                } else if (path.equals("/register.html") || path.equals("/register")) {
                    if ("POST".equals(method)) {
                        handleRegister(exchange);
                    } else {
                        serveFile(exchange, webRoot.resolve("register.html"), "text/html");
                    }
                } else if (path.equals("/login.html") || path.equals("/login")) {
                    if ("POST".equals(method)) {
                        handleLogin(exchange);
                    } else {
                        serveFile(exchange, webRoot.resolve("login.html"), "text/html");
                    }
                } else if (path.equals("/logout")) {
                    handleLogout(exchange, session);
                } else if (path.equals("/dashboard") || path.equals("/dashboard.jsp")) {
                    // Protected route: Restrict dashboard access without login
                    if (session == null || session.isExpired()) {
                        redirect(exchange, "/login.html?error=" + urlEncode("Access restricted. Please log in first."));
                        return;
                    }
                    session.touch();
                    renderDashboard(exchange, session.user);
                } else if (path.equals("/compose") || path.equals("/compose.html")) {
                    if ("POST".equals(method)) {
                        if (session == null || session.isExpired()) {
                            redirect(exchange, "/login.html?error=" + urlEncode("Please log in to compose notes."));
                            return;
                        }
                        session.touch();
                        handleCompose(exchange, session.user);
                    } else {
                        serveFile(exchange, webRoot.resolve("compose.html"), "text/html");
                    }
                } else if (path.equals("/read")) {
                    handleRead(exchange);
                } else if (path.startsWith("/css/")) {
                    serveFile(exchange, webRoot.resolve(path.substring(1)), "text/css");
                } else if (path.startsWith("/js/")) {
                    serveFile(exchange, webRoot.resolve(path.substring(1)), "application/javascript");
                } else if (path.startsWith("/json/")) {
                    serveFile(exchange, webRoot.resolve(path.substring(1)), "application/json");
                } else if (path.equals("/admin") || path.startsWith("/admin/")) {
                    handleAdmin(exchange, session, path, method);
                } else {
                    Path file = webRoot.resolve(path.startsWith("/") ? path.substring(1) : path);
                    if (insideWebRoot(file) && Files.exists(file) && !Files.isDirectory(file)) {
                        String mime = Files.probeContentType(file);
                        serveFile(exchange, file, mime != null ? mime : "text/plain");
                    } else {
                        sendResponse(exchange, 404, "text/html", "<h1>404 Not Found</h1><p>Resource " + escapeHtml(path) + " does not exist.</p>");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "text/html", "<h1>500 Server Error</h1><pre>" + e.getMessage() + "</pre>");
            }
        }
    }

    // --- Controller Actions ---

    private static void handleRegister(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseFormData(exchange);
        String username = params.get("username");
        String password = params.get("password");
        String confirmPassword = params.get("confirmPassword");

        try {
            User user = userService.register(username, password, confirmPassword);
            redirect(exchange, "/login.html?msg=" + urlEncode("Account successfully created for " + user.getUsername() + "! Please sign in."));
        } catch (InvalidInputException | UserAlreadyExistsException e) {
            redirect(exchange, "/register.html?error=" + urlEncode(e.getMessage()));
        }
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseFormData(exchange);
        String username = params.get("username");
        String password = params.get("password");

        try {
            User user = userService.authenticate(username, password);
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            activeSessions.put(sessionId, new SessionData(sessionId, user));

            // Set Session Cookie
            exchange.getResponseHeaders().add("Set-Cookie", "VANISH_SESSION_ID=" + sessionId + "; Path=/; HttpOnly");
            redirect(exchange, user.isAdmin() ? "/admin" : "/dashboard");
        } catch (InvalidInputException | AuthenticationException e) {
            redirect(exchange, "/login.html?error=" + urlEncode(e.getMessage()));
        }
    }

    private static void handleLogout(HttpExchange exchange, SessionData session) throws IOException {
        if (session != null) {
            activeSessions.remove(session.sessionId);
        }
        exchange.getResponseHeaders().add("Set-Cookie", "VANISH_SESSION_ID=; Path=/; Max-Age=0; HttpOnly");
        redirect(exchange, "/login.html?msg=" + urlEncode("You have been logged out securely."));
    }

    private static void handleCompose(HttpExchange exchange, User user) throws IOException {
        Map<String, String> params = parseFormData(exchange);
        String secretText = params.get("secretText");
        int expiry = 10;
        int views = 1;
        try {
            if (params.containsKey("expiryMinutes")) expiry = Integer.parseInt(params.get("expiryMinutes"));
            if (params.containsKey("views")) views = Integer.parseInt(params.get("views"));
        } catch (NumberFormatException ignored) {
        }

        try {
            Note note = noteService.createNote(user.getUsername(), secretText, expiry, views);
            redirect(exchange, "/dashboard?created=" + note.getToken());
        } catch (InvalidInputException e) {
            redirect(exchange, "/compose.html?error=" + urlEncode(e.getMessage()));
        }
    }

    private static void handleRead(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
        String token = query.get("t");

        if (token == null || token.trim().isEmpty()) {
            sendResponse(exchange, 400, "text/html", "<h1>400 Bad Request</h1><p>Missing note token 't'.</p>");
            return;
        }

        token = token.trim();

        try {
            String decryptedSecret = noteService.readNote(token);
            renderReadScreen(exchange, token, decryptedSecret);
        } catch (NoteBurnedException e) {
            renderBurnedScreen(exchange, token, e.getMessage());
        } catch (NoteExpiredException e) {
            renderExpiredScreen(exchange, token, e.getMessage());
        } catch (NotePendingException e) {
            renderPendingScreen(exchange, token);
        } catch (NoteNotFoundException e) {
            renderErrorScreen(exchange, "No note found with token: " + token);
        }
    }

    // --- Dynamic Templating Renderers ---

    private static void renderDashboard(HttpExchange exchange, User user) throws IOException {
        List<Note> notes = noteService.getNotesByOwner(user.getUsername());
        Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
        String createdToken = query.get("created");

        String hostHeader = exchange.getRequestHeaders().getFirst("Host");
        String scheme = "http";
        String proto = exchange.getRequestHeaders().getFirst("X-Forwarded-Proto");
        if (proto != null && !proto.isEmpty()) {
            scheme = proto;
        }
        String baseUrl = (hostHeader != null && !hostHeader.trim().isEmpty())
                ? scheme + "://" + hostHeader
                : "http://localhost:" + activePort;

        StringBuilder rows = new StringBuilder();
        if (notes.isEmpty()) {
            rows.append("<tr><td colspan='8' style='text-align:center; padding:2rem; color:var(--text-muted);'>No notes created yet.</td></tr>");
        } else {
            for (Note n : notes) {
                String linkUrl = baseUrl + "/read?t=" + n.getToken();
                rows.append("<tr>")
                        .append("<td>#").append(n.getId()).append("</td>")
                        .append("<td><code class='code-font' style='color:var(--accent-warm); font-weight:600;'>").append(n.getToken()).append("</code></td>")
                        .append("<td>").append(statusBadge(n.getStatus())).append("</td>")
                        .append("<td>").append(n.getViewsLeft()).append("</td>")
                        .append("<td style='color:var(--text-secondary);'>").append(n.getCreatedAt().format(Note.STAMP)).append("</td>")
                        .append("<td style='color:var(--text-secondary);'>").append(n.getExpiresAt().format(Note.STAMP)).append("</td>")
                        .append("<td style='color:").append(n.getReadAt() != null ? "var(--danger)" : "var(--text-muted)").append(";'>")
                        .append(n.getReadAt() != null ? n.getReadAt().format(Note.STAMP) : "-").append("</td>")
                        .append("<td><div style='display:flex; gap:0.4rem;'>")
                        .append("<button type='button' class='btn btn-secondary btn-sm copy-btn' data-url='").append(linkUrl).append("'>Copy</button>")
                        .append("<a href='").append(linkUrl).append("' class='btn btn-primary btn-sm' target='_blank'>Open</a>")
                        .append("</div></td></tr>");
            }
        }

        String banner = "";
        if (createdToken != null && !createdToken.isEmpty()) {
            String fullUrl = baseUrl + "/read?t=" + createdToken;
            banner = "<div class='alert alert-success'><div><strong>Secret Note Created!</strong> "
                    + (user.isAdmin() ? "" : "Waiting for admin approval before the link works. ")
                    + "Share link: <code class='code-font' style='color:#ffffff;'>"
                    + fullUrl + "</code> <button type='button' class='btn btn-primary btn-sm copy-btn' data-url='" + fullUrl + "' style='margin-left:0.5rem;'>Copy Link</button></div></div>";
        }

        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Dashboard — Vanish</title><link rel='stylesheet' href='/css/style.css'><script src='/js/theme.js'></script></head><body data-current-user='" + escapeHtml(user.getUsername()) + "'>"
                + "<header class='navbar'><div class='container nav-wrapper'><a href='/' class='brand'><span class='brand-icon'>⚡</span><span>VANISH</span></a>"
                + "<div class='nav-right'><nav><ul class='nav-links'><li><a href='/dashboard' class='nav-link active'>Dashboard</a></li><li><a href='/compose' class='nav-link'>Compose</a></li>"
                + (user.isAdmin() ? "<li><a href='/admin' class='nav-link'>Admin</a></li>" : "")
                + "<li><div class='user-badge'><span class='user-avatar'>👤</span><span>" + escapeHtml(user.getUsername()) + "</span></div></li>"
                + "<li><form action='/logout' method='POST' style='display:inline;'><button type='submit' class='btn btn-secondary btn-sm'>Sign Out</button></form></li></ul></nav>"
                + "<button type='button' class='theme-toggle-btn' aria-label='Toggle theme'></button></div></div></header>"
                + "<main class='container' style='padding-top:2.5rem; padding-bottom:3.5rem; flex:1;'>"
                + banner
                + "<div class='card' style='margin-bottom:2rem;'><div style='display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:1rem;'>"
                + "<div style='display:flex; align-items:center; gap:1rem;'><div class='user-avatar' style='width:52px; height:52px; font-size:1.4rem;'>👤</div><div>"
                + "<h1 style='font-size:1.6rem; font-weight:700; margin:0;'>Welcome back, " + escapeHtml(user.getUsername()) + "!</h1><p style='font-size:0.85rem; color:var(--text-secondary); margin:0;'>Registered: " + user.getRegisteredAt().format(Note.STAMP) + "</p></div></div>"
                + "<div style='display:flex; gap:0.5rem;'><a href='#composeForm' class='btn btn-primary btn-sm'>+ New Note</a><a href='/dashboard' class='btn btn-secondary btn-sm'>↻ Refresh</a></div></div>"
                + "<div style='display:grid; grid-template-columns:repeat(auto-fit, minmax(220px, 1fr)); gap:1rem; margin-top:1.5rem; padding-top:1.5rem; border-top:1px solid var(--border-subtle); font-size:0.825rem;'>"
                + "<div><span style='color:var(--text-muted); display:block; margin-bottom:0.25rem;'>Password Hash (Salted SHA-256):</span><code class='code-font' style='color:var(--text-secondary);'>" + user.getPasswordHash().substring(0, Math.min(22, user.getPasswordHash().length())) + "...</code></div>"
                + "<div><span style='color:var(--text-muted); display:block; margin-bottom:0.25rem;'>Cryptographic Salt:</span><code class='code-font' style='color:var(--text-secondary);'>" + user.getSalt() + "</code></div>"
                + "<div><span style='color:var(--text-muted); display:block; margin-bottom:0.25rem;'>RAM Ciphertext Status:</span><span style='color:var(--success); font-weight:600;'>AES-256-GCM In-Memory</span></div></div></div>"
                + "<div id='composeForm' class='card' style='margin-bottom:2rem;'><div class='card-header'><h2 class='card-title' style='font-size:1.25rem;'>Compose Secret Note</h2>"
                + "<p class='card-subtitle'>Secret is kept in volatile RAM only and permanently burned upon read.</p></div>"
                + "<form action='/compose' method='POST' id='composeNoteForm'><div class='form-group'>"
                + "<div style='display:flex; justify-content:space-between; margin-bottom:0.35rem;'><label class='form-label' style='margin:0;'>Secret Content</label><span id='charCounter' style='font-size:0.775rem; color:var(--text-muted); font-family:var(--font-mono);'>0 / 500 chars</span></div>"
                + "<textarea id='secretText' name='secretText' class='form-control' rows='3' placeholder='Paste private message, password, API key...' required maxlength='500'></textarea></div>"
                + "<div class='grid-2'><div class='form-group'><label class='form-label'>Expiry Duration</label><select name='expiryMinutes' class='form-control'><option value='5'>5 Minutes</option><option value='10' selected>10 Minutes</option><option value='60'>1 Hour</option><option value='1440'>24 Hours</option></select></div>"
                + "<div class='form-group'><label class='form-label'>Allowed Views</label><select name='views' class='form-control'><option value='1' selected>1 View (Burn-On-Read)</option><option value='2'>2 Views</option><option value='3'>3 Views</option><option value='5'>5 Views</option></select></div></div>"
                + "<button type='submit' class='btn btn-primary'>Generate Secret Link</button></form></div>"
                + "<div class='card'><div class='card-header'><h2 class='card-title'>Active Notes Ledger</h2><p class='card-subtitle'>Live tracking and delivery status of all notes generated by " + escapeHtml(user.getUsername()) + ".</p></div>"
                + "<div class='table-responsive'><table class='table'><thead><tr><th>ID</th><th>Token</th><th>Status</th><th>Views Left</th><th>Created At</th><th>Expires At</th><th>Read At</th><th>Link Action</th></tr></thead><tbody>"
                + rows.toString() + "</tbody></table></div></div></main>"
                + "<div class='toast-container'></div>"
                + "<footer><div class='container footer-content'><div>&copy; 2026 Vanish &bull; Military-grade One-Time Secret Handoff</div><div><span>Zero Knowledge &bull; End-to-End In-Memory Encryption</span></div></div></footer>"
                + "<script src='/js/validation.js'></script><script src='/js/compose.js'></script>"
                + "<script>document.querySelectorAll('.copy-btn').forEach(btn => { btn.addEventListener('click', () => { navigator.clipboard.writeText(btn.dataset.url).then(() => { showToast('Secret link copied to clipboard!', 'success'); }); }); });</script>"
                + "</body></html>";

        sendResponse(exchange, 200, "text/html", html);
    }

    private static void renderReadScreen(HttpExchange exchange, String token, String secret) throws IOException {
        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Secret Revealed — Vanish</title><link rel='stylesheet' href='/css/style.css'><script src='/js/theme.js'></script></head><body>"
                + "<header class='navbar'><div class='container nav-wrapper'><a href='/' class='brand'><span class='brand-icon'>⚡</span><span>VANISH</span></a>"
                + "<div class='nav-right'><span style='font-size:0.85rem; color:var(--text-muted);'>Encrypted Handoff</span><button type='button' class='theme-toggle-btn' aria-label='Toggle theme'></button></div></div></header>"
                + "<main class='auth-wrapper' style='padding:3.5rem 1rem;'><div class='card auth-card' style='max-width:580px;'>"
                + "<div class='card-header' style='margin-bottom:1rem;'><div style='display:flex; justify-content:space-between; align-items:center;'><h1 class='card-title' style='color:var(--success); font-size:1.35rem;'>Secret Revealed</h1><span class='badge badge-unread'>Read & Burned</span></div>"
                + "<p class='card-subtitle'>Token: <code class='code-font' style='color:var(--primary);'>" + token + "</code> &bull; Decrypted from memory</p></div>"
                + "<div class='secret-box'><div style='font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; color:var(--text-muted); margin-bottom:0.35rem;'>Decrypted Secret Message:</div>"
                + "<div class='secret-text' id='revealedSecret'>" + escapeHtml(secret) + "</div></div>"
                + "<div class='alert alert-warning' style='font-size:0.85rem; margin-bottom:1.5rem;'><div><strong>Warning:</strong> This secret has now been purged from server memory. Refreshing this page will permanently show a burned status.</div></div>"
                + "<div style='display:flex; gap:0.75rem;'><button type='button' id='copyBtn' class='btn btn-primary btn-block'>Copy Secret</button><a href='/' class='btn btn-secondary btn-block'>Done</a></div>"
                + "</div></main>"
                + "<div class='toast-container'></div>"
                + "<footer><div class='container footer-content'><div>&copy; 2026 Vanish &bull; Military-grade One-Time Secret Handoff</div><div><span>Zero Knowledge &bull; End-to-End In-Memory Encryption</span></div></div></footer>"
                + "<script>document.getElementById('copyBtn').addEventListener('click', () => { navigator.clipboard.writeText(document.getElementById('revealedSecret').textContent.trim()).then(() => { showToast('Secret copied to clipboard!', 'success'); }); });"
                + "function showToast(msg, type='info') { let c = document.querySelector('.toast-container'); if (!c) { c = document.createElement('div'); c.className = 'toast-container'; document.body.appendChild(c); } const t = document.createElement('div'); t.className = 'toast toast-' + type; t.textContent = msg; c.appendChild(t); setTimeout(() => { t.style.opacity = '0'; setTimeout(() => t.remove(), 300); }, 3000); }</script></body></html>";

        sendResponse(exchange, 200, "text/html", html);
    }

    private static void renderBurnedScreen(HttpExchange exchange, String token, String reason) throws IOException {
        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Note Burned — Vanish</title><link rel='stylesheet' href='/css/style.css'><script src='/js/theme.js'></script></head><body>"
                + "<header class='navbar'><div class='container nav-wrapper'><a href='/' class='brand'><span class='brand-icon'>⚡</span><span>VANISH</span></a>"
                + "<div class='nav-right'><button type='button' class='theme-toggle-btn' aria-label='Toggle theme'></button></div></div></header>"
                + "<main class='auth-wrapper' style='padding:3.5rem 1rem;'><div class='card auth-card' style='max-width:520px;'>"
                + "<div class='burned-box'><div class='flame-icon'>🔥</div><h1 style='font-size:1.5rem; color:var(--danger); margin-bottom:0.5rem;'>This note has been burned</h1>"
                + "<p style='font-size:0.925rem; color:var(--text-secondary); max-width:380px; margin:0 auto 1.5rem auto;'>" + reason + " The ciphertext has been permanently purged from volatile RAM and the link is dead.</p>"
                + "<div style='margin-bottom:1.5rem; font-size:0.8rem; color:var(--text-muted);'>Token: <code class='code-font' style='color:var(--danger);'>" + token + "</code></div>"
                + "<a href='/' class='btn btn-secondary btn-sm'>Return to Vanish Home</a></div></div></main>"
                + "<footer><div class='container footer-content'><div>&copy; 2026 Vanish &bull; Military-grade One-Time Secret Handoff</div><div><span>Zero Knowledge &bull; End-to-End In-Memory Encryption</span></div></div></footer></body></html>";
        sendResponse(exchange, 200, "text/html", html);
    }

    private static void renderExpiredScreen(HttpExchange exchange, String token, String reason) throws IOException {
        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Note Expired — Vanish</title><link rel='stylesheet' href='/css/style.css'><script src='/js/theme.js'></script></head><body>"
                + "<header class='navbar'><div class='container nav-wrapper'><a href='/' class='brand'><span class='brand-icon'>⚡</span><span>VANISH</span></a>"
                + "<div class='nav-right'><button type='button' class='theme-toggle-btn' aria-label='Toggle theme'></button></div></div></header>"
                + "<main class='auth-wrapper' style='padding:3.5rem 1rem;'><div class='card auth-card' style='max-width:520px;'>"
                + "<div class='burned-box' style='border-color:var(--border-subtle);'><div class='flame-icon' style='filter:grayscale(1);'>⏳</div><h1 style='font-size:1.5rem; color:var(--text-muted); margin-bottom:0.5rem;'>This note has expired</h1>"
                + "<p style='font-size:0.925rem; color:var(--text-secondary); max-width:380px; margin:0 auto 1.5rem auto;'>" + reason + " The self-destruct timer set by the sender elapsed before this link was opened.</p>"
                + "<div style='margin-bottom:1.5rem; font-size:0.8rem; color:var(--text-muted);'>Token: <code class='code-font' style='color:var(--text-muted);'>" + token + "</code></div>"
                + "<a href='/' class='btn btn-secondary btn-sm'>Return to Vanish Home</a></div></div></main>"
                + "<footer><div class='container footer-content'><div>&copy; 2026 Vanish &bull; Military-grade One-Time Secret Handoff</div><div><span>Zero Knowledge &bull; End-to-End In-Memory Encryption</span></div></div></footer></body></html>";
        sendResponse(exchange, 200, "text/html", html);
    }

    private static void renderPendingScreen(HttpExchange exchange, String token) throws IOException {
        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Waiting for Approval — Vanish</title><link rel='stylesheet' href='/css/style.css'><script src='/js/theme.js'></script></head><body>"
                + "<header class='navbar'><div class='container nav-wrapper'><a href='/' class='brand'><span class='brand-icon'>⚡</span><span>VANISH</span></a>"
                + "<div class='nav-right'><button type='button' class='theme-toggle-btn' aria-label='Toggle theme'></button></div></div></header>"
                + "<main class='auth-wrapper' style='padding:3.5rem 1rem;'><div class='card auth-card' style='max-width:520px;'>"
                + "<div class='burned-box' style='border-color:var(--border-subtle);'><div class='flame-icon'>⏳</div><h1 style='font-size:1.5rem; color:var(--text-muted); margin-bottom:0.5rem;'>Waiting for admin approval</h1>"
                + "<p style='font-size:0.925rem; color:var(--text-secondary); max-width:380px; margin:0 auto 1.5rem auto;'>The sender's note is being reviewed by the Vanish admin. Nothing has been revealed or burned. Open this link again after it is approved.</p>"
                + "<div style='margin-bottom:1.5rem; font-size:0.8rem; color:var(--text-muted);'>Token: <code class='code-font' style='color:var(--text-muted);'>" + escapeHtml(token) + "</code></div>"
                + "<a href='/' class='btn btn-secondary btn-sm'>Return to Vanish Home</a></div></div></main>"
                + "<footer><div class='container footer-content'><div>&copy; 2026 Vanish &bull; Military-grade One-Time Secret Handoff</div><div><span>Zero Knowledge &bull; End-to-End In-Memory Encryption</span></div></div></footer></body></html>";
        sendResponse(exchange, 200, "text/html", html);
    }

    private static void renderErrorScreen(HttpExchange exchange, String message) throws IOException {
        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Notice — Vanish</title><link rel='stylesheet' href='/css/style.css'><script src='/js/theme.js'></script></head><body>"
                + "<header class='navbar'><div class='container nav-wrapper'><a href='/' class='brand'><span class='brand-icon'>⚡</span><span>VANISH</span></a>"
                + "<div class='nav-right'><button type='button' class='theme-toggle-btn' aria-label='Toggle theme'></button></div></div></header>"
                + "<main class='auth-wrapper' style='padding:3.5rem 1rem;'><div class='card auth-card' style='max-width:500px; text-align:center;'>"
                + "<div style='font-size:2.5rem; margin-bottom:0.75rem;'>🔍</div><h1 style='font-size:1.4rem; margin-bottom:0.5rem;'>Note Not Found</h1>"
                + "<p style='font-size:0.925rem; color:var(--text-secondary); margin-bottom:1.5rem;'>" + message + "</p>"
                + "<a href='/' class='btn btn-primary btn-sm'>Return to Home</a></div></main>"
                + "<footer><div class='container footer-content'><div>&copy; 2026 Vanish &bull; Military-grade One-Time Secret Handoff</div><div><span>Zero Knowledge &bull; End-to-End In-Memory Encryption</span></div></div></footer></body></html>";
        sendResponse(exchange, 404, "text/html", html);
    }

    // --- Admin ---

    private static boolean requireAdmin(HttpExchange exchange, SessionData session) throws IOException {
        if (session == null || session.isExpired()) {
            redirect(exchange, "/login.html?error=" + urlEncode("Access restricted. Please log in first."));
            return false;
        }
        session.touch();
        if (!session.user.isAdmin()) {
            sendResponse(exchange, 403, "text/html", "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><title>403 Forbidden — Vanish</title>"
                    + "<link rel='stylesheet' href='/css/style.css'></head><body><main class='container' style='padding-top:2.5rem;'>"
                    + "<div class='card'><h1>403 Forbidden</h1><div class='alert alert-danger'>Administrator access required.</div>"
                    + "<a href='/dashboard' class='btn btn-secondary'>Back to Dashboard</a></div></main></body></html>");
            return false;
        }
        return true;
    }

    private static void handleAdmin(HttpExchange exchange, SessionData session, String path, String method) throws IOException {
        if (!requireAdmin(exchange, session)) return;
        boolean post = "POST".equals(method);
        try {
            if (path.equals("/admin") && !post) {
                renderAdminOverview(exchange, session.user);
            } else if (path.equals("/admin/users") && !post) {
                renderAdminUsers(exchange, session.user);
            } else if (path.equals("/admin/notes") && !post) {
                renderAdminNotes(exchange, session.user);
            } else if (path.equals("/admin/approvals") && !post) {
                renderAdminApprovals(exchange, session.user);
            } else if (path.equals("/admin/users/delete") && post) {
                String name = parseFormData(exchange).get("username");
                adminService.deleteUser(name);
                if (name != null) {
                    activeSessions.values().removeIf(s -> name.equals(s.user.getUsername()));
                }
                redirect(exchange, "/admin/users?msg=" + urlEncode("User deleted."));
            } else if (path.equals("/admin/notes/delete") && post) {
                adminService.deleteNote(parseFormData(exchange).get("token"));
                redirect(exchange, "/admin/notes?msg=" + urlEncode("Note deleted."));
            } else if (path.equals("/admin/notes/burn") && post) {
                adminService.burnNote(parseFormData(exchange).get("token"));
                redirect(exchange, "/admin/notes?msg=" + urlEncode("Note burned."));
            } else if (path.equals("/admin/notes/approve") && post) {
                adminService.approveNote(parseFormData(exchange).get("token"));
                redirect(exchange, "/admin/approvals?msg=" + urlEncode("Note approved."));
            } else if (path.equals("/admin/notes/reject") && post) {
                adminService.rejectNote(parseFormData(exchange).get("token"));
                redirect(exchange, "/admin/approvals?msg=" + urlEncode("Note rejected."));
            } else {
                sendResponse(exchange, 404, "text/html", "<h1>404 Not Found</h1>");
            }
        } catch (InvalidInputException | NoteNotFoundException e) {
            String back = path.startsWith("/admin/users") ? "/admin/users"
                    : (path.equals("/admin/notes/approve") || path.equals("/admin/notes/reject")) ? "/admin/approvals" : "/admin/notes";
            redirect(exchange, back + "?error=" + urlEncode(e.getMessage()));
        }
    }

    private static void adminPage(HttpExchange exchange, User admin, String title, String active, String body) throws IOException {
        Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
        String alerts = "";
        int pending = adminService.listPendingNotes().size();
        if (q.get("msg") != null) alerts += "<div class='alert alert-success'><div>" + escapeHtml(q.get("msg")) + "</div></div>";
        if (q.get("error") != null) alerts += "<div class='alert alert-danger'><div>" + escapeHtml(q.get("error")) + "</div></div>";
        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>" + title + " — Vanish Admin</title><link rel='stylesheet' href='/css/style.css'><script src='/js/theme.js'></script></head><body>"
                + "<header class='navbar'><div class='container nav-wrapper'><a href='/' class='brand'><span class='brand-icon'>⚡</span><span>VANISH</span></a>"
                + "<div class='nav-right'><nav><ul class='nav-links'>"
                + "<li><a href='/admin' class='nav-link" + ("admin".equals(active) ? " active" : "") + "'>Admin</a></li>"
                + "<li><a href='/admin/approvals' class='nav-link" + ("approvals".equals(active) ? " active" : "") + "'>Approvals" + (pending > 0 ? " (" + pending + ")" : "") + "</a></li>"
                + "<li><a href='/admin/users' class='nav-link" + ("users".equals(active) ? " active" : "") + "'>Users</a></li>"
                + "<li><a href='/admin/notes' class='nav-link" + ("notes".equals(active) ? " active" : "") + "'>Notes</a></li>"
                + "<li><div class='user-badge'><span class='user-avatar'>👤</span><span>" + escapeHtml(admin.getUsername()) + "</span></div></li>"
                + "<li><form action='/logout' method='POST' style='display:inline;'><button type='submit' class='btn btn-secondary btn-sm'>Sign Out</button></form></li></ul></nav>"
                + "<button type='button' class='theme-toggle-btn' aria-label='Toggle theme'></button></div></div></header>"
                + "<main class='container' style='padding-top:2.5rem; padding-bottom:3.5rem; flex:1;'>" + alerts + body + "</main>"
                + "<div class='toast-container'></div></body></html>";
        sendResponse(exchange, 200, "text/html", html);
    }

    private static String statusBadge(NoteStatus s) {
        String cls = switch (s) {
            case PENDING -> "badge-pending";
            case UNREAD -> "badge-unread";
            case BURNED -> "badge-burned";
            case EXPIRED -> "badge-expired";
            case REJECTED -> "badge-rejected";
        };
        return "<span class='badge " + cls + "'>" + s + "</span>";
    }

    private static String postForm(String action, String field, String value, String label, String cls, String confirmMsg) {
        return "<form action='" + action + "' method='POST' style='display:inline;'" + (confirmMsg != null ? " onsubmit=\"return confirm('" + confirmMsg + "');\"" : "")
                + "><input type='hidden' name='" + field + "' value='" + escapeHtml(value) + "'><button type='submit' class='btn " + cls + " btn-sm'>" + label + "</button></form> ";
    }

    private static void renderAdminOverview(HttpExchange exchange, User admin) throws IOException {
        AdminService.Stats st = adminService.getStats();
        String[][] cards = {{"Users", "" + st.users(), null}, {"Notes", "" + st.notes(), null}, {"Pending approval", "" + st.pending(), "/admin/approvals"},
                {"Unread", "" + st.unread(), null}, {"Burned", "" + st.burned(), null}, {"Expired", "" + st.expired(), null}, {"Rejected", "" + st.rejected(), null}};
        StringBuilder sb = new StringBuilder("<h1 style='margin-bottom:1.5rem;'>Admin Overview</h1><div style='display:grid; grid-template-columns:repeat(auto-fit, minmax(160px, 1fr)); gap:1rem; margin-bottom:2rem;'>");
        for (String[] c : cards) {
            String card = "<div class='card'><div style='color:var(--text-muted); font-size:0.8rem;'>" + c[0] + "</div><div style='font-size:2rem; font-weight:700;'>" + c[1] + "</div></div>";
            sb.append(c[2] != null ? "<a href='" + c[2] + "' style='text-decoration:none; color:inherit;'>" + card + "</a>" : card);
        }
        sb.append("</div><div class='grid-2'><a href='/admin/users' class='btn btn-primary'>Manage Users</a><a href='/admin/notes' class='btn btn-secondary'>Manage Notes</a></div>");
        adminPage(exchange, admin, "Overview", "admin", sb.toString());
    }

    private static void renderAdminUsers(HttpExchange exchange, User admin) throws IOException {
        Map<String, Integer> owned = new HashMap<>();
        for (Note n : adminService.listNotes()) owned.merge(n.getOwner(), 1, Integer::sum);
        StringBuilder rows = new StringBuilder();
        for (User u : adminService.listUsers()) {
            rows.append("<tr><td>").append(escapeHtml(u.getUsername())).append("</td><td><span class='badge ")
                    .append(u.isAdmin() ? "badge-burned" : "badge-unread").append("'>").append(u.getRole()).append("</span></td><td>")
                    .append(u.getRegisteredAt().format(Note.STAMP)).append("</td><td>").append(owned.getOrDefault(u.getUsername(), 0)).append("</td><td>");
            if (!u.isAdmin()) {
                rows.append(postForm("/admin/users/delete", "username", u.getUsername(), "Delete", "btn-secondary", "Delete this user?"));
            }
            rows.append("</td></tr>");
        }
        adminPage(exchange, admin, "Users", "users", "<div class='card'><div class='card-header'><h2 class='card-title'>Users</h2></div><div class='table-responsive'><table class='table'><thead><tr>"
                + "<th>Username</th><th>Role</th><th>Registered</th><th>Notes</th><th>Action</th></tr></thead><tbody>" + rows + "</tbody></table></div></div>");
    }

    private static void renderAdminNotes(HttpExchange exchange, User admin) throws IOException {
        StringBuilder rows = new StringBuilder();
        for (Note n : adminService.listNotes()) {
            rows.append("<tr><td>#").append(n.getId()).append("</td><td><code class='code-font'>").append(escapeHtml(n.getToken())).append("</code></td><td>")
                    .append(escapeHtml(n.getOwner())).append("</td><td>").append(statusBadge(n.getStatus())).append("</td><td>").append(n.getViewsLeft()).append("</td><td>")
                    .append(n.getCreatedAt().format(Note.STAMP)).append("</td><td>").append(n.getExpiresAt().format(Note.STAMP)).append("</td><td>")
                    .append(n.getReadAt() != null ? n.getReadAt().format(Note.STAMP) : "-").append("</td><td>");
            if (n.isPending()) {
                rows.append(postForm("/admin/notes/approve", "token", n.getToken(), "Approve", "btn-primary", null));
                rows.append(postForm("/admin/notes/reject", "token", n.getToken(), "Reject", "btn-secondary", "Reject this note? Its ciphertext will be destroyed."));
            }
            if (n.getStatus() != NoteStatus.BURNED && !n.isRejected()) {
                rows.append(postForm("/admin/notes/burn", "token", n.getToken(), "Burn", "btn-primary", null));
            }
            rows.append(postForm("/admin/notes/delete", "token", n.getToken(), "Delete", "btn-secondary", "Delete this note?")).append("</td></tr>");
        }
        adminPage(exchange, admin, "Notes", "notes", "<div class='card'><div class='card-header'><h2 class='card-title'>Notes</h2></div><div class='table-responsive'><table class='table'><thead><tr>"
                + "<th>ID</th><th>Token</th><th>Owner</th><th>Status</th><th>Views Left</th><th>Created</th><th>Expires</th><th>Read At</th><th>Action</th></tr></thead><tbody>" + rows + "</tbody></table></div></div>");
    }

    private static void renderAdminApprovals(HttpExchange exchange, User admin) throws IOException {
        StringBuilder rows = new StringBuilder();
        for (Note n : adminService.listPendingNotes()) {
            rows.append("<tr><td>#").append(n.getId()).append("</td><td><code class='code-font'>").append(escapeHtml(n.getToken())).append("</code></td><td>")
                    .append(escapeHtml(n.getOwner())).append("</td><td>").append(n.getCreatedAt().format(Note.STAMP)).append("</td><td>").append(n.getViewsLeft()).append("</td><td>")
                    .append(postForm("/admin/notes/approve", "token", n.getToken(), "Approve", "btn-primary", null))
                    .append(postForm("/admin/notes/reject", "token", n.getToken(), "Reject", "btn-secondary", "Reject this note? Its ciphertext will be destroyed."))
                    .append("</td></tr>");
        }
        if (rows.length() == 0) {
            rows.append("<tr><td colspan='6' style='text-align:center; padding:2rem; color:var(--text-muted);'>No notes waiting. All clear.</td></tr>");
        }
        adminPage(exchange, admin, "Approvals", "approvals", "<div class='card'><div class='card-header'><h2 class='card-title'>Approval Queue</h2>"
                + "<p class='card-subtitle'>Approve blind: secret content is never shown to the admin.</p></div><div class='table-responsive'><table class='table'><thead><tr>"
                + "<th>ID</th><th>Token</th><th>Sender</th><th>Created</th><th>Views</th><th>Action</th></tr></thead><tbody>" + rows + "</tbody></table></div></div>");
    }

    // --- Helpers ---

    private static SessionData getSession(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] pair = cookie.trim().split("=", 2);
                if (pair.length == 2 && "VANISH_SESSION_ID".equals(pair[0])) {
                    SessionData s = activeSessions.get(pair[1]);
                    if (s != null && userService.findByUsername(s.user.getUsername()) == null) {
                        activeSessions.remove(s.sessionId);
                        return null;
                    }
                    return s;
                }
            }
        }
        return null;
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private static boolean insideWebRoot(Path filePath) {
        Path root = webRoot.toAbsolutePath().normalize();
        Path p = filePath.toAbsolutePath().normalize();
        return p.startsWith(root) && !p.toString().toUpperCase(Locale.ROOT).contains("WEB-INF");
    }

    private static void serveFile(HttpExchange exchange, Path filePath, String contentType) throws IOException {
        if (!insideWebRoot(filePath) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendResponse(exchange, 404, "text/html", "<h1>404 Not Found</h1>");
            return;
        }
        byte[] bytes = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(statusCode, -1);
            return;
        }
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int read;
        while ((read = reader.read(buf)) > 0) {
            sb.append(buf, 0, read);
        }
        return parseQuery(sb.toString());
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return map;
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                try {
                    String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name());
                    map.put(key, value);
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        }
        return map;
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
