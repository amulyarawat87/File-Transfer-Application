package FileTransferApplication.Utils;

import java.security.SecureRandom;

public class ShortCodeGenerator {

    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private ShortCodeGenerator() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static String generateShortCode(){
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}