package com.dayone.service;

import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import java.util.List;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;

    // 스크래핑 코드를 사용하기 위해서, 스크래핑은 데이터를 긁어 오는 역할까지만 함
    // 이렇게 사용하기 위해 YahooFinanceScraper 클래스에 @Component 어노테이션 붙여줌
    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker); // 존재 여부를 boolean으로
        if (exists) { // true 라면
            throw new RuntimeException("already exists ticker -> " + ticker);
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }

    // private 외부에서 호출해서 사용 못함, 우리가 db에 저장하지 않은 데이터만 아래 메소드를 실행시킬 것임
    // CompanyRepository에 메소드 추가
    private Company storeCompanyAndDividend(String ticker) {
        // ticker 를 기준으로 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker); // 회사를 고름
        // scrapCompanyByTicker 메소드에서 회사 정보가 없으면 null 을 구현하도록 되어 있음
        if (ObjectUtils.isEmpty(company)) { // company 정보가 비어있다면 에러 던지면서 메소드 종료
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 스크래핑 결과
        // DividendEntity를 보면 company_id를 가진다 따라서 회사 테이블에 먼저 저장시키고 배당금 테이블에 저장시킴
        // 회사테이블에 먼저 저장, repository에 저장되는 타입은 CompanyEntity 타입이 저장되어야 함
        // 이를 편하게 해주기 위해 CompanyEntity에 메소드 추가 // Company -> CompanyEntity로 바꿔서 저장
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        // model 인스턴스(Dividend)를 엔티티 인스턴스(DividendEntity)로 바꾸는 걸 수월하게 해주기 위해
        // DividendEntity에 메소드 추가 // map은 e를 다른 값으로 매핑해줌
        // for문과 비슷한데 getDividends이 전체이고 하나가 e임
        List<DividendEntity> dividendEntityList = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList()); // .collect를 통해 list 형태로 반환함
        this.dividendRepository.saveAll(dividendEntityList);
        return company;
    }

    // trie에 회사 명을 저장
    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null); // 자동완성 기능만 구현할 것이므로 null을 넣어줌
    }

    // trie에서 회사 명을 찾아옴(조회)
    public List<String> autocomplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream()
                .limit(10) // 회사 저장한게 많아지는 경우 자동완성으로 가져오는 회사 수 제한
                .collect(Collectors.toList());
    }

    // trie에 저장된 키워드 삭제
    public void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    public List<String> getCompanyNamesByKeyword(String keyword) {
        // 자동 완성 10개씩 가져오도록
        Pageable limit = PageRequest.of(0, 10);

        // 키워드 이름으로 시작되는
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream()
                                .map(e -> e.getName())
                                .collect(Collectors.toList());
    }

    public String deleteCompany(String ticker) {
        var company = this.companyRepository.findByTicker(ticker)
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사입니다."));

        // company id에 해당되는 배당금 데이터를 다 지워줄 것임
        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company); // company도 지워줌

        // 자동완성기능의 trie에서도 이름 지워줘야함
        this.deleteAutocompleteKeyword(company.getName());
        return company.getName();
        }
}
