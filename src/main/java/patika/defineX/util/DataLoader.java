package patika.defineX.util;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import patika.defineX.model.Department;
import patika.defineX.model.Project;
import patika.defineX.model.enums.ProjectStatus;
import patika.defineX.repository.DepartmentRepository;
import patika.defineX.repository.ProjectRepository;

@Component
public class DataLoader {

    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;

    public DataLoader(DepartmentRepository departmentRepository, ProjectRepository projectRepository) {
        this.departmentRepository = departmentRepository;
        this.projectRepository = projectRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        Department itDepartment = Department.builder().name("IT").build();
        Department hrDepartment = Department.builder().name("HR").build();
        departmentRepository.save(itDepartment);
        departmentRepository.save(hrDepartment);

        Project projectA = Project.builder().department(itDepartment).title("A").description("Description A").status(ProjectStatus.IN_PROGRESS).build();
        Project projectB = Project.builder().department(hrDepartment).title("B").description("Description B").status(ProjectStatus.IN_PROGRESS).build();
        projectRepository.save(projectA);
        projectRepository.save(projectB);
    }
}