package patika.defineX.dto.response;

import patika.defineX.model.Team;

import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName()
        );
    }
}
