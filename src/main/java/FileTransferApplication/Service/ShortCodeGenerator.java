package FileTransferApplication.Service;

import java.security.SecureRandom;

public class ShortCodeGenerator {

    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    // CODE REVIEW [Security]: 6-char codes (~56B space) are guessable at scale — consider 8+ chars or add download rate limiting.
    private static final int LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    // CODE REVIEW [Testing]: No unit tests for output length, charset membership, or statistical distribution.
    public static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}