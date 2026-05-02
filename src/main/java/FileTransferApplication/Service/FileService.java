package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    @Autowired
    private FileMetadataRepo fileMetadataRepo;

    public String uploadService(MultipartFile file) {
        //Logic to Store File in Blob Storage
        return "File Uploaded Successfully";
    }
    public MultipartFile downloadService(String url) {
        MultipartFile file = null;
        if(fileMetadataRepo.findById(url).isPresent()) {
            FileMetadata fileMetadata = fileMetadataRepo.findById(url).get();
            if(fileMetadata.getExpiryDate() != null) {
                //Logic to fetch file from blob storage
                file = null;
            }
        }
        return file;
    }
}
