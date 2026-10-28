<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.vanish.model.User"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%
    @SuppressWarnings("unchecked")
    List<User> users = (List<User>) request.getAttribute("users");
    @SuppressWarnings("unchecked")
    Map<String, Integer> noteCounts = (Map<String, Integer>) request.getAttribute("noteCounts");
    String adminTitle = "Users";
    String adminActive = "users";
%>
<%@ include file="/WEB-INF/admin_header.jspf" %>
    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Users</h2>
      </div>
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr><th>Username</th><th>Role</th><th>Registered</th><th>Notes</th><th>Actions</th></tr>
          </thead>
          <tbody>
            <% for (User u : users) {
                 Integer cnt = noteCounts.get(u.getUsername()); %>
              <tr>
                <td><%= Html.esc(u.getUsername()) %></td>
                <td><span class="badge <%= u.isAdmin() ? "badge-burned" : "badge-unread" %>"><%= Html.esc(String.valueOf(u.getRole())) %></span></td>
                <td style="color: var(--text-secondary);"><%= Html.esc(u.getRegisteredAt().format(com.vanish.model.Note.STAMP)) %></td>
                <td><%= cnt == null ? 0 : cnt %></td>
                <td>
                  <% if (!u.isAdmin()) { %>
                    <form action="<%= Html.esc(ctx) %>/admin/users/delete" method="POST" style="display:inline;"
                          onsubmit="return confirm('Delete this user?');">
                      <input type="hidden" name="username" value="<%= Html.esc(u.getUsername()) %>">
                      <button type="submit" class="btn btn-secondary btn-sm">Delete</button>
                    </form>
                  <% } %>
                </td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
    </div>
<%@ include file="/WEB-INF/admin_footer.jspf" %>
