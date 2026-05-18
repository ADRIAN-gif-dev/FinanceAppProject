package com.example.financeappproject.models;

public class Transactions {
    public String trans_id;       // uuid
    public String type;           // e.g., "Expense", "Income"
    public String source_id;      // Matches 'uuid'
    public String user_id;        // Matches 'uuid'
    public double amount;         // Matches 'numeric'
    public String timestamp;      // Matches 'timestamp'
}