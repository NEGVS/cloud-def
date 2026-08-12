package xCloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @Description Spring Security 配置（Spring Security 6 / Spring Boot 3 写法）
 * 学习阶段：放行 swagger、knife4j、druid、openapi 等基础设施路径，避免被默认随机密码拦截。
 * 生产阶段：把业务接口的 permitAll() 改为 authenticated()，并在 UsernamePasswordAuthenticationFilter 前挂载 JWT 过滤器。
 * @Author Andy Fan
 * @Date 2026/08/08
 * @ClassName SecurityConfig
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ==========【白名单：文档 & 监控相关，永远放行】==========
    private static final String[] WHITELIST = {
            "/doc.html",              // knife4j 文档首页
            "/swagger-ui/**",         // swagger-ui 静态资源
            "/swagger-ui.html",
            "/v3/api-docs/**",        // openapi 描述文件
            "/swagger-resources/**",
            "/webjars/**",            // knife4j/swagger 依赖的前端资源
            "/favicon.ico",
            "/druid/**",              // druid 监控页（其自身另有登录）
            "/actuator/**",           // 健康检查/监控端点
            "/error"                  // 错误页
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离 + 无状态，关闭 csrf（否则 POST 接口会被拦）
                .csrf(csrf -> csrf.disable())
                // 无状态：不创建 session（后续用 JWT 时正是这种模式）
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITELIST).permitAll()
                        // ==========【业务接口】==========
                        // 学习阶段：暂时全部放行，避免没有登录接口时被锁死；
                        // 待 JWT 登录做好后，改为 .anyRequest().authenticated()
                        .anyRequest().permitAll()
                )
                // 关闭默认的表单登录与 httpBasic，避免弹出浏览器登录框
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        // 预留：等做 JWT 时，在这里挂过滤器
        // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ==========【BCrypt密码编码器：注册/登录统一使用，自带盐值，杜绝明文存储】==========
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
