package xCloud.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @Description JWT 工具类：生成/解析/校验令牌（HS256）
 * @Author Andy Fan
 * @Date 2026/08/08
 * @ClassName JwtUtil
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-minutes}")
    private long expireMinutes;

    private SecretKey secretKey;

    // 密钥仅初始化一次，避免每次生成/解析都重新构建，兼顾性能与线程安全
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成令牌
     *
     * @param userId   用户ID（作为subject）
     * @param userName 用户账号（放入claim）
     * @return JWT字符串
     */
    public String generateToken(Long userId, String userName) {
        long now = System.currentTimeMillis();
        Date expiration = new Date(now + expireMinutes * 60 * 1000);
        return Jwts.builder().setSubject(String.valueOf(userId)).claim("userName", userName).setIssuedAt(new Date(now)).setExpiration(expiration).signWith(secretKey, SignatureAlgorithm.HS256).compact();
    }

    /**
     * 解析令牌，非法/过期返回null，避免抛异常打断上层流程
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.warn("JWT解析失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : Long.valueOf(claims.getSubject());
    }

    /**
     * 校验令牌是否有效
     */
    public boolean validate(String token) {
        return parseToken(token) != null;
    }
}
