package com.example.financeappproject.models;

public class Fund {
    public String fund_id;
    public String user_id;
    public String fund_name;
    public double goal_amount;
    public double current_balance;
    public boolean is_achieved;     // Automated trigger when goal is met [cite: 151]
}