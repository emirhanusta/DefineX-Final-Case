package patika.defineX.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.DepartmentRequest;
import patika.defineX.dto.response.DepartmentResponse;
import patika.defineX.event.DepartmentDeletedEvent;
import patika.defineX.exception.custom.CustomAlreadyExistException;
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
        return departmentRepository.findAllByDeletedAtNull().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    public DepartmentResponse getById(UUID id) {
        return DepartmentResponse.from(findById(id));
    }

    public DepartmentResponse save(DepartmentRequest departmentRequest) {
        existsByName(departmentRequest.name().toUpperCase());
        Department department = DepartmentRequest.from(departmentRequest);
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    public DepartmentResponse update(UUID id, DepartmentRequest departmentRequest) {
        Department department = findById(id);
        String name = departmentRequest.name().toUpperCase();
        if (!department.getName().equals(name)) {
            existsByName(name);
        }
        department.setName(name);
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional
    public void delete(UUID id) {
        Department department = findById(id);
        department.softDelete();
        departmentRepository.save(department);
        applicationEventPublisher.publishEvent(new DepartmentDeletedEvent(id));
    }

    protected Department findById(UUID id) {
        return departmentRepository.findByIdAndDeletedAtNull(id).orElseThrow(
                () -> new CustomNotFoundException("Department not found with id: " + id));
    }

    private void existsByName(String name) {
        if (departmentRepository.existsByNameAndDeletedAtNull(name)) {
            throw new CustomAlreadyExistException("Department already exists with name: " + name);
        }
    }

}
