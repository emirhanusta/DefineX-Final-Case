package patika.defineX.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import patika.defineX.dto.request.ProjectRequest;
import patika.defineX.dto.response.ProjectResponse;
import patika.defineX.event.DepartmentDeletedEvent;
import patika.defineX.event.ProjectDeletedEvent;
import patika.defineX.exception.custom.CustomAlreadyExistException;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.exception.custom.StatusChangeException;
import patika.defineX.model.Department;
import patika.defineX.model.Project;
import patika.defineX.model.enums.ProjectStatus;
import patika.defineX.repository.ProjectRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private ProjectRequest projectRequest;
    private Project project;
    private Department department;
    private UUID projectId;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        projectRequest = new ProjectRequest(departmentId, "Test Project", "desc", ProjectStatus.IN_PROGRESS);

        department = Department.builder()
                .name("Test Department")
                .build();
        department.setId(departmentId);

        project = Project.builder()
                .title("Test Project")
                .description("desc")
                .department(department)
                .status(ProjectStatus.IN_PROGRESS)
                .build();
        project.setId(projectId);
    }


    @Test
    void save_ShouldSaveProject() {
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(departmentService.findById(any(UUID.class))).thenReturn(null);

        ProjectResponse response = projectService.save(projectRequest);

        assertNotNull(response);
        assertEquals("Test Project", response.title());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void save__WhenDuplicateTitle_ShouldThrowException() {
        when(projectRepository.existsByTitleAndDeletedAtNull(anyString())).thenReturn(true);

        CustomAlreadyExistException exception = assertThrows(CustomAlreadyExistException.class, () -> projectService.save(projectRequest));
        assertEquals("Project already exists with name: TEST PROJECT", exception.getMessage());
    }

    @Test
    void getById_ShouldReturnProjectResponse() {
        when(projectRepository.findByIdAndDeletedAtNull(any(UUID.class))).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getById(projectId);

        assertNotNull(response);
        assertEquals(projectId, response.id());
    }

    @Test
    void getById_WhenProjectNotFound_ShouldThrowException() {
        when(projectRepository.findByIdAndDeletedAtNull(any(UUID.class))).thenReturn(Optional.empty());

        CustomNotFoundException exception = assertThrows(CustomNotFoundException.class, () -> projectService.getById(projectId));
        assertEquals("Project not found with id: " + projectId, exception.getMessage());
    }

    @Test
    void listAllByDepartmentId_ShouldReturnProjectsResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(List.of(project), pageable, 1);

        when(departmentService.findById(any(UUID.class))).thenReturn(department);
        when(projectRepository.findAllWithPaginationByDepartmentIdAndDeletedAtNull(departmentId,pageable)).thenReturn(projectPage);

        Page<ProjectResponse> responses = projectService.listAllByDepartmentId(departmentId, pageable);

        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
        assertEquals("Test Project", responses.getContent().getFirst().title());
    }

    @Test
    void listAllByDepartmentId_WhenDepartmentNotFound_ShouldThrowException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(departmentService.findById(any(UUID.class)))
                .thenThrow(new CustomNotFoundException("Department not found with id: " + departmentId));

        CustomNotFoundException exception = assertThrows(CustomNotFoundException.class,
                () -> projectService.listAllByDepartmentId(departmentId, pageable));
        assertEquals("Department not found with id: " + departmentId, exception.getMessage());
    }

    @Test
    void update_ShouldBeUpdated() {
        when(departmentService.findById(any(UUID.class))).thenReturn(department);
        when(projectRepository.findByIdAndDeletedAtNull(any(UUID.class))).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.update(projectId, projectRequest);

        assertNotNull(response);
        assertEquals("TEST PROJECT", response.title());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void update_WhenProjectNotFound_ShouldThrowException() {
        when(projectRepository.findByIdAndDeletedAtNull(any(UUID.class))).thenReturn(Optional.empty());

        CustomNotFoundException exception = assertThrows(CustomNotFoundException.class,
                () -> projectService.update(projectId, projectRequest));
        assertEquals("Project not found with id: " + projectId, exception.getMessage());
    }

    @Test
    void updateStatus_StatusShouldBeUpdated() {
        when(projectRepository.findByIdAndDeletedAtNull(any(UUID.class))).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.updateStatus(projectId, "COMPLETED");

        assertNotNull(response);
        assertEquals(ProjectStatus.COMPLETED.getDisplayName(), response.status());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testUpdateStatus_ProjectAlreadyCompletedShouldThrowException() {
        project.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findByIdAndDeletedAtNull(any(UUID.class))).thenReturn(Optional.of(project));

        StatusChangeException exception = assertThrows(StatusChangeException.class, () -> projectService.updateStatus(projectId, "ACTIVE"));
        assertEquals("Project status cannot be changed because it is completed.", exception.getMessage());
    }

    @Test
    void testDelete_ProjectShouldBeSoftDeleted() {
        when(projectRepository.findByIdAndDeletedAtNull(any(UUID.class))).thenReturn(Optional.of(project));

        projectService.delete(projectId);

        verify(projectRepository, times(1)).save(any(Project.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(ProjectDeletedEvent.class));
    }

    @Test
    void delete_WhenProjectNotFound_ShouldThrowException() {
        when(projectRepository.findByIdAndDeletedAtNull(any(UUID.class))).thenReturn(Optional.empty());

        CustomNotFoundException exception = assertThrows(CustomNotFoundException.class, () -> projectService.delete(projectId));
        assertEquals("Project not found with id: " + projectId, exception.getMessage());
    }

    @Test
    void deleteAllByDepartmentId_ShouldBeDeleted() {
        DepartmentDeletedEvent event = new DepartmentDeletedEvent(departmentId);

        when(projectRepository.findAllByDepartmentIdAndDeletedAtNull(any(UUID.class))).thenReturn(List.of(project));

        projectService.deleteAllByDepartmentId(event);

        verify(projectRepository, times(1)).saveAll(any());
        verify(applicationEventPublisher, times(1)).publishEvent(any(ProjectDeletedEvent.class));
    }
}
