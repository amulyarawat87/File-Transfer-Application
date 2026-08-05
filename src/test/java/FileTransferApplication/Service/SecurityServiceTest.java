package FileTransferApplication.Service;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityServiceTest {

    private static final String AES_ALGO = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecurityService securityService = new SecurityService();

    @Test
    void decryptFileWithDefaultKeyRoundTripsCiphertext() throws Exception {
        byte[] plaintext = "hello world".getBytes(StandardCharsets.UTF_8);
        SecretKey key = defaultSecretKey();

        byte[] encrypted = encrypt(plaintext, key);

        assertArrayEquals(plaintext, securityService.decryptFile(encrypted));
    }

    @Test
    void decryptFileWithJsonKeyRoundTripsCiphertext() throws Exception {
        byte[] plaintext = "payload".getBytes(StandardCharsets.UTF_8);
        SecretKey key = generatedKey();

        byte[] encrypted = encrypt(plaintext, key);

        assertArrayEquals(plaintext, securityService.decryptFile(encrypted, toJsonWebKey(key)));
    }

    @Test
    void decryptFileWithMissingKeyMaterialFails() {
        byte[] encrypted = new byte[GCM_IV_LENGTH + 16];

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> securityService.decryptFile(encrypted, "{\"kty\":\"oct\"}"));

        assertTrue(exception.getMessage().contains("Invalid encryption key format")
                || exception.getMessage().contains("Decryption failed"));
    }

    @Test
    void decryptFileWithShortCiphertextFails() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> securityService.decryptFile(new byte[GCM_IV_LENGTH - 1]));

        assertTrue(exception.getMessage().contains("Decryption failed"));
    }

    @Test
    void decryptFileWithWrongKeyFails() throws Exception {
        byte[] plaintext = "payload".getBytes(StandardCharsets.UTF_8);
        SecretKey encryptionKey = generatedKey();
        SecretKey wrongKey = generatedKey();

        byte[] encrypted = encrypt(plaintext, encryptionKey);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> securityService.decryptFile(encrypted, toJsonWebKey(wrongKey)));

        assertTrue(exception.getMessage().contains("Decryption failed"));
    }

    private static SecretKey generatedKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, new SecureRandom());
        return keyGenerator.generateKey();
    }

    private static SecretKey defaultSecretKey() throws Exception {
        Field field = SecurityService.class.getDeclaredField("secretKey");
        field.setAccessible(true);
        return (SecretKey) field.get(null);
    }

    private static byte[] encrypt(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] encrypted = cipher.doFinal(plaintext);

        return ByteBuffer.allocate(iv.length + encrypted.length)
                .put(iv)
                .put(encrypted)
                .array();
    }

    private static String toJsonWebKey(SecretKey key) {
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(key.getEncoded());
        return "{\"kty\":\"oct\",\"k\":\"" + encoded + "\"}";
    }
}