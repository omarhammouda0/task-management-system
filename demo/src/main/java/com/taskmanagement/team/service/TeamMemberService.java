package com.taskmanagement.team.service;

import com.taskmanagement.team.dto.AddMemberRequestDto;
import com.taskmanagement.team.dto.TeamMemberResponseDto;
import com.taskmanagement.team.dto.UpdateMemberRoleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeamMemberService {


    TeamMemberResponseDto addMember (AddMemberRequestDto dto);

    void removeMember (Long teamId, Long userId);

    TeamMemberResponseDto updateMemberRole (UpdateMemberRoleDto dto);

    Page<TeamMemberResponseDto> getMembersByTeam (Long teamId , Pageable pageable);

    Page <TeamMemberResponseDto> getTeamsByUser (Long userId , Pageable pageable);

    TeamMemberResponseDto getMember ( Long userId , Long teamId);

    boolean isMemberInTeam (Long teamId, Long userId);

    Long getMembersCount (Long teamId);

}
