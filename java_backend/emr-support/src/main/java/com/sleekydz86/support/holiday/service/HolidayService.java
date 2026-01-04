package com.sleekydz86.support.holiday.service;

import com.sleekydz86.support.holiday.dto.HolidayRequest;
import com.sleekydz86.support.holiday.dto.HolidayResponse;
import com.sleekydz86.support.holiday.entity.HolidayEntity;
import com.sleekydz86.support.holiday.repository.HolidayRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;

    @Transactional
    public HolidayResponse registerHoliday(HolidayRequest req) {
        HolidayEntity entity = HolidayEntity.builder()
                .holidayDate(req.getHolidayDate())
                .holidayNational(req.getHolidayNational())
                .holidayReason(req.getHolidayReason())
                .build();

        HolidayEntity saved = holidayRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public HolidayResponse updateHoliday(Long id, HolidayRequest req) {
        HolidayEntity existing = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("휴일 정보를 찾을 수 없습니다."));

        HolidayEntity updated = HolidayEntity.builder()
                .id(existing.getId())
                .holidayDate(req.getHolidayDate() != null ? req.getHolidayDate() : existing.getHolidayDate())
                .holidayNational(req.getHolidayNational() != null ? req.getHolidayNational() : existing.getHolidayNational())
                .holidayReason(req.getHolidayReason() != null ? req.getHolidayReason() : existing.getHolidayReason())
                .build();

        HolidayEntity saved = holidayRepository.save(updated);
        return toDto(saved);
    }

    @Transactional
    public void deleteHoliday(Long id) {
        HolidayEntity entity = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("휴일 정보를 찾을 수 없습니다."));
        holidayRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> listByPeriod(String period, String number) {
        if (!validatePeriod(period, number)) {
            return Collections.emptyList();
        }

        List<HolidayEntity> holidayList = getHolidayDateByNumber(period, number);
        if (holidayList.isEmpty()) {
            return Collections.emptyList();
        }

        return holidayList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private Boolean validatePeriod(String period, String number) {
        return ("day".equals(period) && number.length() == 8)
                || ("year".equals(period) && number.length() == 4);
    }

    private List<HolidayEntity> getHolidayDateByNumber(String period, String number) {
        if ("day".equals(period)) {
            // YYYYMMDD 형식 파싱
            int year = Integer.parseInt(number.substring(0, 4));
            int month = Integer.parseInt(number.substring(4, 6));
            int day = Integer.parseInt(number.substring(6, 8));

            LocalDate targetDate = LocalDate.of(year, month, day);
            HolidayEntity holiday = holidayRepository.findByHolidayDate(targetDate);

            return holiday != null ? List.of(holiday) : Collections.emptyList();
        } else if ("year".equals(period)) {
            // YYYY 형식 파싱
            int year = Integer.parseInt(number);
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);

            return holidayRepository.findAllByHolidayDateBetween(start, end);
        }

        return Collections.emptyList();
    }

    private HolidayResponse toDto(HolidayEntity entity) {
        return new HolidayResponse(
                entity.getId(),
                entity.getHolidayDate(),
                entity.getHolidayNational(),
                entity.getHolidayReason()
        );
    }
}

