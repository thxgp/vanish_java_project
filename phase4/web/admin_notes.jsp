<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.vanish.model.Note"%>
<%@ page import="com.vanish.model.NoteStatus"%>
<%@ page import="java.util.List"%>
<%
    @SuppressWarnings("unchecked")
    List<Note> notes = (List<Note>) request.getAttribute("notes");
    String adminTitle = "Notes";
    String adminActive = "notes";
%>
<%@ include file="/WEB-INF/admin_header.jspf" %>
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Notes</h2>
        <p class="card-subtitle">Secrets are never shown.</p>
      </div>
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr><th>ID</th><th>Token</th><th>Owner</th><th>Status</th><th>Views Left</th><th>Created</th><th>Expires</th><th>Read At</th><th>Actions</th></tr>
          </thead>
          <tbody>
            <% for (Note n : notes) {
                 String badgeClass = "badge-unread";
                 if (n.getStatus() == NoteStatus.PENDING) badgeClass = "badge-pending";
                 else if (n.getStatus() == NoteStatus.BURNED) badgeClass = "badge-burned";
                 else if (n.getStatus() == NoteStatus.EXPIRED) badgeClass = "badge-expired";
                 else if (n.getStatus() == NoteStatus.REJECTED) badgeClass = "badge-rejected"; %>
              <tr>
                <td>#<%= Html.esc(String.valueOf(n.getId())) %></td>
                <td><code class="code-font"><%= Html.esc(n.getToken()) %></code></td>
                <td><%= Html.esc(n.getOwner()) %></td>
                <td><span class="badge <%= badgeClass %>"><%= Html.esc(String.valueOf(n.getStatus())) %></span></td>
                <td><%= n.getViewsLeft() %></td>
                <td style="color: var(--text-secondary);"><%= Html.esc(n.getCreatedAt().format(Note.STAMP)) %></td>
                <td style="color: var(--text-secondary);"><%= Html.esc(n.getExpiresAt().format(Note.STAMP)) %></td>
                <td style="color: var(--text-secondary);"><%= n.getReadAt() == null ? "—" : Html.esc(n.getReadAt().format(Note.STAMP)) %></td>
                <td>
                  <div style="display: flex; gap: 0.4rem;">
                    <% if (n.getStatus() == NoteStatus.PENDING) { %>
                      <form action="<%= Html.esc(ctx) %>/admin/notes/approve" method="POST" style="display:inline;">
                        <input type="hidden" name="token" value="<%= Html.esc(n.getToken()) %>">
                        <button type="submit" class="btn btn-primary btn-sm">Approve</button>
                      </form>
                      <form action="<%= Html.esc(ctx) %>/admin/notes/reject" method="POST" style="display:inline;"
                            onsubmit="return confirm('Reject this note? Its content will be destroyed.');">
                        <input type="hidden" name="token" value="<%= Html.esc(n.getToken()) %>">
                        <button type="submit" class="btn btn-secondary btn-sm">Reject</button>
                      </form>
                    <% } %>
                    <% if (n.getStatus() != NoteStatus.BURNED && n.getStatus() != NoteStatus.REJECTED) { %>
                      <form action="<%= Html.esc(ctx) %>/admin/notes/burn" method="POST" style="display:inline;">
                        <input type="hidden" name="token" value="<%= Html.esc(n.getToken()) %>">
                        <button type="submit" class="btn btn-secondary btn-sm">Burn</button>
                      </form>
                    <% } %>
                    <form action="<%= Html.esc(ctx) %>/admin/notes/delete" method="POST" style="display:inline;"
                          onsubmit="return confirm('Delete this note?');">
                      <input type="hidden" name="token" value="<%= Html.esc(n.getToken()) %>">
                      <button type="submit" class="btn btn-secondary btn-sm">Delete</button>
                    </form>
                  </div>
                </td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
    </div>
<%@ include file="/WEB-INF/admin_footer.jspf" %>
