package com.taskmanagement.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Management System API")
                        .version("1.0.0")
                        .description("""
                                ## Enterprise Task Management System API
                                
                                A comprehensive REST API for managing tasks, projects, teams, and users.
                                
                                ### Features
                                - **Authentication**: JWT-based authentication with access and refresh tokens
                                - **User Management**: User registration, profiles, and role-based access control
                                - **Team Management**: Create and manage teams with member roles
                                - **Project Management**: Organize work into projects with lifecycle management
                                - **Task Management**: Create, assign, and track tasks with priorities and due dates
                                - **Comments**: Add discussions and updates to tasks
                                - **Attachments**: Upload and manage file attachments on tasks
                                
                                ### Authentication
                                Most endpoints require authentication. To authenticate:
                                1. Register a new account or login with existing credentials
                                2. Copy the `accessToken` from the response
                                3. Click the **Authorize** button above and enter: `Bearer YOUR_ACCESS_TOKEN`
                                
                                ### Roles
                                - **MEMBER**: Basic access to assigned tasks and team resources
                                - **MANAGER**: Can manage team members and project settings
                                - **ADMIN**: Full system access including user management
                                """)
                        .contact(new Contact()
                                .name("Task Management System Support")
                                .email("support@taskmanagement.com")
                                .url("https://taskmanagement.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Task Management System Documentation")
                        .url("https://docs.taskmanagement.com"))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Current Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT access token obtained from the login endpoint.\n\nExample: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`")));
    }

    @Bean
    public OperationCustomizer customizePageableParameter() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null) {
                operation.getParameters().forEach(parameter -> {
                    if ("sort".equals(parameter.getName())) {
                        // Set default sort to "createdAt,desc" to avoid internal server error
                        parameter.setExample("createdAt,desc");
                        parameter.setDescription("Sorting criteria in the format: property(,asc|desc). Default sort order is descending. Multiple sort criteria are supported. Example: createdAt,desc or createdAt,desc&sort=id,asc");
                    }
                });
            }
            return operation;
        };
    }
}