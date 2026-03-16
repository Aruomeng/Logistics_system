package com.logistics.controller;

import com.logistics.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码控制器
 */
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    @GetMapping("/image")
    public Result<Map<String, String>> getCaptcha() {
        // 生成验证码文本
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        // 生成唯一 key
        String captchaKey = UUID.randomUUID().toString().replace("-", "");

        // 存入 Redis，5分钟过期
        @SuppressWarnings("null")
        var ops = redisTemplate.opsForValue();
        ops.set("captcha:" + captchaKey, code.toString(), 5, TimeUnit.MINUTES);

        // 生成图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制验证码文字
        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < CODE_LENGTH; i++) {
            g.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
            g.drawString(String.valueOf(code.charAt(i)), 10 + i * 26, 30);
        }

        // 干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }

        // 噪点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.fillRect(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
        }
        g.dispose();

        // 转 Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            Map<String, String> data = new HashMap<>();
            data.put("captchaKey", captchaKey);
            data.put("captchaImage", "data:image/png;base64," + base64);
            return Result.ok(data);
        } catch (Exception e) {
            return Result.fail("验证码生成失败");
        }
    }

    /**
     * 校验验证码（供 UserService 调用）
     */
    public boolean verify(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null)
            return false;
        Object cached = redisTemplate.opsForValue().get("captcha:" + captchaKey);
        // 使用后删除，防止重复使用
        redisTemplate.delete("captcha:" + captchaKey);
        return cached != null && cached.toString().equalsIgnoreCase(captchaCode);
    }
}
