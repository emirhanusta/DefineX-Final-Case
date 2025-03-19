package patika.defineX.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import patika.defineX.dto.request.DepartmentRequest;
import patika.defineX.dto.response.DepartmentResponse;
import patika.defineX.event.DepartmentDeletedEvent;
import patika.defineX.exception.custom.CustomAlreadyExistException;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.Department;
import patika.defineX.repository.DepartmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private DepartmentService departmentService;

    private UUID departmentId;
    private Department department;
    private DepartmentRequest departmentRequest;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        department = new Department();
        department.setId(departmentId);
        department.setName("IT");

        departmentRequest = new DepartmentRequest("IT");
    }

    @Test
    void listAll_ShouldReturnDepartmentList() {
        when(departmentRepository.findAllByDeletedAtNull()).thenReturn(List.of(department));

        List<DepartmentResponse> result = departmentService.listAll();

        assertEquals(1, result.size());
        assertEquals("IT", result.getFirst().name());
        verify(departmentRepository, times(1)).findAllByDeletedAtNull();
    }

    @Test
    void getById_WhenDepartmentExists_ShouldReturnDepartmentResponse() {
        when(departmentRepository.findByIdAndDeletedAtNull(departmentId)).thenReturn(Optional.of(department));

        DepartmentResponse result = departmentService.getById(departmentId);

        assertEquals("IT", result.name());
        verify(departmentRepository, times(1)).findByIdAndDeletedAtNull(departmentId);
    }

    @Test
    void getById_WhenDepartmentDoesNotExist_ShouldThrowException() {
        when(departmentRepository.findByIdAndDeletedAtNull(departmentId)).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> departmentService.getById(departmentId));
        verify(departmentRepository, times(1)).findByIdAndDeletedAtNull(departmentId);
    }

    @Test
    void save_WhenDepartmentDoesNotExist_ShouldCreateDepartment() {
        when(departmentRepository.existsByNameAndDeletedAtNull("IT")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        DepartmentResponse result = departmentService.save(departmentRequest);

        assertNotNull(result);
        assertEquals("IT", result.name());
        verify(departmentRepository, times(1)).existsByNameAndDeletedAtNull("IT");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void save_WhenDepartmentAlreadyExists_ShouldThrowException() {
        when(departmentRepository.existsByNameAndDeletedAtNull("IT")).thenReturn(true);

        assertThrows(CustomAlreadyExistException.class, () -> departmentService.save(departmentRequest));
        verify(departmentRepository, times(1)).existsByNameAndDeletedAtNull("IT");
    }

    @Test
    void update_WhenDepartmentExists_ShouldUpdateDepartment() {
        when(departmentRepository.findByIdAndDeletedAtNull(departmentId)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        DepartmentResponse result = departmentService.update(departmentId, departmentRequest);

        assertNotNull(result);
        assertEquals("IT", result.name());
        verify(departmentRepository, times(1)).findByIdAndDeletedAtNull(departmentId);
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void update_WhenDepartmentNameIsUsedByAnotherDepartment_ShouldThrowException() {
        when(departmentRepository.findByIdAndDeletedAtNull(departmentId)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByNameAndDeletedAtNull("HR")).thenReturn(true);

        assertThrows(CustomAlreadyExistException.class, () -> departmentService.update(departmentId, new DepartmentRequest("HR")));
        verify(departmentRepository, times(1)).findByIdAndDeletedAtNull(departmentId);
        verify(departmentRepository, times(1)).existsByNameAndDeletedAtNull("HR");

    }

    @Test
    void update_WhenDepartmentDoesNotExist_ShouldThrowException() {
        when(departmentRepository.findByIdAndDeletedAtNull(departmentId)).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> departmentService.update(departmentId, departmentRequest));
        verify(departmentRepository, times(1)).findByIdAndDeletedAtNull(departmentId);
    }

    @Test
    void delete_WhenDepartmentExists_ShouldSoftDeleteAndPublishEvent() {
        when(departmentRepository.findByIdAndDeletedAtNull(departmentId)).thenReturn(Optional.of(department));

        departmentService.delete(departmentId);

        verify(departmentRepository, times(1)).findByIdAndDeletedAtNull(departmentId);
        verify(departmentRepository, times(1)).save(department);
        verify(applicationEventPublisher, times(1)).publishEvent(any(DepartmentDeletedEvent.class));
    }

    @Test
    void delete_WhenDepartmentDoesNotExist_ShouldThrowException() {
        when(departmentRepository.findByIdAndDeletedAtNull(departmentId)).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> departmentService.delete(departmentId));
        verify(departmentRepository, times(1)).findByIdAndDeletedAtNull(departmentId);
    }
}
