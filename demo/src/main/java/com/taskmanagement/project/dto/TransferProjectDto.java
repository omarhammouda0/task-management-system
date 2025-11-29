package com.taskmanagement.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferProjectDto {

    @NotNull(message = "New team ID is required")
    private Long newTeamId;
}