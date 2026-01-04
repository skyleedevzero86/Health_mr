package com.sleekydz86.support.recess.service;

import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.support.recess.dto.RecessRequest;
import com.sleekydz86.support.recess.dto.RecessResponse;
import com.sleekydz86.support.recess.entity.RecessEntity;
import com.sleekydz86.support.recess.repository.RecessRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecessService {

    private final RecessRepository recessRepository;
    private final UserRepository userRepository;

    @Transactional
    public RecessResponse registerRecess(RecessRequest req) {

        validateTimeSlot(req.getRecessStart(), req.getRecessEnd());

        UserEntity user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getRole() != req.getRole()) {
            throw new RuntimeException("사용자의 역할이 일치하지 않습니다.");
        }

        LocalDateTime start = adjustToHalfHour(req.getRecessStart());
        LocalDateTime end = adjustToHalfHour(req.getRecessEnd());

        RecessEntity entity = RecessEntity.builder()
                .userEntity(user)
                .recessStart(start)
                .recessEnd(end)
                .recessReason(req.getRecessReason())
                .build();

        RecessEntity saved = recessRepository.save(entity);
        return toDto(saved, user);
    }

    @Transactional
    public RecessResponse updateRecess(Long id, RecessRequest req) {

        validateTimeSlot(req.getRecessStart(), req.getRecessEnd());

        RecessEntity existing = recessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("휴진 정보를 찾을 수 없습니다."));

        UserEntity user = existing.getUserEntity();

        if (user.getRole() != req.getRole()) {
            throw new RuntimeException("사용자의 역할이 일치하지 않습니다.");
        }

        LocalDateTime start = adjustToHalfHour(req.getRecessStart());
        LocalDateTime end = adjustToHalfHour(req.getRecessEnd());

        RecessEntity updated = RecessEntity.builder()
                .id(existing.getId())
                .userEntity(user)
                .recessStart(start)
                .recessEnd(end)
                .recessReason(req.getRecessReason() != null ? req.getRecessReason() : existing.getRecessReason())
                .build();

        RecessEntity saved = recessRepository.save(updated);
        return toDto(saved, user);
    }

    @Transactional
    public void deleteRecess(Long id) {
        RecessEntity entity = recessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("휴진 정보를 찾을 수 없습니다."));
        recessRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<RecessResponse> listByRole(RoleType role) {
        return recessRepository.findByUserEntity_Role(role).stream()
                .map(e -> toDto(e, e.getUserEntity()))
                .collect(Collectors.toList());
    }

    private void validateTimeSlot(LocalDateTime start, LocalDateTime end) {
        if (start.getMinute() % 30 != 0 || end.getMinute() % 30 != 0) {
            throw new RuntimeException("시간은 반드시 30분 단위여야 합니다.");
        }
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new RuntimeException("종료 시간은 시작 시간 이후여야 합니다.");
        }
    }

    private LocalDateTime adjustToHalfHour(LocalDateTime time) {
        int minute = time.getMinute();
        int adjustedMinute = (minute < 30) ? 0 : 30;
        return time.withMinute(adjustedMinute).withSecond(0).withNano(0);
    }

    private RecessResponse toDto(RecessEntity entity, UserEntity user) {
        return new RecessResponse(
                entity.getId(),
                user.getId(),
                user.getName(),
                user.getRole().name(),
                entity.getRecessStart(),
                entity.getRecessEnd(),
                entity.getRecessReason(),
                entity.getRecessCreate()
        );
    }
}

