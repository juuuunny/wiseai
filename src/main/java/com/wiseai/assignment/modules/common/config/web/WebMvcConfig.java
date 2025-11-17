package com.wiseai.assignment.modules.common.config.web;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
  private final MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter;

  /**
   * 가장 앞에 추가해 Jackson의 default converter보다 우선되게 하여 multipart/form-data에서의 http message converter에서의
   * 문제를 해결한다.
   */
  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(0, multipartJackson2HttpMessageConverter);
  }

  /**
   * Pageable 파라미터 처리를 위한 리졸버를 추가한다.
   *
   * @param resolvers 리졸버 리스트
   */
  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(pageableHandlerMethodArgumentResolver());
  }

  @Bean
  public PageableHandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver() {
    return new PageableHandlerMethodArgumentResolver();
  }
}
