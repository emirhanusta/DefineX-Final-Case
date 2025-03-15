package patika.defineX.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import patika.defineX.dto.request.UserRequest;
import patika.defineX.dto.response.UserResponse;
import patika.defineX.service.UserService;
import patika.defineX.model.enums.Role;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/v1")
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(userService.listAll());
    }

    @GetMapping("/v1/{id}")
    public ResponseEntity<UserResponse> getUserById (@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("/v1/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id,@Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.update(id, userRequest));
    }

    @Secured("ROLE_PROJECT_MANAGER")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @PatchMapping("/v1/{id}/roles/add")
    public ResponseEntity<UserResponse> addRole(@PathVariable UUID id, @RequestParam Role role) {
        return ResponseEntity.ok(userService.addRole(id, role));
    }

    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @PatchMapping("/v1/{id}/roles/remove")
    public ResponseEntity<UserResponse> removeRole(@PathVariable UUID id, @RequestParam Role role) {
        return ResponseEntity.ok(userService.removeRole(id, role));
    }

    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
