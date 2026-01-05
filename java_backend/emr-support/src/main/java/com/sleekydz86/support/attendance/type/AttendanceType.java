package com.sleekydz86.support.attendance.type;

/**
 * 근태 타입
 * 업무와 관련된 근태 상태를 구분하는 열거형
 */
public enum AttendanceType {
    /**
     * 교육 - 교육 및 연수 활동
     */
    EDUCATION,
    
    /**
     * 자리비움 - 자리를 비운 상태
     */
    AWAY,
    
    /**
     * 식사 - 식사 시간
     */
    MEAL,
    
    /**
     * 출근 - 근무 시작
     */
    CHECK_IN,
    
    /**
     * 퇴근 - 근무 종료
     */
    CHECK_OUT,
    
    /**
     * 외근 - 외부 업무 수행
     */
    FIELD_WORK
}

