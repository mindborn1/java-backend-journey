package com.mindborn.jianzhicommunity.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 *
 * 作用：负责生成、解析、验证 JWT Token。
 *
 * JWT 结构：Header.Payload.Signature
 *   - Header：声明类型和签名算法
 *   - Payload：存放数据（用户ID、用户名、过期时间等）
 *   - Signature：签名，防止篡改
 *
 * 为什么用 @Component？
 *   因为 JwtUtil 需要被 Spring 管理，才能通过 @Autowired 注入到其他类中。
 */
@Component
public class JwtUtil {


    /**
     * 密钥（至少 256 位，也就是 32 个字符以上）
     * 实际项目中应该放在配置文件或环境变量里，不要硬编码
     */
    private static final String SECRET = "jianzhi-community-secret-key-2024-very-long-and-safe";

    /**
     * Token 有效期：7 天（单位：毫秒）
     */
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    /**
     * 根据密钥生成 HMAC-SHA 算法的 SecretKey 对象
     *
     * Keys.hmacShaKeyFor() 是 jjwt 推荐的安全密钥生成方式，
     * 它会自动处理密钥长度，确保符合算法要求。
     */
    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT 字符串
     */
    public String generateToken(Long userId , String username) {
        //当前时间
        Date now = new Date();
        // 过期时间 = 当前时间 + 有效期
        Date expiryDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .subject(String.valueOf(userId))          // 主题：存放用户ID
                .claim("username", username)        // 自定义声明：存放用户名
                .issuedAt(now)                            // 签发时间
                .expiration(expiryDate)                   // 过期时间
                .signWith(key)                            // 用密钥签名
                .compact();                               // 生成字符串
    }

    /**
     * 从 Token 中解析出 Claims（载荷数据）
     *
     * @param token JWT 字符串
     * @return Claims 对象，包含用户ID、用户名、过期时间等
     * @throws JwtException Token 无效或过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
        // 用密钥验证签名
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取用户ID
     *
     * 用户ID存在 subject 字段里，所以用 getSubject() 获取。
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }
    /**
     * 从 Token 中提取用户名
     *
     * 用户名是自定义 claim，用 get("username", String.class) 获取。
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 验证 Token 是否有效
     *
     * 原理：尝试解析 Token，如果解析成功说明有效；
     *      如果抛出异常（过期、签名错误、格式不对），说明无效。
     *
     * @param token JWT 字符串
     * @return true 有效，false 无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException |
        IllegalArgumentException e) {
            // Token 过期、签名错误、格式错误都会进这里
            return false;
        }
    }
}
