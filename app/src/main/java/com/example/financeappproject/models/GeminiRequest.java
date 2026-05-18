package com.example.financeappproject.models;

import java.util.ArrayList;
import java.util.List;

public class GeminiRequest {
    public List<Content> contents = new ArrayList<>();

    public GeminiRequest(String promptText) {
        Content content = new Content();
        Part part = new Part();
        part.text = promptText;
        content.parts.add(part);
        this.contents.add(content);
    }

    public static class Content {
        public List<Part> parts = new ArrayList<>();
    }

    public static class Part {
        public String text;
    }
}