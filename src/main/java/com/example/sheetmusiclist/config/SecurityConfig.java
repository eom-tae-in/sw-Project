package com.example.sheetmusiclist.config;


import com.example.sheetmusiclist.config.jwt.JwtAccessDeniedHandler;
import com.example.sheetmusiclist.config.jwt.JwtAuthenticationEntryPoint;
import com.example.sheetmusiclist.config.jwt.JwtSecurityConfig;
import com.example.sheetmusiclist.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;

import java.util.List;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**", "/swagger/**");
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // CSRF 설정 Disable
        http.csrf().disable();

        // CORS
        http.cors().configurationSource(request -> {
            var cors = new CorsConfiguration();
            cors.setAllowedOrigins(List.of("http://localhost:3000"));
            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
            cors.setAllowedHeaders(List.of("*"));
            return cors;
        });
        http
                .authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll();


        http
                // exception handling 할 때 우리가 만든 클래스를 추가
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)


                // 시큐리티는 기본적으로 세션을 사용
                // 여기서는 세션을 사용하지 않기 때문에 세션 설정을 Stateless 로 설정
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // 로그인, 회원가입 API 는 토큰이 없는 상태에서 요청이 들어오기 때문에 permitAll 설정
                .and()
                .authorizeRequests()

                .antMatchers("/swagger-ui/**", "/v3/**", "/test").permitAll() // swagger
                .antMatchers(HttpMethod.GET, "/pdfs/**").permitAll() // images
                .antMatchers("/api/sign-up/**", "/api/sign-in", "/api/reissue").permitAll() // auth



                .antMatchers(HttpMethod.POST, "/api/sheetmusics").access("hasRole('ROLE_USER')")
                .antMatchers(HttpMethod.GET, "/api/sheetmusics").access("hasRole('ROLE_USER')")
                .antMatchers(HttpMethod.GET, "/api/test/download/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/sheetmusics/pdfs/test/**").access("hasRole('ROLE_USER')")
                .antMatchers(HttpMethod.GET, "/api/sheetmusics/title/{title}").access("hasRole('ROLE_USER')")
                .antMatchers(HttpMethod.GET, "/api/sheetmusics/writer/{writer}").access("hasRole('ROLE_USER')")
                .antMatchers(HttpMethod.GET, "/api/sheetmusics/{id}").access("hasRole('ROLE_USER')")
                .antMatchers(HttpMethod.PUT, "/api/sheetmusics/{id}").access("hasRole('ROLE_USER')")
                .antMatchers(HttpMethod.DELETE, "/api/sheetmusics/{id}").access("hasRole('ROLE_USER')")

                .antMatchers(HttpMethod.POST, "/api/reviews").access("hasRole('ROLE_USER')")
                .antMatchers(HttpMethod.GET, "/api/music/{id}/reviews").access("hasRole('ROLE_USER')")
                .antMatchers(HttpMethod.PUT, "/api/reviews/{id}").access("hasRole('ROLE_USER')")
                .antMatchers(HttpMethod.DELETE, "/api/reviews/{id}").access("hasRole('ROLE_USER')")

                .anyRequest().hasAnyRole("ROLE_ADMIN")
//                .anyRequest().authenticated() // 나머지는 전부 인증 필요
//                .anyRequest().permitAll()   // 나머지는 모두 그냥 접근 가능

                // JwtFilter 를 addFilterBefore 로 등록했던 JwtSecurityConfig 클래스를 적용
                .and()
                .apply(new JwtSecurityConfig(tokenProvider));
    }
}