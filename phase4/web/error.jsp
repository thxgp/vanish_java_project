<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
    if (errorMessage == null) {
        errorMessage = "The requested resource or note could not be found.";
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Error — Vanish</title>
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
    <div class="card auth-card" style="max-width: 500px; text-align: center;">
      <div style="font-size: 2.5rem; margin-bottom: 0.75rem;">🔍</div>
      <h1 style="font-size: 1.4rem; margin-bottom: 0.5rem;">Note Not Found</h1>
      <p style="font-size: 0.925rem; color: var(--text-secondary); margin-bottom: 1.5rem;">
        <%= errorMessage %>
      </p>
      <a href="<%= request.getContextPath() %>/index.html" class="btn btn-primary btn-sm">Return to Home</a>
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
