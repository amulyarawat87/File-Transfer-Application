package FileTransferApplication.Utils;

import java.util.UUID;

public class FileIDGenerator {

    private FileIDGenerator(){ throw new AssertionError("Cannot Instantiate Utility Class");}

    public static String generateFileId(){
        return UUID.randomUUID().toString();
    }
}
