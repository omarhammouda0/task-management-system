package com.taskmanagement.team.dto;

import com.taskmanagement.team.enums.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating a team. All fields are optional - only provided fields will be updated.")
public record TeamUpdateDto (

        @Schema(description = "New team name (letters/spaces/hyphens/apostrophes only)",
                example = "Updated Team Name", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minLength = 2, maxLength = 100)
        @Size(min = 2 , max = 100 , message = "Team name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Team name can only contain letters," +
                " spaces, hyphens, and apostrophes")
        String name ,

        @Schema(description = "New team description", example = "Updated team description",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 500)
        @Size (max = 500 , message = "Description must not exceed 500 characters")
        String description ,

        @Schema(description = "New team status", example = "ACTIVE",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"ACTIVE", "INACTIVE"})
        TeamStatus status) {
}
