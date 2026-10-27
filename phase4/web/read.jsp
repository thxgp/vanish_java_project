<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String secret = (String) request.getAttribute("secret");
    String token = (String) request.getAttribute("token");
    if (secret == null) {
        response.sendRedirect(request.getContextPath() + "/index.html");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Secret Revealed — Vanish</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
  <script src="<%= request.getContextPath() %>/js/theme.js"></script>
</head>
<body>

  <!-- Minimal Header -->
  <header class="navbar">
    <div class="container nav-wrapper">
      <a href="<%= request.getContextPath() %>/index.html" class="brand">
        <span class="brand-icon">⚡</span>
        <span>VANISH</span>
      </a>
      <div class="nav-right">
        <span style="font-size: 0.85rem; color: var(--text-muted);">Encrypted Handoff</span>
        <button type="button" class="theme-toggle-btn" aria-label="Toggle theme"></button>
      </div>
    </div>
  </header>

  <main class="auth-wrapper" style="padding: 3.5rem 1rem;">
    <div class="card auth-card" style="max-width: 580px;">
      
      <div class="card-header" style="margin-bottom: 1rem;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h1 class="card-title" style="font-size: 1.35rem; color: var(--success);">Secret Revealed</h1>
          <span class="badge badge-unread">Final View</span>
        </div>
        <p class="card-subtitle">
          Token: <code class="code-font" style="color: var(--primary);"><%= token %></code> &bull; Decrypted from memory
        </p>
      </div>

      <div class="secret-box">
        <div style="font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.08em; color: var(--text-muted); margin-bottom: 0.35rem;">
          Decrypted Content:
        </div>
        <div class="secret-text" id="revealedSecretText"><%= secret %></div>
      </div>

      <div class="alert alert-warning" style="font-size: 0.85rem; margin-bottom: 1.5rem;">
        <div>
          <strong>Note:</strong> This message has been purged from server memory. Refreshing or leaving this page permanently destroys it.
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem;">
        <button type="button" id="copySecretBtn" class="btn btn-primary btn-block">Copy Secret</button>
        <a href="<%= request.getContextPath() %>/index.html" class="btn btn-secondary btn-block">Done</a>
      </div>

    </div>
  </main>

  <div class="toast-container"></div>

  <!-- Footer -->
  <footer>
    <div class="container footer-content">
      <div>&copy; 2026 Vanish &bull; Military-grade One-Time Secret Handoff</div>
      <div><span>Zero Knowledge &bull; End-to-End In-Memory Encryption</span></div>
    </div>
  </footer>

  <script>
    document.addEventListener('DOMContentLoaded', () => {
      const copyBtn = document.getElementById('copySecretBtn');
      const textEl = document.getElementById('revealedSecretText');
      if (copyBtn && textEl) {
        copyBtn.addEventListener('click', () => {
          navigator.clipboard.writeText(textEl.textContent.trim()).then(() => {
            showToast('Secret message copied to clipboard!', 'success');
          });
        });
      }
    });

    function showToast(msg, type = 'info') {
      let c = document.querySelector('.toast-container');
      if (!c) {
        c = document.createElement('div');
        c.className = 'toast-container';
        document.body.appendChild(c);
      }
      const t = document.createElement('div');
      t.className = `toast toast-${type}`;
      t.textContent = msg;
      c.appendChild(t);
      setTimeout(() => {
        t.style.opacity = '0';
        setTimeout(() => t.remove(), 300);
      }, 3000);
    }
  </script>
</body>
</html>
