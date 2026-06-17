package FileTransferApplication.Service;

import java.security.SecureRandom;

public class ShortCodeGenerator {

    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}