package com.dayone.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// api 호출 시 filter-servlet-intercepter-aop layer-controller 코드 실행
// OncePerRequestFilter을 상속받으면 한 요청당 아래가 제일 먼저 한번 실행됨
// 요청들어올 때 토큰이 유효한지 확인하는 작업을 해주겠다
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 토큰은 http header에 포함되는데 어떤 키를 기준으로 토큰을 주고 받을지에 대한 키 값임
    public static final String TOKEN_HEADER = "Authorization"; // 키 값
    public static final String TOKEN_PREFIX = "Bearer "; // jwt 토큰 사용하는 경우 붙임

    // 토큰의 유효성 검증위해
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = this.resolveTokenFromRequest(request);

        if (StringUtils.hasText(token) && this.tokenProvider.validToken(token)) { // 토큰 유효하다면
            Authentication auth = this.tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth); // 인증정보를 context에 담음
        }

        filterChain.doFilter(request, response); // 유효하지 않으면 바로 실행
    }

    // 요청 헤더에 토큰이 있는지
    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length()); // prefix 이후를 반환 -> 실제 토큰만 반환
        }

        return null;
    }
}