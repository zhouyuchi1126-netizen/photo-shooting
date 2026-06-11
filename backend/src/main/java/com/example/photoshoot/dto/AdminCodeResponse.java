package com.example.photoshoot.dto;
public class AdminCodeResponse {
    private boolean success;
    private String message;
    private String token;
    private String username;
    private String role;
    private String displayName;
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean s) { success = s; }
    public String getMessage() { return message; }
    public void setMessage(String m) { message = m; }
    public String getToken() { return token; }
    public void setToken(String t) { token = t; }
    public String getUsername() { return username; }
    public void setUsername(String u) { username = u; }
    public String getRole() { return role; }
    public void setRole(String r) { role = r; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String d) { displayName = d; }
}
