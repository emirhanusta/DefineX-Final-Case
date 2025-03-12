package patika.defineX.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import patika.defineX.dto.request.DepartmentRequest;
import patika.defineX.dto.response.DepartmentResponse;
import patika.defineX.service.DepartmentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/v1")
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        return ResponseEntity.ok(departmentService.listAll());
    }

    @GetMapping("/v1/{id}")
    public ResponseEntity<DepartmentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @PostMapping("/v1")
    public ResponseEntity<DepartmentResponse> save(@Valid @RequestBody DepartmentRequest departmentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.save(departmentRequest));
    }

    @PutMapping("/v1/{id}")
    public ResponseEntity<DepartmentResponse> update(@PathVariable UUID id,@Valid @RequestBody DepartmentRequest departmentRequest) {
        return ResponseEntity.ok(departmentService.update(id, departmentRequest));
    }

    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
