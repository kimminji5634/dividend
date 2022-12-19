package com.dayone.service;

import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.model.ScrapedResult;
import com.dayone.model.constants.CacheKey;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    // 요청이 자주 들어오는가? yes
    // 자주 변경되는 데이터인가? no
    // => 캐싱이 적합함!! => 캐싱 대상이되는 메소드
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);
        // 1. 회사명을 기준으로 회사 정보를 조회
        // findByName 리턴값이 Optional이므로 여기서도 데이터 타입을 맞춰줌!!
        // **.orElseThrow 는 값이 없으면 인자값에 정의한 예외 발생, 값이 있으면 Optional이 벗겨진 알맹이 뱉어냄!!!!
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명입니다."));
        // 2. 조회된 company_id로 배당금 정보를 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());
        // 3. 결과 조합 후 반환
        // ScrapedResult 로 반환하려는데 ScrapedResult 는 Company, List<Dividend>로 리턴하는데 여기서는
        // CompanyEntity, list<DividendEntity>로 리턴한다. 따라서 아래와 같이 가공해야 한다.
        // List가 붙으면 가공하는 방식이 다른데 아래의 2가지 방식이 있다
        /*List<Dividend> dividends = new ArrayList<>();
        for (var entity : dividendEntities) {
            dividends.add(Dividend.builder()
                                .date(entity.getDate())
                                .dividend(entity.getDividend())
                                .build());
        }*/
        // 위의 dividends와 아래의 dividends 는 같은 것임 -> 하나 골라 쓰면 됨
        List<Dividend> dividends = dividendEntities.stream()
                                            .map(e -> new Dividend(e.getDate(), e.getDividend()))
                                            .collect(Collectors.toList());

        return new ScrapedResult(new Company(company.getTicker(), company.getName()),
                                    dividends);
    }
}
