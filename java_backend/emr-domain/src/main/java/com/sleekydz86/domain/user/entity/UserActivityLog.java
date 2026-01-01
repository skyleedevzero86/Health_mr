package com.sleekydz86.domain.user.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity(name = "user_activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String activityType;

    @Column(nullable = false)
    private LocalDateTime activityTime;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    private String description;
}

