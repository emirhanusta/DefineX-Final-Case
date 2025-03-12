package patika.defineX.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TeamRequest(
        @NotBlank
        @Size(min = 2, max = 50)
        String name,
        @NotNull
        UUID projectId
) {
}
