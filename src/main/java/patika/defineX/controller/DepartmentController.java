package patika.defineX.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import patika.defineX.dto.request.DepartmentRequest;
import patika.defineX.dto.response.DepartmentResponse;
import patika.defineX.service.DepartmentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Department", description = "Department API")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }


    @Operation(
            method = "GET",
            summary = "List all departments",
            description = "List all departments in the system"
    )
    @GetMapping("/v1")
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        return ResponseEntity.ok(departmentService.listAll());
    }

    @Operation(
            method = "GET",
            summary = "Get department",
            description = "Get department by id"
    )
    @GetMapping("/v1/{id}")
    public ResponseEntity<DepartmentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @Operation(
            method = "POST",
            summary = "Create a department",
            description = "Create a department. Only project managers can create a department"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PostMapping("/v1")
    public ResponseEntity<DepartmentResponse> save(@Valid @RequestBody DepartmentRequest departmentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.save(departmentRequest));
    }

    @Operation(
            method = "PUT",
            summary = "Update a department",
            description = "Update a department by id with new values. Only project managers can update a department"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PutMapping("/v1/{id}")
    public ResponseEntity<DepartmentResponse> update(@PathVariable UUID id,@Valid @RequestBody DepartmentRequest departmentRequest) {
        return ResponseEntity.ok(departmentService.update(id, departmentRequest));
    }

    @Operation(
            method = "DELETE",
            summary = "Delete a department",
            description = "Delete a department by id. Only project managers can delete a department"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
