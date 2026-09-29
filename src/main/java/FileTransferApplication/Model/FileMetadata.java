package FileTransferApplication.Model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "file_metadata")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
    @Id
    private String fileId;

    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private Instant expiryTime;

    // CODE REVIEW [Security]: Raw encryption key persisted in DB — high-value secret; encrypt column or use external KMS.
    @Column(columnDefinition = "TEXT")
    private String encryptionKey;

    @Column(unique = true, nullable = false, length = 6)
    private String shortCode;

    @Override
    public int hashCode() {
        return fileId != null ? fileId.hashCode() : 0;
    }

    @Override
    public boolean equals(Object currentObject) {
        if (this == currentObject) return true;
        if (currentObject == null || getClass() != currentObject.getClass()) return false;
        FileMetadata currentFileMetaData = (FileMetadata) currentObject;
        return fileId != null && fileId.equals(currentFileMetaData.fileId);
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "id='" + fileId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", shortCode='" + shortCode + '\'' +
                ", createdAt=" + expiryTime +
                '}';
    }
}

//[Not Clear Implementation and Understanding: Review Again]