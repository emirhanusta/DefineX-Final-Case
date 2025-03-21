package patika.defineX.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import patika.defineX.model.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByIdAndDeletedAtNull(UUID id);
    Page<Project> findAllWithPaginationByDepartmentIdAndDeletedAtNull(UUID departmentId, Pageable pageable);
    List<Project> findAllByDepartmentIdAndDeletedAtNull(UUID departmentId);
    boolean existsByTitleAndDeletedAtNull(String name);
}
