# Photo Shooting 项目

本项目将当前静态站点重构为 Vue + Spring Boot + MySQL + MyBatis 的全栈登录系统。

## 目录结构

- `frontend/` - Vue 3 前端应用，提供登录页和作品展示页。
- `backend/` - Spring Boot 后端应用，提供登录接口和 MyBatis 数据库访问。

## 前端运行

1. 进入前端目录：`cd frontend`
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`

前端默认运行在 `http://localhost:5173`。

## 后端运行

1. 进入后端目录：`cd backend`
2. 启动 Spring Boot：`mvn spring-boot:run`

后端默认运行在 `http://localhost:8080`。

## 数据库配置

请在 `backend/src/main/resources/application.properties` 中替换以下内容为你的 MySQL 配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_password
```

示例数据库表结构：

```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  display_name VARCHAR(120)
);

INSERT INTO users (username, password, display_name) VALUES ('admin', '123456', '管理员');
```

## 登录流程

- 前端提交 `POST /api/auth/login`。
- 后端通过 MyBatis 查询 `users` 表并验证密码。
- 登录成功后返回 `displayName` 和示例 token。

## 备注

- 当前实现为演示用途，密码未做哈希处理；生产环境应使用加密存储和安全认证机制。