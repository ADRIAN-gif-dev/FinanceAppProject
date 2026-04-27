package com.example.financeappproject.models;

public class Debt {
    public String debt_id;          // Primary Key
    public String user_id;          // Foreign Key
    public String debt_name;
    public double total_amount;
    public double remaining_balance;
    public String due_date;         // Stored as String for easy parsing from SQL
    public String status;           // e.g., "Active", "Paid" [cite: 148]
}