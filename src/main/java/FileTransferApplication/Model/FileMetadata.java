package FileTransferApplication.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "file_metadata")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
    @Id
    private String id;

    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private long fileSize;
    
    @Column(nullable = false)
    private Instant createdAt;

    // CODE REVIEW [Security]: Raw encryption key persisted in DB — high-value secret; encrypt column or use external KMS.
    @Column(columnDefinition = "TEXT")
    private String encryptionKey;

    @Column(unique = true, nullable = false, length = 6)
    private String shortCode;

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object currentObject) {
        if (this == currentObject) return true;
        if (currentObject == null || getClass() != currentObject.getClass()) return false;
        FileMetadata currentFileMetaData = (FileMetadata) currentObject;
        return id != null && id.equals(currentFileMetaData.id);
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "id='" + id + '\'' +
                ", fileId='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", shortCode='" + shortCode + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

//[Not Clear Implementation and Understanding: Review Again]