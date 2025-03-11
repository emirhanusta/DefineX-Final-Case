package patika.defineX.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.DepartmentRequest;
import patika.defineX.dto.response.DepartmentResponse;
import patika.defineX.event.DepartmentDeletedEvent;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.Department;
import patika.defineX.repository.DepartmentRepository;

import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DepartmentService(DepartmentRepository departmentRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.departmentRepository = departmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public List<DepartmentResponse> listAll() {
        return departmentRepository.findAllByIsDeletedFalse().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    public DepartmentResponse getById(UUID id) {
        return DepartmentResponse.from(findById(id));
    }

    public DepartmentResponse save(DepartmentRequest departmentRequest) {
        Department department = DepartmentRequest.from(departmentRequest);
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    public DepartmentResponse update(UUID id, DepartmentRequest departmentRequest) {
        Department department = findById(id);
        department.setName(departmentRequest.name());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional
    public void delete(UUID id) {
        Department department = findById(id);
        department.setDeleted(true);
        departmentRepository.save(department);
        applicationEventPublisher.publishEvent(new DepartmentDeletedEvent(id));
    }

    protected Department findById(UUID id) {
        return departmentRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new CustomNotFoundException("Department not found with id: " + id));
    }

}
