package com.taskmanagement.welcome;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class WelcomeController {

    @GetMapping("/")
    public String welcome(Model model) {
        model.addAttribute("title", "Task Management System");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("status", "UP");
        model.addAttribute("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        ));
        return "welcome";
    }

    @GetMapping("/health")
    public String health(Model model) {
        model.addAttribute("healthStatus", "UP");
        model.addAttribute("serviceName", "Task Management System API");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("timestamp", Instant.now().toString());
        return "health"; // This renders health.html
    }


    @GetMapping(value = "/api/info", produces = "application/json")
    @org.springframework.web.bind.annotation.ResponseBody
    public ApiInfo apiInfo() {
        return new ApiInfo(
                "Task Management System",
                "REST API for managing tasks, projects, teams and users",
                "1.0.0",
                Instant.now().toString()
        );
    }


    public record ApiInfo(String name, String description, String version, String timestamp) {}
}