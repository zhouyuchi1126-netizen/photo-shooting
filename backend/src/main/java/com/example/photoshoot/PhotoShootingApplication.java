package com.example.photoshoot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static com.example.photoshoot.util.PasswordEncryptor.encrypt;
import static com.example.photoshoot.util.PasswordEncryptor.isEncrypted;

@SpringBootApplication
@MapperScan("com.example.photoshoot.mapper")
public class PhotoShootingApplication {

    private static final List<String> IMAGE_EXTS = List.of("jpg", "jpeg", "png", "gif", "webp");

    public static void main(String[] args) {
        SpringApplication.run(PhotoShootingApplication.class, args);
    }

    @Bean
    public CommandLineRunner initializeDatabase(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                ensureUserTable(connection);
                ensureAdminUser(connection);
                migratePasswords(connection);
                ensureAlbumAndImageTables(connection);
                migrateData(connection);
            }
        };
    }

    /* ========== 用户表 ========== */

    private void ensureUserTable(Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet tables = meta.getTables(null, null, "ps_user", null)) {
            if (!tables.next()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate("CREATE TABLE ps_user (" +
                            "id VARCHAR(50) PRIMARY KEY, " +
                            "username VARCHAR(100) NOT NULL UNIQUE, " +
                            "password VARCHAR(100) NOT NULL, " +
                            "display_name VARCHAR(100), " +
                            "role VARCHAR(50) NOT NULL DEFAULT 'user', " +
                            "wechat_openid VARCHAR(100), " +
                            "phone VARCHAR(20))");
                }
            } else {
                // 迁移旧表：补充可能缺失的列
                try (ResultSet columns = meta.getColumns(null, null, "ps_user", "role")) {
                    if (!columns.next()) {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE ps_user ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'user'");
                        }
                    }
                }
                try (ResultSet columns = meta.getColumns(null, null, "ps_user", "wechat_openid")) {
                    if (!columns.next()) {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE ps_user ADD COLUMN wechat_openid VARCHAR(100)");
                        }
                    }
                }
                try (ResultSet columns = meta.getColumns(null, null, "ps_user", "phone")) {
                    if (!columns.next()) {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE ps_user ADD COLUMN phone VARCHAR(20)");
                        }
                    }
                }
            }
        }
    }

    private void ensureAdminUser(Connection connection) throws SQLException {
        String encryptedPw = encrypt("Admin@123");
        try (Statement stmt = connection.createStatement()) {
            int updated = stmt.executeUpdate(
                "UPDATE ps_user SET password = '" + esc(encryptedPw) + "', role = 'admin', display_name = '管理员' WHERE username = 'admin'"
            );
            if (updated == 0) {
                String id = UUID.randomUUID().toString();
                stmt.executeUpdate("INSERT INTO ps_user (id, username, password, display_name, role) VALUES ('" +
                        id + "','admin','" + esc(encryptedPw) + "','管理员','admin')");
            }
        }
    }

    private void migratePasswords(Connection connection) throws SQLException {
        // 先查询需要迁移的用户
        java.util.List<String[]> toMigrate = new java.util.ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, password FROM ps_user")) {
            while (rs.next()) {
                String id = rs.getString("id");
                String pw = rs.getString("password");
                if (!isEncrypted(pw)) {
                    toMigrate.add(new String[]{id, pw});
                }
            }
        }
        // 再逐个更新（使用新的 Statement）
        for (String[] row : toMigrate) {
            String encrypted = encrypt(row[1]);
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("UPDATE ps_user SET password = '" + esc(encrypted) + "' WHERE id = '" + esc(row[0]) + "'");
            }
        }
    }

    /* ========== 相册 & 图片表 ========== */

    private void ensureAlbumAndImageTables(Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();

        try (ResultSet tables = meta.getTables(null, null, "album", null)) {
            if (!tables.next()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate("CREATE TABLE album (" +
                            "id VARCHAR(100) PRIMARY KEY, " +
                            "title VARCHAR(200) NOT NULL, " +
                            "description TEXT, " +
                            "cover_image VARCHAR(200), " +
                            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
                }
            }
        }

        try (ResultSet tables = meta.getTables(null, null, "image", null)) {
            if (!tables.next()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate("CREATE TABLE image (" +
                            "id VARCHAR(50) PRIMARY KEY, " +
                            "album_id VARCHAR(100) NOT NULL, " +
                            "filename VARCHAR(200) NOT NULL, " +
                            "filepath VARCHAR(500) NOT NULL, " +
                            "uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                            "sort_order INT DEFAULT 0)");
                }
            } else {
                try (ResultSet columns = meta.getColumns(null, null, "image", "sort_order")) {
                    if (!columns.next()) {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE image ADD COLUMN sort_order INT DEFAULT 0");
                            // 为已有图片设置初始排序值
                            stmt.executeUpdate("UPDATE image SET sort_order = 0");
                        }
                    }
                }
            }
        }
    }

    /* ========== 从文件系统迁移已有数据 ========== */

    private void migrateData(Connection connection) throws SQLException, IOException {
        // 仅首次部署时从文件系统迁移，已有数据则跳过
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM album")) {
            rs.next();
            if (rs.getInt(1) > 0) return;
        }

        Path imagesDir = Paths.get("images");
        if (!Files.exists(imagesDir)) return;

        List<Path> dirs;
        try { dirs = Files.list(imagesDir).filter(Files::isDirectory).sorted().toList(); }
        catch (IOException e) { return; }

        for (Path dir : dirs) {
            String groupId = dir.getFileName().toString();
            String title = formatTitle(groupId);
            String description = "由管理员维护的作品集";
            String coverImage = null;

            // 读取 metadata.properties
            Path metaFile = dir.resolve("metadata.properties");
            if (Files.exists(metaFile)) {
                Properties props = new Properties();
                try (Reader reader = Files.newBufferedReader(metaFile, StandardCharsets.UTF_8)) {
                    props.load(reader);
                } catch (IOException ignored) {}
                if (props.containsKey("title")) title = props.getProperty("title");
                if (props.containsKey("description")) description = props.getProperty("description");
                if (props.containsKey("coverImage")) coverImage = props.getProperty("coverImage");
            }

            // 插入 album（使用 groupId 作为主键）
            try (Statement stmt = connection.createStatement()) {
                String sql = "INSERT INTO album (id, title, description, cover_image, created_at) VALUES ('" +
                        esc(groupId) + "','" + esc(title) + "','" + esc(description) + "'," +
                        (coverImage != null ? "'" + esc(coverImage) + "'" : "NULL") + ", NOW())";
                try { stmt.executeUpdate(sql); } catch (SQLException ignored) {} // 已存在则跳过
            }

            // 扫描图片文件
            List<Path> imageFiles;
            try {
                imageFiles = Files.list(dir)
                        .filter(Files::isRegularFile)
                        .filter(p -> {
                            String n = p.getFileName().toString().toLowerCase();
                            return IMAGE_EXTS.stream().anyMatch(n::endsWith);
                        })
                        .sorted().toList();
            } catch (IOException e) { continue; }

            for (Path imgFile : imageFiles) {
                String fname = imgFile.getFileName().toString();
                String filepath = "/images/" + groupId + "/" + fname;
                try (Statement stmt = connection.createStatement()) {
                    String imgId = UUID.randomUUID().toString();
                    stmt.executeUpdate("INSERT INTO image (id, album_id, filename, filepath, uploaded_at, sort_order) VALUES ('" +
                            imgId + "','" + esc(groupId) + "','" + esc(fname) + "','" + esc(filepath) + "', NOW(), 0)");
                }
            }
        }
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    private String formatTitle(String groupId) {
        String[] parts = groupId.split("[-_\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isBlank()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
