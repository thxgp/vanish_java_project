<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.vanish.model.Note"%>
<%@ page import="java.util.List"%>
<%
    @SuppressWarnings("unchecked")
    List<Note> pending = (List<Note>) request.getAttribute("pending");
    String adminTitle = "Approvals";
    String adminActive = "approvals";
%>
<%@ include file="/WEB-INF/admin_header.jspf" %>
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Approval Queue</h2>
        <p class="card-subtitle">Notes wait here until you approve or reject them. Secrets are never shown.</p>
      </div>
      <% if (pending.isEmpty()) { %>
        <p style="text-align: center; padding: 2.5rem; color: var(--text-muted);">No notes waiting. All clear.</p>
      <% } else { %>
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr><th>ID</th><th>Token</th><th>Sender</th><th>Created</th><th>Views</th><th>Actions</th></tr>
          </thead>
          <tbody>
            <% for (Note n : pending) { %>
              <tr>
                <td>#<%= Html.esc(String.valueOf(n.getId())) %></td>
                <td><code class="code-font"><%= Html.esc(n.getToken()) %></code></td>
                <td><%= Html.esc(n.getOwner()) %></td>
                <td style="color: var(--text-secondary);"><%= Html.esc(n.getCreatedAt().format(Note.STAMP)) %></td>
                <td><%= n.getViewsLeft() %></td>
                <td>
                  <div style="display: flex; gap: 0.4rem;">
                    <form action="<%= Html.esc(ctx) %>/admin/notes/approve" method="POST" style="display:inline;">
                      <input type="hidden" name="token" value="<%= Html.esc(n.getToken()) %>">
                      <button type="submit" class="btn btn-primary btn-sm">Approve</button>
                    </form>
                    <form action="<%= Html.esc(ctx) %>/admin/notes/reject" method="POST" style="display:inline;"
                          onsubmit="return confirm('Reject this note? Its content will be destroyed.');">
                      <input type="hidden" name="token" value="<%= Html.esc(n.getToken()) %>">
                      <button type="submit" class="btn btn-secondary btn-sm">Reject</button>
                    </form>
                  </div>
                </td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
      <% } %>
    </div>
<%@ include file="/WEB-INF/admin_footer.jspf" %>
