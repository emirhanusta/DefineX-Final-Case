package patika.defineX.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.ProjectRequest;
import patika.defineX.dto.response.ProjectResponse;
import patika.defineX.event.DepartmentDeletedEvent;
import patika.defineX.event.ProjectDeletedEvent;
import patika.defineX.exception.custom.CustomAlreadyExistException;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.exception.custom.StatusChangeException;
import patika.defineX.model.Project;
import patika.defineX.model.enums.ProjectStatus;
import patika.defineX.repository.ProjectRepository;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DepartmentService departmentService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ProjectService(ProjectRepository projectRepository, DepartmentService departmentService,
                          ApplicationEventPublisher applicationEventPublisher) {
        this.projectRepository = projectRepository;
        this.departmentService = departmentService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public List<ProjectResponse> listAllByDepartmentId(UUID departmentId) {
        departmentService.findById(departmentId);
        return projectRepository.findAllByDepartmentIdAndDeletedAtNull(departmentId).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    public ProjectResponse getById(UUID id) {
        return ProjectResponse.from(findById(id));
    }

    public ProjectResponse save(ProjectRequest projectRequest) {
        existsByTitle(projectRequest.title().toUpperCase());
        Project project = ProjectRequest.from(projectRequest);
        project.setDepartment(departmentService.findById(projectRequest.departmentId()));
        return ProjectResponse.from(projectRepository.save(project));
    }

    public ProjectResponse update(UUID id, ProjectRequest projectRequest) {
        Project project = findById(id);
        if (!project.getTitle().equals(projectRequest.title())) {
            existsByTitle(projectRequest.title().toUpperCase());
        }
        project.setTitle(projectRequest.title());
        project.setDescription(projectRequest.description());
        project.setStatus(projectRequest.status());
        project.setDepartment(departmentService.findById(projectRequest.departmentId()));
        return ProjectResponse.from(projectRepository.save(project));
    }

    public void delete(UUID id) {
        Project project = findById(id);
        project.softDelete();
        projectRepository.save(project);
        applicationEventPublisher.publishEvent(new ProjectDeletedEvent(project.getId()));
    }

    public ProjectResponse updateStatus(UUID id, String status) {
        Project project = findById(id);
        if (project.getStatus().equals(ProjectStatus.COMPLETED)){
            throw new StatusChangeException("Project status cannot be changed because it is completed.");
        }
        project.setStatus(ProjectStatus.valueOf(status));
        return ProjectResponse.from(projectRepository.save(project));
    }

    @EventListener
    @Transactional
    protected void deleteAllByDepartmentId(DepartmentDeletedEvent event) {
        List<Project> projects = projectRepository.findAllByDepartmentIdAndDeletedAtNull(event.departmentId());
        projects.forEach(project -> {
            project.softDelete();
            applicationEventPublisher.publishEvent(new ProjectDeletedEvent(project.getId()));
        });
        projectRepository.saveAll(projects);
    }

    protected Project findById(UUID id) {
        return projectRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> new CustomNotFoundException("Project not found with id: " + id));
    }

    private void existsByTitle(String name) {
        if (projectRepository.existsByTitleAndDeletedAtNull(name)) {
            throw new CustomAlreadyExistException("Project already exists with name: " + name);
        }
    }
}
