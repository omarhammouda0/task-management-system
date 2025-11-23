package com.taskmanagement.team.dto;

import com.taskmanagement.team.enums.TeamStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record TeamUpdateDto (

        @Size(min = 2 , max = 100 , message = "Team name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Team name can only contain letters," +
                " spaces, hyphens, and apostrophes")
        String name ,

        @Size (max = 500 , message = "Description must not exceed 500 characters")
        String description ,

        TeamStatus status) {
}
