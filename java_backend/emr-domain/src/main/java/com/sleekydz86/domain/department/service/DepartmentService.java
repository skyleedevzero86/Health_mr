package com.sleekydz86.domain.department.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.department.dto.DepartmentCreateRequest;
import com.sleekydz86.domain.department.dto.DepartmentUpdateRequest;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.repository.DepartmentRepository;
import com.sleekydz86.domain.department.type.DepartmentType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService implements BaseService<DepartmentEntity, Long> {

    private final DepartmentRepository departmentRepository;
    private final EventPublisher eventPublisher;

    public DepartmentEntity getDepartmentById(Long id) {
        return validateExists(departmentRepository, id, "부서를 찾을 수 없습니다. ID: " + id);
    }

    public DepartmentEntity getDepartmentByCode(String code) {
        return departmentRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("부서를 찾을 수 없습니다. Code: " + code));
    }

    public List<DepartmentEntity> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Page<DepartmentEntity> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }

    public List<DepartmentEntity> getDepartmentsByType(DepartmentType type) {
        return departmentRepository.findByType(type);
    }

    public List<DepartmentEntity> searchDepartments(String keyword) {
        return departmentRepository.findByNameContaining(keyword);
    }

    @Transactional
    public DepartmentEntity createDepartment(DepartmentCreateRequest request) {

        validateNotDuplicate(departmentRepository.existsByCode(request.getCode()),
                "이미 사용 중인 부서 코드입니다.");

        DepartmentEntity department = request.toEntity();
        DepartmentEntity savedDepartment = departmentRepository.save(department);


        eventPublisher.publish(new com.sleekydz86.core.event.domain.DepartmentCreatedEvent(
                savedDepartment.getId(),
                savedDepartment.getCode(),
                savedDepartment.getName()
        ));

        return savedDepartment;
    }

    @Transactional
    public DepartmentEntity updateDepartment(Long id, DepartmentUpdateRequest request) {
        DepartmentEntity department = getDepartmentById(id);

        if (request.getName() != null) {
            department.setName(request.getName());
        }
        if (request.getEngName() != null) {
            department.setEngName(request.getEngName());
        }
        if (request.getType() != null) {
            department.setType(request.getType());
        }

        DepartmentEntity updatedDepartment = departmentRepository.save(department);


        eventPublisher.publish(new com.sleekydz86.core.event.domain.DepartmentUpdatedEvent(
                updatedDepartment.getId(),
                updatedDepartment.getCode(),
                updatedDepartment.getName()
        ));

        return updatedDepartment;
    }

    @Transactional
    public void deleteDepartment(Long id) {
        DepartmentEntity department = getDepartmentById(id);

        // 사용 중인 부서인지 확인 하는 기능 만들기
        // UserRepository를 주입받아 확인하기

        departmentRepository.delete(department);
    }
}

