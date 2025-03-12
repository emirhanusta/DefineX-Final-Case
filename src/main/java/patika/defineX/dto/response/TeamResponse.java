package patika.defineX.dto.response;

import patika.defineX.model.Team;

import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        List<TeamMemberResponse> members
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getMembers() == null ? List.of() : team.getMembers().stream().map(TeamMemberResponse::from).toList()
        );
    }
}
