package patika.defineX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import patika.defineX.model.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findAllByDeletedAtNull();
    Optional<Department> findByIdAndDeletedAtNull(UUID id);
    boolean existsByNameAndDeletedAtNull(String name);
}
