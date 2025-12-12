package com.taskmanagement.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a new team")
public record TeamCreateDto(

        @Schema(description = "Team name (must be unique, letters/spaces/hyphens/apostrophes only)",
                example = "Development Team", requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 2, maxLength = 100)
        @NotBlank(message = "Team name must not be blank")
        @Size(min = 2 , max = 100 , message = "Team name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Team name can only contain letters," +
                " spaces, hyphens, and apostrophes")
        String name ,

        @Schema(description = "Optional team description", example = "Backend development team for core services",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 500)
        @Size (max = 500 , message = "Description must not exceed 500 characters")
        String description


) {
}
