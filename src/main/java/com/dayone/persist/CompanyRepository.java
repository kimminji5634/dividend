package com.dayone.persist;

import com.dayone.persist.entity.CompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByTicker(String ticker); // ticker 존재 여부를 받아옴

    // Optional 로 감싸는 이유 : nullPointException 방지, 값이 없는 경우에 대한 처리도 깔끔하게 해 줌
    Optional<CompanyEntity> findByName(String name); // 회사명을 기준

    Optional<CompanyEntity> findByTicker(String ticker);

    // LIKE 연산자를 쓸 준비
    Page<CompanyEntity> findByNameStartingWithIgnoreCase(String s, Pageable pageable);
}
