package com.l2kb.runhome.domain.user.controller;

import com.l2kb.runhome.domain.user.dto.*;
import com.l2kb.runhome.domain.user.service.UserService;
import com.l2kb.runhome.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "닉네임으로 회원가입합니다. 비밀번호(4자리)와 위치 정보는 선택입니다.")
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity
                .created(URI.create("/api/users/me"))
                .body(ApiResponse.created(response));
    }

    @Operation(summary = "로그인", description = "닉네임으로 로그인합니다. 비밀번호가 설정된 경우 비밀번호도 필요합니다. 성공 시 userId를 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(@RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.login(request)));
    }

    @Operation(summary = "전체 프로필 조회", description = "모든 사용자의 닉네임과 선호 구단을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers()));
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, description = "요청 사용자 ID", required = true)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUser(userId)));
    }

    @Operation(summary = "선호 구단 변경", description = "사용자의 선호 KBO 구단을 변경합니다.")
    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, description = "요청 사용자 ID", required = true)
    @PatchMapping("/me/team")
    public ResponseEntity<ApiResponse<UserResponse>> updateFavoriteTeam(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserUpdateTeamRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateFavoriteTeam(userId, request)));
    }

}
