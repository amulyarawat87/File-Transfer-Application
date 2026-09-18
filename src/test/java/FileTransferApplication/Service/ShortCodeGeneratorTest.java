package FileTransferApplication.Service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortCodeGeneratorTest {

    @Test
    void generateProducesSixCharacterBase62Code() {
        String code = ShortCodeGenerator.generate();

        assertEquals(6, code.length());
        assertTrue(code.chars().allMatch(character ->
                (character >= 'A' && character <= 'Z')
                        || (character >= 'a' && character <= 'z')
                        || (character >= '0' && character <= '9')));
    }
}