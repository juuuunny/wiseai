package com.wiseai.assignment.modules.auth.adapter.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.auth.adapter.jwt.JwtProperties;
import com.wiseai.assignment.modules.auth.application.port.out.token.ManageRefreshTokenPort;
import com.wiseai.assignment.modules.common.exception.CommonException;
import com.wiseai.assignment.modules.common.status.CommonErrorStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenRedisAdapter implements ManageRefreshTokenPort {
  private final StringRedisTemplate redisTemplate;
  private final JwtProperties jwtProperties;

  /**
   * 리프레시 토큰 키 설정
   *
   * @param userId 유저 id
   * @return 레디스 키
   */
  private String getRefreshTokenKey(String userId) {
    return "refreshToken:user:" + userId;
  }

  /**
   * 지정한 유저 ID에 대한 리프레시 토큰을 Redis에 저장합니다. 토큰은 설정된 만료 기간(일 단위) 동안 유지됩니다.
   *
   * @param userId 리프레시 토큰을 저장할 유저의 ID
   * @param refreshToken 저장할 리프레시 토큰 값
   * @throws CommonException Redis 연결 실패 또는 데이터 접근 오류 발생 시 예외가 발생합니다.
   */
  @Override
  public void saveRefreshToken(String userId, String refreshToken) {
    try {
      // refreshTokenExpirationTime은 밀리초 단위이므로 초 단위로 변환
      long expirationSeconds = jwtProperties.getRefreshTokenExpirationTime() / 1000;
      redisTemplate
          .opsForValue()
          .set(getRefreshTokenKey(userId), refreshToken, expirationSeconds, TimeUnit.SECONDS);
    } catch (RedisConnectionFailureException e) {
      throw new CommonException(CommonErrorStatus.REDIS_CONNECTION_FAILURE);
    } catch (DataAccessException e) {
      throw new CommonException(CommonErrorStatus.DATA_ACCESS_EXCEPTION);
    }
  }

  /**
   * 주어진 유저 ID에 해당하는 리프레시 토큰을 Redis에서 조회하여 반환합니다.
   *
   * @param userId 리프레시 토큰을 조회할 유저의 ID
   * @return 저장된 리프레시 토큰, 존재하지 않으면 null 반환
   * @throws CommonException Redis 연결 실패 또는 데이터 접근 오류 발생 시 예외를 발생시킵니다.
   */
  @Override
  public String getRefreshToken(String userId) {
    try {
      String refreshTokenKey = getRefreshTokenKey(userId);
      return redisTemplate.opsForValue().get(refreshTokenKey);
    } catch (RedisConnectionFailureException e) {
      throw new CommonException(CommonErrorStatus.REDIS_CONNECTION_FAILURE);
    } catch (DataAccessException e) {
      throw new CommonException(CommonErrorStatus.DATA_ACCESS_EXCEPTION);
    }
  }

  /**
   * 지정된 userId에 해당하는 리프레시 토큰을 Redis에서 삭제합니다.
   *
   * @param userId 리프레시 토큰을 삭제할 사용자 ID
   * @throws CommonException Redis 연결 실패 또는 데이터 접근 오류 발생 시 예외가 발생합니다.
   */
  @Override
  public void deleteRefreshToken(String userId) {
    try {
      redisTemplate.delete(getRefreshTokenKey(userId));
    } catch (RedisConnectionFailureException e) {
      throw new CommonException(CommonErrorStatus.REDIS_CONNECTION_FAILURE);
    } catch (DataAccessException e) {
      throw new CommonException(CommonErrorStatus.DATA_ACCESS_EXCEPTION);
    }
  }
}
