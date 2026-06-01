package com.example.photoshoot.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES/CBC/PKCS5Padding 对称加密工具
 * 密文格式: Base64( 16字节IV + 加密数据 )
 */
public class PasswordEncryptor {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH = 16;
    private static final String PREFIX = "{AES}";

    // 固定密钥（从密钥字符串派生）
    private static final SecretKeySpec KEY;

    static {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest("PhotoShootingSecretKey2025!@#".getBytes("UTF-8"));
            byte[] keyBytes = new byte[16];
            System.arraycopy(hash, 0, keyBytes, 0, 16);
            KEY = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("无法初始化加密密钥", e);
        }
    }

    /** 加密明文密码，返回 {AES}Base64(IV+密文) */
    public static String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, KEY, ivSpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));

            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /** 验证明文是否匹配密文（自动识别 {AES} 前缀和旧版明文） */
    public static boolean matches(String plaintext, String stored) {
        if (stored == null || plaintext == null) return false;
        if (stored.startsWith(PREFIX)) {
            // AES 加密存储
            return plaintext.equals(decrypt(stored));
        } else {
            // 旧版明文存储（兼容迁移前）
            return plaintext.equals(stored);
        }
    }

    /** 判断是否已使用 AES 加密 */
    public static boolean isEncrypted(String stored) {
        return stored != null && stored.startsWith(PREFIX);
    }

    private static String decrypt(String encryptedText) {
        try {
            String base64Data = encryptedText.substring(PREFIX.length());
            byte[] combined = Base64.getDecoder().decode(base64Data);

            byte[] iv = new byte[IV_LENGTH];
            byte[] ciphertext = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, KEY, new IvParameterSpec(iv));
            return new String(cipher.doFinal(ciphertext), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("密码解密失败", e);
        }
    }
}
