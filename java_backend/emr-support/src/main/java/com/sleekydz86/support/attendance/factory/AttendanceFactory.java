package com.sleekydz86.support.attendance.factory;

import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.support.attendance.entity.AttendanceEntity;
import com.sleekydz86.support.attendance.strategy.AttendanceValidationStrategy;
import com.sleekydz86.support.attendance.type.AttendanceType;
import com.sleekydz86.support.attendance.valueobject.AttendanceTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceFactory {

    private final List<AttendanceValidationStrategy> validationStrategies;

    public AttendanceEntity create(
            UserEntity userEntity,
            AttendanceType attendanceType,
            LocalDateTime attendanceTime,
            LocalDateTime endTime,
            String location,
            String remarks
    ) {
        AttendanceValidationStrategy strategy = findStrategy(attendanceType);
        strategy.validate(attendanceTime, endTime, location);

        return AttendanceEntity.builder()
                .userEntity(userEntity)
                .attendanceType(attendanceType)
                .attendanceTime(AttendanceTime.of(attendanceTime))
                .endTime(endTime)
                .location(location)
                .remarks(remarks)
                .build();
    }

    private AttendanceValidationStrategy findStrategy(AttendanceType type) {
        return validationStrategies.stream()
                .filter(strategy -> strategy.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 근태 타입입니다: " + type));
    }
}

