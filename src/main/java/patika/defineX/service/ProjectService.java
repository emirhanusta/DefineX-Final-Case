package patika.defineX.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

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
        log.info("Fetching projects for department with id: {} from database", departmentId);
        departmentService.findById(departmentId);
        List<ProjectResponse> projects = projectRepository.findAllByDepartmentIdAndDeletedAtNull(departmentId)
                .stream()
                .map(ProjectResponse::from)
                .toList();
        log.info("{} projects found for department id: {}", projects.size(), departmentId);
        return projects;
    }

    public ProjectResponse getById(UUID id) {
        log.info("Fetching project with id: {} from database", id);
        ProjectResponse response = ProjectResponse.from(findById(id));
        log.info("Project with id: {} retrieved successfully.", id);
        return response;
    }

    public ProjectResponse save(ProjectRequest projectRequest) {
        log.info("Creating a new project with title: {}", projectRequest.title());
        existsByTitle(projectRequest.title().toUpperCase());
        Project project = ProjectRequest.from(projectRequest);
        project.setDepartment(departmentService.findById(projectRequest.departmentId()));
        ProjectResponse response = ProjectResponse.from(projectRepository.save(project));
        log.info("Project '{}' created successfully with id: {}", project.getTitle(), project.getId());
        return response;
    }

    public ProjectResponse update(UUID id, ProjectRequest projectRequest) {
        log.info("Updating project with id: {}", id);
        Project project = findById(id);

        if (!project.getTitle().equalsIgnoreCase(projectRequest.title())) {
            if (projectRepository.existsByTitleAndDeletedAtNull(projectRequest.title().toUpperCase())) {
                throw new CustomAlreadyExistException("Project already exists with name: " + projectRequest.title().toUpperCase());
            }
        }

        project.setTitle(projectRequest.title().toUpperCase());
        project.setDescription(projectRequest.description());
        project.setStatus(projectRequest.status());
        project.setDepartment(departmentService.findById(projectRequest.departmentId()));

        ProjectResponse response = ProjectResponse.from(projectRepository.save(project));
        log.info("Project with id: {} updated successfully. New title: {}", id, project.getTitle());
        return response;
    }

    @Transactional
    public void delete(UUID id) {
        log.warn("Attempting to delete project with id: {}", id);
        Project project = findById(id);
        project.softDelete();
        projectRepository.save(project);
        applicationEventPublisher.publishEvent(new ProjectDeletedEvent(project.getId()));
        log.warn("Project with id: {} has been marked as deleted.", id);
    }

    public ProjectResponse updateStatus(UUID id, String status) {
        log.info("Updating project status for id: {} to {}", id, status);
        Project project = findById(id);

        if (project.getStatus().equals(ProjectStatus.COMPLETED)) {
            log.error("Project status update failed for id: {}. Project is already completed.", id);
            throw new StatusChangeException("Project status cannot be changed because it is completed.");
        }

        project.setStatus(ProjectStatus.valueOf(status));
        ProjectResponse response = ProjectResponse.from(projectRepository.save(project));
        log.info("Project with id: {} status updated successfully to {}", id, project.getStatus());
        return response;
    }

    @EventListener
    @Transactional
    protected void deleteAllByDepartmentId(DepartmentDeletedEvent event) {
        log.warn("Deleting all projects for department id: {}", event.departmentId());

        List<Project> projects = projectRepository.findAllByDepartmentIdAndDeletedAtNull(event.departmentId());

        projects.forEach(project -> {
            project.softDelete();
            applicationEventPublisher.publishEvent(new ProjectDeletedEvent(project.getId()));
        });

        projectRepository.saveAll(projects);

        log.warn("{} projects deleted for department id: {}", projects.size(), event.departmentId());

        //cacheService.clearProjectCache(event.departmentId());
    }

    protected Project findById(UUID id) {
        log.debug("Searching for project with id: {}", id);
        return projectRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> {
                    log.error("Project with id: {} not found!", id);
                    return new CustomNotFoundException("Project not found with id: " + id);
                });
    }

    protected void existsByTitle(String name) {
        log.debug("Checking if project with title '{}' already exists...", name);
        if (projectRepository.existsByTitleAndDeletedAtNull(name)) {
            log.error("Project with title '{}' already exists!", name);
            throw new CustomAlreadyExistException("Project already exists with name: " + name);
        }
    }
}
