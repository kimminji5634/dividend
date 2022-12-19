package com.dayone.web;

import com.dayone.model.Company;
import com.dayone.model.constants.CacheKey;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company") //아래의 /company 계속 겹치는데 맨 위로 빼줌
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private  final CacheManager redisCacheManager;

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
        // 회사 이름이 저장된 trie에서 조회
        var result = this.companyService.getCompanyNamesByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasRole('READ')")
    // Pageable 값이 임의로 변경 안되도록 final 붙임
    public ResponseEntity<?> searchCompany(final Pageable pageable) { //Pageable springboot..domain 꺼
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    @PostMapping
    @PreAuthorize("hasRole('WRITE')") // 쓰기 권한이 있는 유저만 아래 API를 호출할 수 있도록
    // input 으로 저장할 ticker 명을 받을 것임
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        String ticker = request.getTicker().trim(); // 앞 뒤 공백 없앰
        if (ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("ticker is empty");
        }

        Company company = this.companyService.save(ticker);
        // trie에도 회사 저장케 함
        this.companyService.addAutocompleteKeyword(company.getName());
        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{ticker}")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker){ // 지워줄 회사의 ticker 명 받음
        String companyName = this.companyService.deleteCompany(ticker);
        return null;
    }

    // 위에서 회사 삭제해주면 캐시에서도 삭제해줘야 한다
    public void clearFinanceCache(String companyName) {
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }
}
