package com.sleekydz86.emrclinical.types;

public enum PrescriptionStatus {
    PENDING,        // 대기 중
    PRESCRIBED,     // 처방 완료
    DISPENSED,      // 조제 완료
    CANCELLED       // 취소
}