package com.dayone.web;

/*
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    // 회원가입을 위한 API
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {

        var result = this.memberService.register(request);
        return ResponseEntity.ok(result);
    }

    // 로그인용 API
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {

        var member = this.memberService.authenticate(request);
        // 정상적으로 로그인 되면 토큰 만들어줌
        var token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());
        return ResponseEntity.ok(token);
    }
}
*/
