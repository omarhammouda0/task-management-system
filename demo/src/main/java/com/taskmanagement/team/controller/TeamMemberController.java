package com.taskmanagement.team.controller;


import com.taskmanagement.team.dto.*;
import com.taskmanagement.team.service.TeamMemberService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/{teamId}/members")

    public ResponseEntity<Page<TeamMemberResponseDto>> getMembersByTeam
            (@PathVariable Long teamId , Pageable pageable) {

        return ResponseEntity.status ( HttpStatus.OK ).body ( teamMemberService.getMembersByTeam ( teamId , pageable ) );
    }

    @GetMapping("/{teamId}/members/{userId}")

    public ResponseEntity<TeamMemberResponseDto> getMember
            ( @PathVariable Long teamId  ,  @PathVariable Long userId) {

        return ResponseEntity.status ( HttpStatus.OK ).body ( teamMemberService.getMember ( teamId ,userId   ) );

    }

    @GetMapping("/{teamId}/count")
    @PreAuthorize ("hasRole('ADMIN')")

    public ResponseEntity <Long> getTotalMembersCountInTeamForAdmin (@PathVariable Long teamId) {

        return ResponseEntity.status ( HttpStatus.OK ).body
                ( teamMemberService.getTotalMembersCountForAdmin ( teamId ) );

    }

    @GetMapping("/{teamId}/count/active")
    @PreAuthorize ("hasRole('ADMIN')")

    public ResponseEntity <Long> getActiveMembersCount (@PathVariable Long teamId) {

        return ResponseEntity.status ( HttpStatus.OK ).body
                ( teamMemberService.getActiveMembersCount ( teamId ) );

    }

    @PutMapping
    public ResponseEntity<TeamMemberResponseDto> updateMemberRole
            (@Valid @RequestBody UpdateMemberRoleDto dto) {

        return ResponseEntity.status ( HttpStatus.OK ).body ( teamMemberService.updateMemberRole ( dto ) );

    }


    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember
            (@PathVariable Long teamId , @PathVariable Long userId) {


        teamMemberService.removeMember ( teamId , userId );
        return ResponseEntity.status ( HttpStatus.NO_CONTENT ).build ();

    }

    @DeleteMapping("/{teamId}/leave")
    public ResponseEntity <Void> leaveTeam
            (@PathVariable Long teamId) {

        teamMemberService.leaveTeam ( teamId );
        return ResponseEntity.status ( HttpStatus.NO_CONTENT ).build ();

    }




}