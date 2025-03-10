package patika.defineX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import patika.defineX.model.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByIdAndIsDeletedFalse(UUID id);
    List<Project> findAllByDepartmentIdAndIsDeletedFalse(UUID departmentId);
}
