package patika.defineX.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DepartmentService(DepartmentRepository departmentRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.departmentRepository = departmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public List<DepartmentResponse> listAll() {
        log.info("Fetching all departments from the database...");
        List<DepartmentResponse> departments = departmentRepository.findAllByDeletedAtNull()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
        log.info("Retrieved {} departments.", departments.size());
        return departments;
    }

    public DepartmentResponse getById(UUID id) {
        log.info("Fetching department with id: {} from database...", id);
        DepartmentResponse response = DepartmentResponse.from(findById(id));
        log.info("Department with id: {} retrieved successfully.", id);
        return response;
    }

    public DepartmentResponse save(DepartmentRequest departmentRequest) {
        log.info("Attempting to create a new department with name: {}", departmentRequest.name());
        existsByName(departmentRequest.name().toUpperCase());
        Department department = DepartmentRequest.from(departmentRequest);
        DepartmentResponse response = DepartmentResponse.from(departmentRepository.save(department));
        log.info("Department '{}' created successfully with id: {}", department.getName(), department.getId());
        return response;
    }

    public DepartmentResponse update(UUID id, DepartmentRequest departmentRequest) {
        log.info("Updating department with id: {}", id);
        Department department = findById(id);
        String name = departmentRequest.name().toUpperCase();
        if (!department.getName().equals(name)) {
            existsByName(name);
        }
        department.setName(name);
        DepartmentResponse response = DepartmentResponse.from(departmentRepository.save(department));
        log.info("Department with id: {} updated successfully. New name: {}", id, name);
        return response;
    }

    @Transactional
    public void delete(UUID id) {
        log.warn("Attempting to delete department with id: {}", id);
        Department department = findById(id);
        department.softDelete();
        departmentRepository.save(department);
        applicationEventPublisher.publishEvent(new DepartmentDeletedEvent(id));
        log.warn("Department with id: {} has been marked as deleted.", id);
    }

    protected Department findById(UUID id) {
        log.debug("Searching for department with id: {}", id);
        return departmentRepository.findByIdAndDeletedAtNull(id).orElseThrow(() -> {
            log.error("Department with id: {} not found!", id);
            return new CustomNotFoundException("Department not found with id: " + id);
        });
    }

    private void existsByName(String name) {
        log.debug("Checking if department with name '{}' already exists...", name);
        if (departmentRepository.existsByNameAndDeletedAtNull(name)) {
            log.error("Department with name '{}' already exists!", name);
            throw new CustomAlreadyExistException("Department already exists with name: " + name);
        }
    }
}
