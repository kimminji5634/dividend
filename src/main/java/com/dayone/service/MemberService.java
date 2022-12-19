package com.dayone.service;

import com.dayone.model.Auth;
import com.dayone.persist.entity.MemberEntity;
import com.dayone.persist.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j // log 쓸 수 있도록
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder; // AppConfig에서 정의
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username) // findByUsername 리턴 타입 Optional로 반환
                // orElseThrow(()로 인해 Optional이 벗겨진 MemberEntity로 리턴됨
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));

        // 해당 메소드 리턴 타입이 UserDetails가 될 수 있는 건 MemberEntity가 상속받고 있기 때문 => 바로 리턴 가능
    }

    // 회원가입
    public MemberEntity register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new RuntimeException("이미 사용중인 아이디입니다.");
        }

        // 사용자 비밀번호를 db에 바로 넣지 않고 암호화 해서 넣기
        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        var result = this.memberRepository.save(member.toEntity());
        // 정상적으로 회원 정보가 저장되었다면 result를 반환
        return result;
    }

    // 로그인 검증
    public MemberEntity authenticate(Auth.SignIn member) {

        var user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));

        // user에 있는 테이블의 비밀번호는 인코딩 된 상태이므로, member로 들어온 값도 인코딩하여 비교해야 한다
        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }
}
