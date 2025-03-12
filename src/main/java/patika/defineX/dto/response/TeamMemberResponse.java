package patika.defineX.dto.response;

import patika.defineX.model.TeamMember;

import java.util.UUID;

public record TeamMemberResponse(
        UUID id,
        UUID userId,
        UUID teamId
) {
    public static TeamMemberResponse from(TeamMember teamMember) {
        return new TeamMemberResponse(
                teamMember.getId(),
                teamMember.getUser().getId(),
                teamMember.getTeam().getId()
        );
    }
}
