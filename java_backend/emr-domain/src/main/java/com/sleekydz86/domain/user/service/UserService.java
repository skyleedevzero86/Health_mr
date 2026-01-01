package com.sleekydz86.domain.user.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.Password;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.repository.DepartmentRepository;
import com.sleekydz86.domain.institution.entity.InstitutionEntity;
import com.sleekydz86.domain.institution.repository.InstitutionRepository;
import com.sleekydz86.domain.institution.service.InstitutionService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.entity.UserInstitution;
import com.sleekydz86.domain.user.repository.UserInstitutionRepository;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.domain.user.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements BaseService<UserEntity, Long> {

    private final UserRepository userRepository;
    private final UserInstitutionRepository userInstitutionRepository;
    private final DepartmentRepository departmentRepository;
    private final InstitutionRepository institutionRepository;
    private final InstitutionService institutionService;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    public UserEntity getUserById(Long userId) {
        return validateExists(userRepository, userId, "사용자를 찾을 수 없습니다. ID: " + userId);
    }

    public UserEntity getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. LoginId: " + loginId));
    }

    public UserEntity getUserByLoginId(com.sleekydz86.domain.common.valueobject.LoginId loginId) {
        return getUserByLoginId(loginId.getValue());
    }

    public Page<UserEntity> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<UserEntity> getUsersByDepartment(Long departmentId) {
        return userRepository.findByDepartmentId(departmentId);
    }

    public List<UserEntity> getUsersByRole(RoleType role) {
        return userRepository.findAllByRole(role);
    }

    public List<UserEntity> getUsersToBeApproved() {
        return userRepository.findAllByRole(RoleType.WAIT);
    }

    @Transactional
    public UserEntity createUser(UserCreateRequest request) {

        validateNotDuplicate(userRepository.existsByLoginId(request.getLoginId()),
                "이미 사용 중인 계정입니다.");
        if (request.getEmail() != null) {
            validateNotDuplicate(userRepository.existsByEmail(request.getEmail()),
                    "이미 사용 중인 이메일입니다.");
        }

        if (request.getInttCd() != null && !request.getInttCd().isBlank()) {
            if (!institutionService.existsActiveByCode(request.getInttCd())) {
                throw new NotFoundException("존재하지 않거나 비활성화된 기관입니다.");
            }
        }

        DepartmentEntity department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("부서가 존재하지 않습니다."));

        UserEntity.UserEntityBuilder builder = request.toEntityBuilder(department);
        UserEntity user = builder
                .password(Password.fromPlainText(request.getPassword(), passwordEncoder))
                .build();

        UserEntity savedUser = userRepository.save(user);

        eventPublisher.publish(new com.cloud.emr.core.event.domain.UserCreatedEvent(
                savedUser.getId(),
                savedUser.getLoginIdValue(),
                savedUser.getRole().name()
        ));

        return savedUser;
    }

    @Transactional
    public UserEntity updateUser(Long userId, UserUpdateRequest request) {
        UserEntity user = getUserById(userId);

        // 프로필 정보 업데이트
        user.updateProfile(
                request.getName(),
                request.getGender(),
                request.getAddress()
        );

        if (request.getEmail() != null) {
            Email newEmail = request.getEmailValueObject();
            validateNotDuplicate(
                    userRepository.existsByEmail(newEmail.getValue()) &&
                            (user.getEmail() == null || !user.getEmail().equals(newEmail)),
                    "이미 사용 중인 이메일입니다.");
            user.changeEmail(newEmail);
        }

        if (request.getTelNum() != null) {
            PhoneNumber newPhoneNumber = request.getTelNumValueObject();
            user.changePhoneNumber(newPhoneNumber);
        }

        // 부서 변경은 별도 처리 필요 DepartmentService 의존성 확인하기
        // if (request.getDepartmentId() != null) {
        //     DepartmentEntity department = departmentService.getDepartmentById(request.getDepartmentId());
        //     user.changeDepartment(department);
        // }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        UserEntity user = getUserById(userId);
        userRepository.delete(user);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserEntity user = getUserById(userId);

        if (!user.verifyPassword(oldPassword, passwordEncoder)) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        user.changePassword(newPassword, passwordEncoder);
        userRepository.save(user);

        eventPublisher.publish(new com.sleekydz86.core.event.domain.UserPasswordChangedEvent(
                user.getId(),
                user.getLoginIdValue()
        ));
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        UserEntity user = getUserById(userId);
        user.changePassword(newPassword, passwordEncoder);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long userId) {
        UserEntity user = getUserById(userId);
        // 활성화 로직 필요시 활성화 필드 추가 하기
    }

    @Transactional
    public void deactivateUser(Long userId) {
        UserEntity user = getUserById(userId);
        // 비활성화 로직 필요시 활성화 필드 추가 하기
    }

    @Transactional
    public void changeUserRole(Long userId, RoleType roleType) {
        UserEntity user = getUserById(userId);
        user.changeRole(roleType, eventPublisher);
        userRepository.save(user);
    }

    @Transactional
    public void approveUserWithInstitutions(WaitApprovedRequest request) {
        UserEntity user = getUserById(request.getUserId());

        user.changeRole(request.getRole(), eventPublisher);

        if (request.getInstitutionCodes() != null && !request.getInstitutionCodes().isEmpty()
                && !request.getRole().equals(RoleType.ADMIN)) {

            if (request.getInstitutionCodes().size() > 3) {
                throw new IllegalArgumentException("병원은 최대 3개까지 지정할 수 있습니다.");
            }


            userInstitutionRepository.deleteByUserId(user.getId());


            String primaryCode = request.getPrimaryInstitutionCode();
            boolean hasPrimary = false;

            for (String institutionCode : request.getInstitutionCodes()) {

                InstitutionEntity institution = institutionRepository.findActiveByInstitutionCode(institutionCode)
                        .orElseThrow(() -> new NotFoundException("존재하지 않거나 비활성화된 기관입니다: " + institutionCode));


                boolean isPrimary = primaryCode != null && primaryCode.equals(institutionCode) && !hasPrimary;
                if (isPrimary) {
                    hasPrimary = true;
                }

                UserInstitution userInstitution = UserInstitution.builder()
                        .user(user)
                        .institution(institution)
                        .isPrimary(isPrimary)
                        .build();

                userInstitutionRepository.save(userInstitution);
            }
        }

        userRepository.save(user);
    }
}