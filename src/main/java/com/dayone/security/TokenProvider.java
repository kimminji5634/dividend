package com.dayone.security;


import java.util.Date;
import java.util.List;

import com.dayone.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final String KEY_ROLES = "roles";
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1시간

    private final MemberService memberService;

    // lombok value 가 아닌 springframework.beans.factory.annotation 으로 가져와야 함
    @Value("{spring.jwt.secret}")
    private String secretKey;

    // 토큰을 생성하는 메소드
    public String generateToken(String username, List<String> roles) {
        // 사용자의 권한 정보 저장
        Claims claims = Jwts.claims().setSubject(username); // username 저장
        // 키 벨류 형태로 저장해야 함
        // claims.put("roles", roles); 이렇게 쓰는 것보다 상수값을 가져와서 넣어주는게 좋음
        claims.put(KEY_ROLES, roles);

        var now = new Date(); // 토큰 생성한 시간
        var expiredDate = new Date(now.getTime() +  TOKEN_EXPIRE_TIME); // 토큰 만료시간 정해줘야 함

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 토큰 생성 시간
                .setExpiration(expiredDate) // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS512, this.secretKey) // 사용할 암호화 알고리즘, 비밀키
                .compact();

    }

    // jwt 토큰으로부터 인증 정보를 가져오는 메소드
    public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰이 유효한지 확인하기 위해 파싱
    private Claims parseClaims(String token) {
        // 토큰 만료 시간 지났는데 토큰을 파싱하려고 하면 expiredException 날 수 있으므로 try~ 에러 핸들링 해주기
        try {
            // claim 정보를 가져옴
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }


    // username 구하기
    public String getUsername(String token) {
        return this.parseClaims(token).getSubject(); //위에서 setSubject 해준 username 리턴됨
    }

    // 토큰이 유효한가
    public boolean validToken(String token) {
        if (!StringUtils.hasText(token)) return false; // 토큰 값이 비어있다면

        var claims = this.parseClaims(token);
        return claims.getExpiration().before(new Date()); // 토큰 만료시간이 현재 시간 이전인지!
    }
}
