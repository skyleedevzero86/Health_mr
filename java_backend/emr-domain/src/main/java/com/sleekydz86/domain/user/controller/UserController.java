package com.sleekydz86.domain.user.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.service.UserService;
import com.sleekydz86.domain.user.type.RoleType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdDate") Pageable pageable) {
        Page<UserEntity> users = userService.getAllUsers(pageable);
        Page<UserResponse> response = users.map(UserResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable Long userId) {
        UserEntity user = userService.getUserById(userId);
        return ResponseEntity.ok(UserDetailResponse.from(user));
    }

    @PostMapping
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserEntity user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserEntity user = userService.updateUser(userId, request);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/{userId}")
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/reset-password")
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordResetRequest request) {
        userService.resetPassword(userId, request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/activate")
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/deactivate")
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<UserResponse>> getUsersByDepartment(@PathVariable Long departmentId) {
        List<UserEntity> users = userService.getUsersByDepartment(departmentId);
        List<UserResponse> response = users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable RoleType role) {
        List<UserEntity> users = userService.getUsersByRole(role);
        List<UserResponse> response = users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/approved/waitlist")
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<List<WaitUserResponse>> getUsersToBeApproved() {
        List<UserEntity> users = userService.getUsersToBeApproved();
        List<WaitUserResponse> response = users.stream()
                .map(WaitUserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approved/role")
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<Void> approvedRole(@Valid @RequestBody WaitApprovedRequest request) {
        userService.approveUserWithInstitutions(request);
        return ResponseEntity.ok().build();
    }
}

