package FileTransferApplication.Controller;

import FileTransferApplication.Service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("api")
public class FileController {

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/upload", method =  RequestMethod.POST)
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        System.out.println("Received file: " + file);
        return fileService.uploadService(file);
    }

    @RequestMapping("/download/{url}")
    public MultipartFile downloadFile(@PathVariable String url) {
        System.out.println("Downloading file Started..." + url);
        return fileService.downloadService(url);
    }

}