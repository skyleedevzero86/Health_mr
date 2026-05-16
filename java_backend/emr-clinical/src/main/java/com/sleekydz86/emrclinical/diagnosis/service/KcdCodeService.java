package com.sleekydz86.emrclinical.diagnosis.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.emrclinical.diagnosis.entity.KcdCodeEntity;
import com.sleekydz86.emrclinical.diagnosis.repository.KcdCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KcdCodeService {

    private final KcdCodeRepository kcdCodeRepository;

    public KcdCodeEntity findByCode(String code) {
        return kcdCodeRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("KCD 코드를 찾을 수 없습니다: " + code));
    }

    public Page<KcdCodeEntity> searchByKeyword(String keyword, Pageable pageable) {
        return kcdCodeRepository.searchByKeyword(keyword, pageable);
    }

    public Page<KcdCodeEntity> findByCategory(String category, Pageable pageable) {
        return kcdCodeRepository.findByCategory(category, pageable);
    }
}
