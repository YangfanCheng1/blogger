package com.yangfan.core.api.services.thread.controller;

import com.yangfan.core.api.services.thread.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ThreadController {

    private final List<Post> posts = Arrays.asList(
            Post.builder().id("1").op("JohnDoe").title("Check this out").description("Some description about this post, " +
                    "let's have more text in this post I am just trying to type whatever comes to my midnd, and hopefully it would testo ut my changes").build(),
            Post.builder().id("2").op("JaneDoe").title("Hello").description("Some description about this post").build()
    );

    @GetMapping("/posts")
    public List<Post> getPosts(@RequestParam(defaultValue = "0") Integer pageNum, @RequestParam Integer pageSize) {

        return posts.stream().limit(pageSize).collect(Collectors.toList());
    }

    @PostMapping("/posts")
    public void addPost(@RequestBody @Validated Post post) {
        posts.add(0, post);
    }
}
