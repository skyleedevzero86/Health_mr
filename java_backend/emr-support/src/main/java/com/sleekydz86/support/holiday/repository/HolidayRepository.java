package com.sleekydz86.support.holiday.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.holiday.entity.HolidayEntity;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends BaseRepository<HolidayEntity, Long> {

    HolidayEntity findByHolidayDate(LocalDate holidayDate);

    List<HolidayEntity> findAllByHolidayDateBetween(LocalDate start, LocalDate end);
}

