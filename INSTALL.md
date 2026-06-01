## ✅ 安装完成

### Maven 安装
✅ **已安装**：`~/dev/apache-maven-3.9.6`

配置环境变量：
```bash
export PATH="$HOME/dev/apache-maven-3.9.6/bin:$PATH"
```

### 数据库配置
✅ **已配置**：使用 SQLite 进行本地开发
- 数据库文件：`backend/photo_shooting.db`
- 无需额外安装 MySQL

### 项目结构
```
photo_shooting/
├── frontend/          # Vue 3 + Vite 前端应用（端口 5173）
├── backend/           # Spring Boot 后端应用（端口 8081）
└── README.md
```

## 启动项目

### 方式 1：并行启动（推荐）

**终端 1 - 启动前端：**
```bash
cd /Users/yuchizhou/Project/Vibe_Coding_Project/photo_shooting/frontend
npm run dev
```

**终端 2 - 启动后端：**
```bash
export PATH="$HOME/dev/apache-maven-3.9.6/bin:$PATH"
cd /Users/yuchizhou/Project/Vibe_Coding_Project/photo_shooting/backend
mvn spring-boot:run
```

### 方式 2：直接访问

- 前端：http://localhost:5173
- 后端 API：http://localhost:8081/api/auth/login

## 测试登录

**用户名**：admin  
**密码**：123456

### 测试命令
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

## 未来配置 - MySQL

如果要使用真实 MySQL 数据库：

1. 修改 `backend/pom.xml`，启用 MySQL 驱动
2. 修改 `backend/src/main/resources/application.properties`，配置数据库连接
3. 创建数据库和表：
```sql
CREATE DATABASE photo_shooting;
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  display_name VARCHAR(120)
);
INSERT INTO users (username, password, display_name) VALUES ('admin', '123456', '管理员');
```

## 故障排除

**问题**：Maven 命令找不到
**解决**：运行 `export PATH="$HOME/dev/apache-maven-3.9.6/bin:$PATH"`

**问题**：端口 8081 已占用
**解决**：修改 `application.properties` 中的 `server.port` 值

**问题**：前端无法连接后端
**解决**：检查 Vite 配置中的代理设置，确保指向 `http://localhost:8081`

