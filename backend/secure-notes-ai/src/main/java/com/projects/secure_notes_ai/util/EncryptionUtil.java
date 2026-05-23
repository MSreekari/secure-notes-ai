package com.projects.secure_notes_ai.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptionUtil {

    @Value("${app.security.aes-key}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Convert your application key string into a raw byte array
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        // Ensure the key is exactly 32 bytes (256 bits) for AES-256
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("AES key must be exactly 32 bytes long!");
        }

        // Wrap those bytes into a Java SecretKeySpec structure
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTE = 12;
    private static final int TAG_LENGTH_BIT = 128; // GCM Authentication Tag length

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            // 1. Generate a strong, cryptographically secure random 12-byte IV
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            // 2. Initialize the standard Java Cipher tool to ENCRYPT mode
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, this.key, parameterSpec);

            // 3. Encrypt the plain text bytes
            byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 4. Prefix the IV onto the front of the ciphertext bytes so we can find it during decryption
            byte[] combinedPayload = ByteBuffer.allocate(iv.length + cipherTextBytes.length)
                    .put(iv)
                    .put(cipherTextBytes)
                    .array();

            // 5. Convert the combined binary array into a clean Base64 String for PostgreSQL storage
            return Base64.getEncoder().encodeToString(combinedPayload);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    public String decrypt(String secureBase64Text) {
        if (secureBase64Text == null || secureBase64Text.isEmpty()) {
            return secureBase64Text;
        }
        try {
            // 1. Decode the Base64 database string back into a combined raw byte array
            byte[] combinedPayload = Base64.getDecoder().decode(secureBase64Text);

            // 2. Slice out the first 12 bytes to rebuild our original Initialization Vector
            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(combinedPayload, 0, iv, 0, iv.length);

            // 3. Slice out the remaining bytes as the actual encrypted ciphertext content
            int cipherTextLength = combinedPayload.length - iv.length;
            byte[] cipherTextBytes = new byte[cipherTextLength];
            System.arraycopy(combinedPayload, iv.length, cipherTextBytes, 0, cipherTextBytes.length);

            // 4. Initialize the Java Cipher tool to DECRYPT mode using our key and extracted IV
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, this.key, parameterSpec);

            // 5. Unscramble the ciphertext bytes back into readable plain text characters
            byte[] decryptedBytes = cipher.doFinal(cipherTextBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: Data might be corrupted or key is incorrect", e);
        }
    }

}
