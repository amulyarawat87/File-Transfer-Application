package FileTransferApplication.Controller;

import FileTransferApplication.DTO.PresignedUrlResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.Service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class FileControllerTest {

    @Mock
    private FileService fileService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new FileController(fileService)).build();
    }

    @Test
    void downloadFileRoutesThroughDownloadEndpoint() throws Exception {
        ResponseEntity<org.springframework.core.io.Resource> response = ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample.txt\"")
                .body(new ByteArrayResource("hello".getBytes()));
        when(fileService.downloadService("abc123")).thenReturn(response);

        mockMvc.perform(get("/api/download/abc123"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample.txt\""))
                .andExpect(content().string("hello"));
    }

    @Test
    void alternateDownloadRouteMapsToSameServiceCall() throws Exception {
        ResponseEntity<org.springframework.core.io.Resource> response = ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample.txt\"")
                .body(new ByteArrayResource("hello".getBytes()));
        when(fileService.downloadService("xyz789")).thenReturn(response);

        mockMvc.perform(get("/api/s/xyz789"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello"));
    }

    @Test
    void getPresignedUploadUrlReturnsDtoAsJson() throws Exception {
        when(fileService.getPresignedUploadUrl())
                .thenReturn(new PresignedUrlResponse("file-1", "https://example.com/upload", 60L));

        mockMvc.perform(get("/api/upload/presigned-url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value("file-1"))
                .andExpect(jsonPath("$.presignedUrl").value("https://example.com/upload"))
                .andExpect(jsonPath("$.expiresInSeconds").value(60));
    }

    @Test
    void confirmUploadReturnsShortCodeJson() throws Exception {
        when(fileService.confirmUpload(any(UploadConfirmationRequest.class))).thenReturn("ABC123");

        mockMvc.perform(post("/api/upload/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileId\":\"file-1\",\"fileName\":\"report.txt\",\"fileSize\":12,\"encryptionKey\":\"key\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("ABC123"));
    }
}