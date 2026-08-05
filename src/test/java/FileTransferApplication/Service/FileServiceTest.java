package FileTransferApplication.Service;

import FileTransferApplication.DTO.PresignedUrlResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private DBService dbService;

    @Mock
    private S3Service s3Service;

    @Mock
    private SecurityService securityService;

    @Mock
    private FileMetadataRepo fileMetadataRepo;

    @Test
    void downloadServiceReturnsNotFoundWhenShortCodeIsMissing() throws IOException {
        FileService fileService = new FileService(dbService, s3Service, securityService, fileMetadataRepo);
        when(fileMetadataRepo.findByShortCode("missing")).thenReturn(Optional.empty());

        ResponseEntity<?> response = fileService.downloadService("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(s3Service, securityService, dbService);
    }

    @Test
    void downloadServiceReturnsNotFoundWhenFileIsExpired() throws IOException {
        FileService fileService = new FileService(dbService, s3Service, securityService, fileMetadataRepo);
        FileMetadata expired = fileMetadata("file-1", "report.txt", Instant.now().minusSeconds(60), null);
        when(fileMetadataRepo.findByShortCode("abc123")).thenReturn(Optional.of(expired));

        ResponseEntity<?> response = fileService.downloadService("abc123");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(s3Service, securityService, dbService);
    }

    @Test
    void downloadServiceReturnsDecryptedFileWithFallbackContentType() throws IOException {
        FileService fileService = new FileService(dbService, s3Service, securityService, fileMetadataRepo);
        byte[] encrypted = "encrypted".getBytes(StandardCharsets.UTF_8);
        byte[] decrypted = "decrypted".getBytes(StandardCharsets.UTF_8);
        FileMetadata file = fileMetadata("file-1", "download", Instant.now().plusSeconds(60), null);

        when(fileMetadataRepo.findByShortCode("abc123")).thenReturn(Optional.of(file));
        when(s3Service.downloadFile("file-1")).thenReturn(encrypted);
        when(securityService.decryptFile(encrypted)).thenReturn(decrypted);

        ResponseEntity<?> response = fileService.downloadService("abc123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertEquals("attachment; filename=\"download\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertArrayEquals(decrypted, ((ByteArrayResource) response.getBody()).getByteArray());
        verify(securityService).decryptFile(encrypted);
        verify(securityService, never()).decryptFile(any(byte[].class), any());
    }

    @Test
    void downloadServiceUsesEncryptionKeyWhenPresent() throws IOException {
        FileService fileService = new FileService(dbService, s3Service, securityService, fileMetadataRepo);
        byte[] encrypted = "encrypted".getBytes(StandardCharsets.UTF_8);
        byte[] decrypted = "decrypted".getBytes(StandardCharsets.UTF_8);
        String encryptionKey = "{\"kty\":\"oct\",\"k\":\"a2V5\"}";
        FileMetadata file = fileMetadata("file-1", "photo.txt", Instant.now().plusSeconds(60), encryptionKey);

        when(fileMetadataRepo.findByShortCode("abc123")).thenReturn(Optional.of(file));
        when(s3Service.downloadFile("file-1")).thenReturn(encrypted);
        when(securityService.decryptFile(encrypted, encryptionKey)).thenReturn(decrypted);

        ResponseEntity<?> response = fileService.downloadService("abc123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
        assertEquals("attachment; filename=\"photo.txt\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertArrayEquals(decrypted, ((ByteArrayResource) response.getBody()).getByteArray());
        verify(securityService).decryptFile(encrypted, encryptionKey);
    }

    @Test
    void getPresignedUploadUrlReturnsExpectedResponse() {
        FileService fileService = new FileService(dbService, s3Service, securityService, fileMetadataRepo);
        when(s3Service.generatePresignedPutUrl(any(), eq(Duration.ofSeconds(60))))
                .thenReturn("https://example.com/upload");

        PresignedUrlResponse response = fileService.getPresignedUploadUrl();

        assertNotNull(response.fileId());
        assertEquals("https://example.com/upload", response.presignedUrl());
        assertEquals(60L, response.expiresInSeconds());
    }

    @Test
    void confirmUploadPersistsMetadataAndReturnsGeneratedShortCode() {
        FileService fileService = new FileService(dbService, s3Service, securityService, fileMetadataRepo);
        UploadConfirmationRequest request = new UploadConfirmationRequest("file-1", "archive.txt", 99L, "key-json");

        when(fileMetadataRepo.existsByShortCode(any())).thenReturn(true, false);
        doNothing().when(dbService).save(any(), any(), any(), anyLong(), any(), any(), any());

        String shortCode = fileService.confirmUpload(request);

        assertEquals(6, shortCode.length());
        verify(fileMetadataRepo).existsByShortCode(shortCode);
        verify(dbService).save(eq("file-1"), eq("archive.txt"), eq(".txt"), eq(99L), any(Instant.class), eq("key-json"), eq(shortCode));
    }

    @Test
    void confirmUploadFailsWhenFileNameHasNoExtension() {
        FileService fileService = new FileService(dbService, s3Service, securityService, fileMetadataRepo);
        UploadConfirmationRequest request = new UploadConfirmationRequest("file-1", "archive", 99L, "key-json");

        assertThrows(StringIndexOutOfBoundsException.class, () -> fileService.confirmUpload(request));
    }

    private static FileMetadata fileMetadata(String fileId, String fileName, Instant expiryDateTime, String encryptionKey) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileId(fileId);
        fileMetadata.setShortCode("short");
        fileMetadata.setFileName(fileName);
        fileMetadata.setFileType(".txt");
        fileMetadata.setFileSize(10L);
        fileMetadata.setExpiryDateTime(expiryDateTime);
        fileMetadata.setEncryptionKey(encryptionKey);
        return fileMetadata;
    }
}