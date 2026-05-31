package FileTransferApplication.Service;

import org.springframework.web.multipart.MultipartFile;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.io.IOException;

public class SecurityService {

    private static final String AES_ALGO = "AES/GCM/NoPadding";
    private static final String RSA_ALGO = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
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

    public byte[] encryptFile(MultipartFile file) throws IOException {
        try {
            byte[] fileBytes = file.getBytes();
            Cipher cipher = Cipher.getInstance(AES_ALGO);
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            
            byte[] encryptedData = cipher.doFinal(fileBytes);
            byte[] result = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public byte[] decryptFile(byte[] encryptedFile) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedFile, 0, iv, 0, GCM_IV_LENGTH);
            
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(AES_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            
            return cipher.doFinal(encryptedFile, GCM_IV_LENGTH, encryptedFile.length - GCM_IV_LENGTH);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
