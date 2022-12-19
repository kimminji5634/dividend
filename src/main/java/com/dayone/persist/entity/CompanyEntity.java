package com.dayone.persist.entity;

import com.dayone.model.Company;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity(name = "COMPANY") // 테이블명
@Getter // 멤버 변수의 값을 가져옴
@ToString // 인스턴스를 출력하는데 편의를 높여줌
@NoArgsConstructor // argument(받는값) 하나도 없는 생성자 만들어줌
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto_increment
    private Long id;

    @Column(unique = true) // 중복 없다는 뜻
    private String ticker;

    private String name;

    public CompanyEntity(Company company) {
        this.ticker = company.getTicker();
        this.name = company.getName();
    }
}
