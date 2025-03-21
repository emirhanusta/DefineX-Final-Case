package patika.defineX.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import patika.defineX.model.Department;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Page<Department> findAllWithPaginationByDeletedAtNull(Pageable pageable);
    Optional<Department> findByIdAndDeletedAtNull(UUID id);
    boolean existsByNameAndDeletedAtNull(String name);
}
