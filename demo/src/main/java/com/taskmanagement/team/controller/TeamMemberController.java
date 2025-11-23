package com.taskmanagement.team.controller;


import com.taskmanagement.team.dto.*;
import com.taskmanagement.team.service.TeamMemberService;
import com.taskmanagement.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor

@RestController
@RequestMapping("/api/team-members")

public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @PostMapping
    public ResponseEntity<TeamMemberResponseDto> addMember
            (@Valid @RequestBody AddMemberRequestDto dto) {

        return ResponseEntity.status ( HttpStatus.CREATED ).body ( teamMemberService.addMember ( dto ) );

    }

    @PutMapping
    public ResponseEntity<TeamMemberResponseDto> updateMemberRole
            (@Valid @RequestBody UpdateMemberRoleDto dto) {

        return ResponseEntity.status ( HttpStatus.OK ).body ( teamMemberService.updateMemberRole ( dto ) );


    }

}