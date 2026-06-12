package FileTransferApplication.Service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.SecureRandom;

@Service
public class SecurityService {

    private static final String AES_ALGO = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int AES_KEY_SIZE = 256;
    private static SecretKey secretKey;

    static {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(AES_KEY_SIZE, new SecureRandom());
            secretKey = keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }

    public byte[] decryptFile(byte[] encryptedFile) {
        return decryptFile(encryptedFile, secretKey);
    }

    public byte[] decryptFile(byte[] encryptedFile, String encryptionKeyJson) {
        try {
            SecretKey fileKey = importAesKey(encryptionKeyJson);
            return decryptFile(encryptedFile, fileKey);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private byte[] decryptFile(byte[] encryptedFile, SecretKey key) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedFile, 0, iv, 0, GCM_IV_LENGTH);
            
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(AES_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            
            return cipher.doFinal(encryptedFile, GCM_IV_LENGTH, encryptedFile.length - GCM_IV_LENGTH);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private SecretKey importAesKey(String encryptionKeyJson) {
        try {
            Pattern keyPattern = Pattern.compile("\\\"k\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
            Matcher matcher = keyPattern.matcher(encryptionKeyJson);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Missing key material");
            }

            String keyMaterial = matcher.group(1);
            byte[] keyBytes = Base64.getUrlDecoder().decode(keyMaterial);
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Invalid encryption key format", e);
        }
    }
}
