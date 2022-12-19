package com.dayone.schedular;

import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.model.constants.CacheKey;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j // log.info 와 같은 로깅 기능 사용 가능하게 함 => 개발하면서 사용하는게 중요(어디서 에러 발생했는지 알 수 있음)
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {
    private final CompanyRepository companyRepository; // 회사 목록 조회 위해
    private final DividendRepository dividendRepository; // 배당금 정보 저장 위해
    private final Scraper yahooFinanceScraper;

    // 일정 주기마다 수행
    //@Scheduled(cron = "0 0 0 * * *") // 매일 정각에
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true) // redis 캐시에 finance에 해당하는 데이터 모두 지움
    //@CacheEvict(value = "finance", key = "") redis 캐시에 finance에 해당하는 특정 값 지움
    @Scheduled(cron= "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");
        // 저장되어 있는 회사 목록을 조회
        List< CompanyEntity> companies = this.companyRepository.findAll(); // 모든 회사 목록 가져옴

        // 회사에 대한 배당금 정보를 스크래핑
        for (var company : companies) { // 현재 company 는 CompanyEntitiy 타입
            // 어느 회사에 대한 스크래핑 정보가 출력됐는지 알 수 있음
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(new Company(company.getTicker(), company.getName()));  // Company.builder 로 매칭시킴

            // 스크래핑한 배당금 정보중에 db에 없는 값 저장 => ** 배당금 데이터 중복으로 저장되는 것 막아주기 위해
            // dividendEntity 에서 복합 유니크키 설정해줌
            // repository.saveAll해도 되지만 유니크키 설정했기에 아래와 같이 해주어야 함
            scrapedResult.getDividends().stream() // Dividends 를 하나씩 가져온게 e임
                    // map 을 통해 dividend 모델을 dividendEntity 로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 존재하지 않는 경우에만 엘리먼트를 하나씩 dividendRepository에 저장
                    .forEach(e -> {
                        boolean exist = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exist) {
                            this.dividendRepository.save(e);
                        }
                    });

            // for문 안에서 실행되어야 함!!
            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // for문이 한 번 실행될 때마다 3초 일시정지 => 실행 중인 스레드를 잠시 멈추게 함
            } catch (InterruptedException e) { // InterruptedException 은 인터럽트를 받는 스레드가 blocking될 수 있는 메소드를 실행할 떄 발생
                Thread.currentThread().interrupt();
            }
        }
    }
}
