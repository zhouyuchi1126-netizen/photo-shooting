# Public Gallery + Admin Email Verification Login Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement.

**Goal:** Make gallery publicly accessible, restrict admin to email-code login, hide register.

**Architecture:** Frontend routing changes + backend email verification. No login required for browsing. Admin logs in via `/admin` using email verification code. QQ SMTP sends 6-digit code.

**Tech Stack:** Vue 3 + Element Plus (frontend), Spring Boot 3 + spring-boot-starter-mail (backend), QQ Mail SMTP

---

### Files to Create/Modify

| Action | File | Purpose |
|--------|------|---------|
| Modify | `frontend/src/router/index.js` | Public routes for Home/Gallery, admin guard for /admin |
| Modify | `frontend/src/App.vue` | Remove forced login, adjust nav bar |
| Modify | `frontend/src/components/AdminView.vue` | Add admin login state, remove title |
| Modify | `frontend/src/components/LoginView.vue` | Remove register link |
| Modify | `frontend/src/api/auth.js` | Add sendCode + verifyCode APIs |
| Modify | `backend/pom.xml` | Add spring-boot-starter-mail |
| Modify | `backend/src/main/resources/application.properties` | QQ SMTP + admin email config |
| Create | `backend/.../service/EmailService.java` | Generate & send verification code |
| Create | `backend/.../controller/AdminAuthController.java` | Send-code and verify-code endpoints |
| Create | `backend/.../dto/AdminCodeRequest.java` | Code request DTO |
| Create | `backend/.../dto/AdminCodeResponse.java` | Code response DTO |

---

### Task 1: Public access routing

**Files:** `frontend/src/router/index.js`

- [ ] **Edit router guards**

Remove Login redirect for Home/Gallery. Only protect `/admin`:

```js
import { createWebHistory, createRouter } from 'vue-router';
import LoginView from '../components/LoginView.vue';
import RegisterView from '../components/RegisterView.vue';
import HomeView from '../components/HomeView.vue';
import GalleryView from '../components/GalleryView.vue';
import AdminView from '../components/AdminView.vue';

const routes = [
  { path: '/', name: 'Login', component: LoginView },
  { path: '/register', name: 'Register', component: RegisterView },
  { path: '/home', name: 'Home', component: HomeView },
  { path: '/gallery/:groupId', name: 'Gallery', component: GalleryView, props: true },
  { path: '/admin', name: 'Admin', component: AdminView }
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  const user = (() => { try { return JSON.parse(localStorage.getItem('user')); } catch { return null; } })();
  const isAdmin = user?.role === 'admin';

  if (to.name === 'Admin' && !isAdmin) return next({ name: 'Login' });
  if ((to.name === 'Login' || to.name === 'Register') && isAdmin) return next({ name: 'Admin' });
  if ((to.name === 'Login' || to.name === 'Register') && user && !isAdmin) return next({ name: 'Home' });

  next();
});

export default router;
```

---

### Task 2: Nav bar for public visitors

**Files:** `frontend/src/App.vue`

The template already handles logged-in vs not. The key change is the `logout()` redirect — change it to go to `/home` instead of `/`:

```js
function logout() {
  localStorage.removeItem('user');
  router.push('/home');
}
```

Also ensure the `user` computed works without throwing when localStorage is empty (it already does). No other template changes needed — the `v-if="user"` already falls through to the globe icon for non-logged-in visitors.

---

### Task 3: LoginView — remove register link

**Files:** `frontend/src/components/LoginView.vue`

Delete line 33:
```html
<p class="help-text">{{ t('noAccount') }} <router-link to="/register" class="highlight-link">{{ t('goToRegister') }}</router-link></p>
```
Keep the `/register` route accessible by manual URL.

---

### Task 4: AdminView — admin login inline + remove title

**Files:** `frontend/src/components/AdminView.vue`

- [ ] **Remove "相册管理" title** — delete line 4 `<h1>相册管理</h1>`

- [ ] **Add admin login state** — At the top of AdminView, show a login form if not authenticated as admin.

Wrap the entire existing admin panel template inside a `v-if="isAdminAuthed"` block, and add an admin login form in the `v-else` block.

Template additions at the top of AdminView (before the admin-header div):
```html
    <!-- 管理员验证码登录 -->
    <div class="auth-view" v-if="!isAdminAuthed">
      <div class="auth-card">
        <h1>管理员登录</h1>
        <p style="color:#666;font-size:0.9rem;margin-bottom:1rem;text-align:center;">
          验证码将发送到管理员邮箱
        </p>
        <div class="login-form" style="display:grid;gap:1rem;">
          <el-button type="primary" :loading="sending" @click="handleSendCode" round>
            {{ sending ? '发送中...' : '发送验证码' }}
          </el-button>
          <el-input v-model="adminCode" placeholder="请输入验证码" maxlength="6" style="text-align:center;font-size:1.2rem;letter-spacing:0.3em" />
          <el-button type="primary" :disabled="adminCode.length !== 6" :loading="verifying" @click="handleVerifyCode" round>
            {{ verifying ? '验证中...' : '登录' }}
          </el-button>
        </div>
        <p class="help-text" style="margin-top:1rem;text-align:center;color:#999;font-size:0.85rem;">
          <router-link to="/home">返回首页</router-link>
        </p>
      </div>
    </div>
```

Script additions in `AdminView.vue`:
```js
import { sendAdminCode, verifyAdminCode } from '../api/auth';

const isAdminAuthed = computed(() => {
  try {
    const u = JSON.parse(localStorage.getItem('user'));
    return u?.role === 'admin';
  } catch { return false; }
});

const sending = ref(false);
const verifying = ref(false);
const adminCode = ref('');

async function handleSendCode() {
  sending.value = true;
  try {
    const res = await sendAdminCode();
    if (res.success) {
      ElMessage.success(res.message);
    } else {
      ElMessage.error(res.message);
    }
  } catch { ElMessage.error('发送失败，请检查服务器配置'); }
  sending.value = false;
}

async function handleVerifyCode() {
  if (adminCode.value.length !== 6) return;
  verifying.value = true;
  try {
    const res = await verifyAdminCode(adminCode.value);
    if (res.success) {
      localStorage.setItem('user', JSON.stringify(res));
      ElMessage.success('登录成功');
      await loadGroups();
    } else {
      ElMessage.error(res.message);
    }
  } catch { ElMessage.error('验证失败'); }
  verifying.value = false;
}
```

Import `sendAdminCode, verifyAdminCode` at the top of script.

Add CSS for the auth form in the style section:
```css
.auth-view { display: grid; place-items: center; min-height: 70vh; }
.auth-card { width: min(420px, 100%); box-sizing: border-box; border: 1px solid #e6e6e6; padding: 2rem; border-radius: 8px; box-shadow: 0 0 24px rgba(0,0,0,0.04); }
.auth-card h1 { margin: 0 0 0.5rem; font-size: 1.8rem; text-align: center; }
```

---

### Task 5: Add mail dependency + config

**Files:** `backend/pom.xml`, `backend/src/main/resources/application.properties`

- [ ] **Add to pom.xml** (before `</dependencies>`):

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
```

- [ ] **Append to application.properties:**

```properties
# ===========================================
# 管理员邮箱验证码登录配置（QQ邮箱SMTP）
# ===========================================
app.admin.email=386187435@qq.com
spring.mail.host=smtp.qq.com
spring.mail.port=587
spring.mail.username=386187435@qq.com
spring.mail.password=你的QQ邮箱授权码
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

---

### Task 6: Backend email service

- [ ] **Create `backend/src/main/java/com/example/photoshoot/service/EmailService.java`:**

```java
package com.example.photoshoot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String adminEmail;
    private String currentCode = "";
    private long codeExpireTime = 0;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.admin.email}") String adminEmail) {
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
    }

    public String generateAndSendCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        currentCode = String.valueOf(code);
        codeExpireTime = System.currentTimeMillis() + 5 * 60 * 1000;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(adminEmail);
            msg.setSubject("管理员登录验证码");
            msg.setText("您的验证码是: " + currentCode + "\n有效期5分钟。");
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
        return "验证码已发送到管理员邮箱";
    }

    public boolean verifyCode(String code) {
        if (System.currentTimeMillis() > codeExpireTime) return false;
        return currentCode.equals(code);
    }
}
```

- [ ] **Create `backend/src/main/java/com/example/photoshoot/dto/AdminCodeRequest.java`:**

```java
package com.example.photoshoot.dto;
public class AdminCodeRequest {
    private String code;
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
```

- [ ] **Create `backend/src/main/java/com/example/photoshoot/dto/AdminCodeResponse.java`:**

```java
package com.example.photoshoot.dto;
public class AdminCodeResponse {
    private boolean success;
    private String message;
    private String token;
    private String username;
    private String role;
    private String displayName;
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean s) { success = s; }
    public String getMessage() { return message; }
    public void setMessage(String m) { message = m; }
    public String getToken() { return token; }
    public void setToken(String t) { token = t; }
    public String getUsername() { return username; }
    public void setUsername(String u) { username = u; }
    public String getRole() { return role; }
    public void setRole(String r) { role = r; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String d) { displayName = d; }
}
```

- [ ] **Create `backend/.../controller/AdminAuthController.java`:**

```java
package com.example.photoshoot.controller;

import com.example.photoshoot.dto.AdminCodeRequest;
import com.example.photoshoot.dto.AdminCodeResponse;
import com.example.photoshoot.model.User;
import com.example.photoshoot.service.EmailService;
import com.example.photoshoot.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final EmailService emailService;
    private final UserService userService;
    private final String adminEmail;

    public AdminAuthController(EmailService emailService, UserService userService,
                                @Value("${app.admin.email}") String adminEmail) {
        this.emailService = emailService;
        this.userService = userService;
        this.adminEmail = adminEmail;
    }

    @PostMapping("/send-code")
    public AdminCodeResponse sendCode() {
        AdminCodeResponse resp = new AdminCodeResponse();
        try {
            String msg = emailService.generateAndSendCode();
            resp.setSuccess(true);
            resp.setMessage(msg);
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setMessage("发送失败: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping("/verify-code")
    public AdminCodeResponse verifyCode(@RequestBody AdminCodeRequest request) {
        AdminCodeResponse resp = new AdminCodeResponse();
        if (request.getCode() == null || request.getCode().isBlank()) {
            resp.setSuccess(false); resp.setMessage("请输入验证码"); return resp;
        }
        if (!emailService.verifyCode(request.getCode().trim())) {
            resp.setSuccess(false); resp.setMessage("验证码错误或已过期"); return resp;
        }
        User admin = userService.findByUsername("admin");
        if (admin == null) {
            resp.setSuccess(false); resp.setMessage("管理员账号不存在"); return resp;
        }
        resp.setSuccess(true);
        resp.setUsername("admin");
        resp.setDisplayName("管理员");
        resp.setRole("admin");
        resp.setToken("admin-token");
        resp.setMessage("验证成功");
        return resp;
    }
}
```

---

### Task 7: Frontend auth API

**Files:** `frontend/src/api/auth.js`

Add these functions at the end of the file:
```js
const adminAuthApi = axios.create({ baseURL: '/api/admin/auth' });

export async function sendAdminCode() {
  const response = await adminAuthApi.post('/send-code');
  return response.data;
}

export async function verifyAdminCode(code) {
  const response = await adminAuthApi.post('/verify-code', { code });
  return response.data;
}
```

---

### Task 8: Rebuild and verify

- [ ] **Rebuild backend:**

```bash
cd /Users/yuchizhou/Project/Vibe_Coding_Project/photo_shooting/backend
./apache-maven-3.9.6/bin/mvn clean package -DskipTests
```

- [ ] **Restart backend:**

```bash
kill $(lsof -ti:8081) 2>/dev/null
java -jar target/photo-shooting-backend-0.0.1-SNAPSHOT.jar
```

- [ ] **Verify:** Test that `/home` and `/gallery/:groupId` load without login, `/admin` shows code login, register link is gone from login page.

---

### Implementation Order

1. Task 1 (router) → Task 2 (App.vue nav) → Task 3 (LoginView register link) → Task 4 (AdminView login + title)
2. Task 5 (mail dependency + config) → Task 6 (backend email service + controller) → Task 7 (frontend API)
3. Task 8 (rebuild + verify)

Tasks within each group can be done in parallel (frontend changes → backend changes → build).
