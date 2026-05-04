package FileTransferApplication.Controller;

import FileTransferApplication.Service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin()
@RequestMapping("api")
public class FileController {

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/upload", method =  RequestMethod.POST)
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("Received file: " + file);
        return fileService.uploadService(file);
    }

    @RequestMapping(value = "/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) throws IOException {
        System.out.println("Downloading file Started..." + id);
        return fileService.downloadService(id);
    }

}