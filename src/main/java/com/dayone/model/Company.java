package com.dayone.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Access;

// 어노테이션 편리하다고 마음껏 붙이면 안되는게 값을 변경하지 말아야 하는 값에 대해서 setter기능을 부여하면
// 다른 클래스에서 setter를 사용해서 해당 변수값을 변경할 수 있다
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    //*****CompanyEntity 클래스 사용하지 않고 Company 클래스 만든 이유
    // @Entity 클래스는 db와 직접적으로 연관된 클래스이므로 service 내에서 data를 주고 받는 용도로 써서
    // db가 변경되는게 좋지 않다 => 유지보수 어려움
    // 따라서 model 클래스를 생성해줌!!!
    private String ticker;
    private String name;
}
