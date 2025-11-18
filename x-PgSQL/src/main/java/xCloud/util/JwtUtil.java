package xCloud.util;

import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/17 14:28
 * @ClassName JwtUtil
 */
@Component
public class JwtUtil {
    private final String secret = "replace_with_secure_secret"; // 放到 config
    private final long expiration = 1000L * 60 * 60 * 24; // 1 day

    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(now)
//                .setExpiration(exp)
//                .signWith(SignatureAlgorithm.HS256, secret)
//                .compact();
        return null;
    }

}
