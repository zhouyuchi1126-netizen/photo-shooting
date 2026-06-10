package com.example.photoshoot.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.anti-leech.domains:http://localhost:5173,http://localhost:5174,http://localhost:5175,http://localhost:8081}")
    private String allowedDomainsStr;

    @Value("${app.storage.local.path:images}")
    private String storagePath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173", "http://localhost:5174", "http://localhost:5175")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dir = storagePath.endsWith("/") ? storagePath : storagePath + "/";
        registry.addResourceHandler("/images/**")
            .addResourceLocations("file:" + dir);
    }

    /** 图片防盗链：仅允许指定域名引用 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new org.springframework.web.servlet.HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
                String referer = req.getHeader("Referer");
                // 允许无 Referer 的请求（浏览器直接打开 / 本地开发）
                if (referer == null || referer.isBlank()) return true;

                List<String> allowed = List.of(allowedDomainsStr.split(","));
                boolean ok = allowed.stream().anyMatch(d -> referer.startsWith(d.trim()));
                if (!ok) {
                    resp.setContentType("image/png");
                    resp.getOutputStream().write(new byte[]{ (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });
                    resp.getOutputStream().flush();
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/images/**");
    }
}
