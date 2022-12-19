package com.dayone.model;

import com.dayone.persist.entity.MemberEntity;
import lombok.Data;
import java.util.List;

public class Auth {

    @Data
    // 로그인할 때 사용할 클래스 : 로그인할 때 input 정보
    public static class SignIn {
        private String username;
        private String password;
    }

    @Data
    // 회원가입할 때 사용할 클래스
    public static class SignUp {
        private String username;
        private String password;
        // 일반 회원이 가입하는 경로에서는 role 못 정함
        private List<String> roles;

        // SignUp 클래스의 내용을 memberEntity로 바꿀 수 있도록 메소드 하나 추가
        public MemberEntity toEntity() {
            return MemberEntity.builder()
                    .username(this.username)
                    .password(this.password)
                    //.roles(this.roles)
                    .build();
        }
    }
}
