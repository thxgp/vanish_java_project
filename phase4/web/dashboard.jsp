<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.vanish.model.User"%>
<%@ page import="com.vanish.model.Note"%>
<%@ page import="com.vanish.model.NoteStatus"%>
<%@ page import="java.util.List"%>
<%@ page import="com.vanish.web.Html"%>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login.html?error=" + java.net.URLEncoder.encode("Authentication required. Please sign in.", "UTF-8"));
        return;
    }

    @SuppressWarnings("unchecked")
    List<Note> notes = (List<Note>) request.getAttribute("notes");
    String createdToken = request.getParameter("created");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Dashboard — Vanish</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
  <script src="<%= request.getContextPath() %>/js/theme.js"></script>
</head>
<body data-current-user="<%= Html.esc(user.getUsername()) %>">

  <!-- Navigation Bar -->
  <header class="navbar">
    <div class="container nav-wrapper">
      <a href="<%= request.getContextPath() %>/index.html" class="brand">
        <span class="brand-icon">⚡</span>
        <span>VANISH</span>
      </a>

      <div class="nav-right">
        <nav>
          <ul class="nav-links">
            <li><a href="<%= request.getContextPath() %>/dashboard" class="nav-link active">Dashboard</a></li>
            <li><a href="<%= request.getContextPath() %>/compose" class="nav-link">Compose</a></li>
            <% if (user.isAdmin()) { %><li><a href="<%= request.getContextPath() %>/admin" class="nav-link">Admin</a></li><% } %>
            <li>
              <div class="user-badge">
                <span class="user-avatar">👤</span>
                <span><%= Html.esc(user.getUsername()) %></span>
              </div>
            </li>
            <li>
              <form action="<%= request.getContextPath() %>/logout" method="POST" style="display:inline;">
                <button type="submit" class="btn btn-secondary btn-sm">Sign Out</button>
              </form>
            </li>
          </ul>
        </nav>
        <button type="button" class="theme-toggle-btn" aria-label="Toggle theme"></button>
      </div>
    </div>
  </header>

  <!-- Main Container -->
  <main class="container" style="padding-top: 2.5rem; padding-bottom: 3.5rem; flex: 1;">
    
    <!-- Flash Banner on new note creation -->
    <% if (createdToken != null && !createdToken.isEmpty()) { 
         String shareUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/read?t=" + createdToken;
    %>
      <div class="alert alert-success" style="margin-bottom: 1.5rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%; flex-wrap: wrap; gap: 0.75rem;">
          <div>
            <strong>Secret Note Created!</strong>
            <%= user.isAdmin() ? "Single-use link:" : "Waiting for admin approval before the link works." %>
            <code class="code-font" style="color: var(--text-primary); font-weight: bold; margin-left: 0.5rem;"><%= Html.esc(shareUrl) %></code>
          </div>
          <button type="button" class="btn btn-primary btn-sm copy-btn" data-url="<%= Html.esc(shareUrl) %>">Copy Link</button>
        </div>
      </div>
    <% } %>

    <!-- User Header & Quick Actions -->
    <div class="card" style="margin-bottom: 2rem;">
      <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
        <div style="display: flex; align-items: center; gap: 1rem;">
          <div class="user-avatar" style="width: 48px; height: 48px; font-size: 1.35rem;">👤</div>
          <div>
            <h1 style="font-size: 1.5rem; margin: 0;">Welcome, <%= Html.esc(user.getUsername()) %></h1>
            <p style="font-size: 0.85rem; margin: 0; color: var(--text-secondary);">
              All notes are encrypted with AES-256 and kept exclusively in volatile memory.
            </p>
          </div>
        </div>

        <div style="display: flex; gap: 0.75rem;">
          <a href="#quickComposer" class="btn btn-primary btn-sm">+ Compose Note</a>
          <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-secondary btn-sm">↻ Refresh Ledger</a>
        </div>
      </div>
    </div>

    <!-- Quick Note Composer -->
    <div id="quickComposer" class="card" style="margin-bottom: 2rem;">
      <div class="card-header" style="margin-bottom: 1rem;">
        <h2 class="card-title" style="font-size: 1.25rem;">New Secret Note</h2>
        <p class="card-subtitle">Encrypted directly in RAM &bull; Burned immediately once read</p>
      </div>

      <form action="<%= request.getContextPath() %>/compose" method="POST" id="composeNoteForm">
        <div class="form-group">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.35rem;">
            <label for="secretText" class="form-label" style="margin: 0;">Secret Content</label>
            <span id="charCounter" style="font-size: 0.775rem; color: var(--text-muted); font-family: var(--font-mono);">0 / 500 chars</span>
          </div>
          <textarea id="secretText" name="secretText" class="form-control" rows="3" placeholder="Enter sensitive secret, password, private key..." required maxlength="500"></textarea>
          <div id="secretTextError" class="error-feedback"></div>
        </div>

        <div class="grid-2">
          <div class="form-group">
            <label for="expiryMinutes" class="form-label">Expiry Duration</label>
            <select id="expiryMinutes" name="expiryMinutes" class="form-control">
              <option value="5">5 Minutes</option>
              <option value="10" selected>10 Minutes</option>
              <option value="60">1 Hour</option>
              <option value="1440">24 Hours</option>
            </select>
          </div>

          <div class="form-group">
            <label for="views" class="form-label">Permitted Views</label>
            <select id="views" name="views" class="form-control">
              <option value="1" selected>1 View (Standard Burn)</option>
              <option value="2">2 Views</option>
              <option value="3">3 Views</option>
              <option value="5">5 Views</option>
            </select>
          </div>
        </div>

        <button type="submit" id="composeSubmitBtn" class="btn btn-primary">Generate Secret Note Link</button>
      </form>
    </div>

    <!-- Notes Ledger Card -->
    <div class="card">
      <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
        <div>
          <h2 class="card-title">Secret Notes Ledger</h2>
          <p class="card-subtitle">Track whether your recipients have accessed their secrets. Secrets themselves are never shown.</p>
        </div>
        <div>
          <input type="text" id="filterInput" class="form-control" placeholder="Search tokens or status..." style="padding: 0.45rem 0.85rem; font-size: 0.85rem; width: 220px;">
        </div>
      </div>

      <div class="table-responsive">
        <table class="table" id="notesTable">
          <thead>
            <tr>
              <th>ID</th>
              <th>Token</th>
              <th>Status</th>
              <th>Views Left</th>
              <th>Created At</th>
              <th>Expires At</th>
              <th>Read Timestamp</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <% if (notes == null || notes.isEmpty()) { %>
              <tr>
                <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                  No secret notes created yet. Use the composer above to generate your first link!
                </td>
              </tr>
            <% } else { 
                for (Note n : notes) {
                  String badgeClass = "badge-unread";
                  if (n.getStatus() == NoteStatus.PENDING) {
                    badgeClass = "badge-pending";
                  } else if (n.getStatus() == NoteStatus.BURNED) {
                    badgeClass = "badge-burned";
                  } else if (n.getStatus() == NoteStatus.EXPIRED) {
                    badgeClass = "badge-expired";
                  } else if (n.getStatus() == NoteStatus.REJECTED) {
                    badgeClass = "badge-rejected";
                  }
                  String linkUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/read?t=" + n.getToken();
            %>
              <tr>
                <td>#<%= n.getId() %></td>
                <td><code class="code-font" style="color: var(--primary); font-weight: 600;"><%= Html.esc(n.getToken()) %></code></td>
                <td><span class="badge <%= badgeClass %>"><%= Html.esc(String.valueOf(n.getStatus())) %></span></td>
                <td><%= n.getViewsLeft() %></td>
                <td style="color: var(--text-secondary);"><%= n.getCreatedAt().format(Note.STAMP) %></td>
                <td style="color: var(--text-secondary);"><%= n.getExpiresAt().format(Note.STAMP) %></td>
                <td style="color: <%= n.getReadAt() != null ? "var(--danger)" : "var(--text-muted)" %>;">
                  <%= n.getReadAt() == null ? "—" : n.getReadAt().format(Note.STAMP) %>
                </td>
                <td>
                  <div style="display: flex; gap: 0.4rem;">
                    <button type="button" class="btn btn-secondary btn-sm copy-btn" data-url="<%= Html.esc(linkUrl) %>">Copy</button>
                    <a href="<%= Html.esc(linkUrl) %>" class="btn btn-primary btn-sm" target="_blank">Open</a>
                  </div>
                </td>
              </tr>
            <%  } 
               } %>
          </tbody>
        </table>
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

  <script src="<%= request.getContextPath() %>/js/validation.js"></script>
  <script src="<%= request.getContextPath() %>/js/compose.js"></script>
  <script>
    document.addEventListener('DOMContentLoaded', () => {
      document.querySelectorAll('.copy-btn').forEach(btn => {
        btn.addEventListener('click', () => {
          const url = btn.dataset.url;
          navigator.clipboard.writeText(url).then(() => {
            showToast('Secret link copied to clipboard!', 'success');
          });
        });
      });

      const filterInput = document.getElementById('filterInput');
      const tableRows = document.querySelectorAll('#notesTable tbody tr');
      if (filterInput) {
        filterInput.addEventListener('input', () => {
          const q = filterInput.value.toLowerCase();
          tableRows.forEach(row => {
            row.style.display = row.textContent.toLowerCase().includes(q) ? '' : 'none';
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
