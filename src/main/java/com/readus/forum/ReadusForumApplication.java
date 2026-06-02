package com.readus.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ReadusForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReadusForumApplication.class, args);
    }
}
