<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    com.vanish.service.AdminService.Stats stats = (com.vanish.service.AdminService.Stats) request.getAttribute("stats");
    String adminTitle = "Admin";
    String adminActive = "overview";
%>
<%@ include file="/WEB-INF/admin_header.jspf" %>
    <h1 style="font-size: 1.5rem; margin-bottom: 1.5rem;">Admin Overview</h1>
    <div class="grid-2" style="margin-bottom: 2rem;">
      <div class="card"><h2 class="card-title"><%= stats.users() %></h2><p class="card-subtitle">Users</p></div>
      <div class="card"><h2 class="card-title"><%= stats.notes() %></h2><p class="card-subtitle">Notes</p></div>
      <a href="<%= Html.esc(ctx) %>/admin/approvals" class="card" style="text-decoration: none; color: inherit;"><h2 class="card-title"><%= stats.pending() %></h2><p class="card-subtitle">Pending approval</p></a>
      <div class="card"><h2 class="card-title"><%= stats.unread() %></h2><p class="card-subtitle">Unread</p></div>
      <div class="card"><h2 class="card-title"><%= stats.burned() %></h2><p class="card-subtitle">Burned</p></div>
      <div class="card"><h2 class="card-title"><%= stats.expired() %></h2><p class="card-subtitle">Expired</p></div>
      <div class="card"><h2 class="card-title"><%= stats.rejected() %></h2><p class="card-subtitle">Rejected</p></div>
    </div>
    <div style="display: flex; gap: 0.75rem;">
      <a href="<%= Html.esc(ctx) %>/admin/users" class="btn btn-primary btn-sm">Manage Users</a>
      <a href="<%= Html.esc(ctx) %>/admin/notes" class="btn btn-primary btn-sm">Manage Notes</a>
    </div>
<%@ include file="/WEB-INF/admin_footer.jspf" %>
