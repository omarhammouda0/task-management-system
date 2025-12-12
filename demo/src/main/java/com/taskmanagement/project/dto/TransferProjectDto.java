package com.taskmanagement.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request body for transferring a project to another team")
public class TransferProjectDto {

    @Schema(description = "ID of the target team to transfer the project to", example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "New team ID is required")
    private Long newTeamId;
}