package com.wiseai.assignment.modules.user.adapter.web.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.user.adapter.web.request.SelfSignUpWebRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "User", description = "사용자 회원가입 API")
@RequestMapping("/api/v1/users")
public interface UserSignUpApi {

  @Operation(
      summary = "자체 회원가입",
      description = "이메일, 비밀번호, 이름, 연락처를 입력해 사내 사용자 계정을 생성합니다.",
      security = {})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "회원가입 성공", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
      })
  @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<SuccessResponse<Void>> signUpUserSelf(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              description = "회원가입 정보",
              content = @Content(schema = @Schema(implementation = SelfSignUpWebRequest.class)))
          @Valid
          @RequestBody
          SelfSignUpWebRequest webRequest,
      HttpServletRequest request,
      HttpServletResponse response);
}
