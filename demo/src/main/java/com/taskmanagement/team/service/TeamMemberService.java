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

    void leaveTeam (Long teamId );

    Page<TeamMemberResponseDto> getMembersByTeam (Long teamId , Pageable pageable);

    TeamMemberResponseDto getMember ( Long userId , Long teamId);

    Long getTotalMembersCountForAdmin (Long teamId);

    Long getActiveMembersCount (Long teamId);

}
