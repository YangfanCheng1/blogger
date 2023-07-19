package com.yangfan.core.api.services.thread.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.List;

@Value
@With
@Builder
public class Post {

    String id;
    String op;
    String title;
    String description;
    Instant createdAt;
    Instant updatedAt;

    List<Comment> comments;

    @Value
    @With
    @Builder
    private static class Comment {
        String user;
        String message;
        Instant createdAt;
        Instant updatedAt;
    }
}
