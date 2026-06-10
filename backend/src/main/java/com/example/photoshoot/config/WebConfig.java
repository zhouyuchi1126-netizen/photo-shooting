package com.example.photoshoot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.storage.local.path}")
    private String storagePath;

    @Value("${app.cors.allowed-origins}")
    private String allowedOriginsStr;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOriginsStr.split(","))
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dir = storagePath.endsWith("/") ? storagePath : storagePath + "/";
        registry.addResourceHandler("/images/**")
            .addResourceLocations("file:" + dir);
    }

    // TODO: 防盗链 — 域名申请后取消注释并替换为你自己的域名
    // @Override
    // public void addInterceptors(InterceptorRegistry registry) {
    //     String domain = "https://你的域名.com";
    //     registry.addInterceptor(new org.springframework.web.servlet.HandlerInterceptor() {
    //         @Override
    //         public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
    //             String referer = req.getHeader("Referer");
    //             if (referer == null || referer.isBlank()) return true;
    //             if (!referer.startsWith("http://localhost") && !referer.startsWith(domain)) {
    //                 resp.setContentType("image/png");
    //                 resp.getOutputStream().write(new byte[]{ (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });
    //                 resp.getOutputStream().flush();
    //                 return false;
    //             }
    //             return true;
    //         }
    //     }).addPathPatterns("/images/**");
    // }
}
