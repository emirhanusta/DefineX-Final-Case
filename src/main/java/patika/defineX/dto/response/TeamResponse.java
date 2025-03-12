package patika.defineX.dto.response;

import patika.defineX.model.Team;
import patika.defineX.model.TeamMember;

import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        List<TeamMember> members
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                null
        );
    }
}
