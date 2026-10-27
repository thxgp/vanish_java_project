<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.vanish.web.Html"%>
<%
    String token = (String) request.getAttribute("token");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Waiting for Approval — Vanish</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
  <script src="<%= request.getContextPath() %>/js/theme.js"></script>
</head>
<body>

  <header class="navbar">
    <div class="container nav-wrapper">
      <a href="<%= request.getContextPath() %>/index.html" class="brand">
        <span class="brand-icon">⚡</span>
        <span>VANISH</span>
      </a>
      <div class="nav-right">
        <button type="button" class="theme-toggle-btn" aria-label="Toggle theme"></button>
      </div>
    </div>
  </header>

  <main class="auth-wrapper" style="padding: 3.5rem 1rem;">
    <div class="card auth-card" style="max-width: 520px;">
      <div class="burned-box" style="border-color: var(--border-subtle);">
        <div class="flame-icon" style="filter: grayscale(1);">⏳</div>
        <h1 style="font-size: 1.5rem; color: var(--text-muted); margin-bottom: 0.5rem;">Waiting for admin approval</h1>
        <p style="font-size: 0.925rem; color: var(--text-secondary); max-width: 380px; margin: 0 auto 1.5rem auto;">
          The sender's note is being reviewed by the Vanish admin. Nothing has been revealed or burned. Open this link again after it is approved.
        </p>
        <% if (token != null) { %>
          <div style="margin-bottom: 1.5rem; font-size: 0.8rem; color: var(--text-muted);">
            Token: <code class="code-font" style="color: var(--text-muted);"><%= Html.esc(token) %></code>
          </div>
        <% } %>
        <a href="<%= request.getContextPath() %>/index.html" class="btn btn-secondary btn-sm">Return to Vanish Home</a>
      </div>
    </div>
  </main>

  <footer>
    <div class="container footer-content">
      <div>&copy; 2026 Vanish &bull; Military-grade One-Time Secret Handoff</div>
      <div><span>Zero Knowledge &bull; End-to-End In-Memory Encryption</span></div>
    </div>
  </footer>

</body>
</html>
