package com.example.financeappproject.models;

public class User {
    public String user_id;        // Matches 'uuid'
    public String name;           // Matches 'character varying'
    public String email;          // Matches 'character varying'
    public String password_hash;  // Matches 'character varying'
    public String biometric_token;// Matches 'text'
    public String created_at;     // Matches 'timestamp'
}