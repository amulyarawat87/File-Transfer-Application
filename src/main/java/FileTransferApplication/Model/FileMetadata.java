package FileTransferApplication.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
// CODE REVIEW [Code Quality]: @Data on JPA entities generates equals/hashCode on all fields including mutable state —
// can break Hibernate session management. Prefer @Getter/@Setter and explicit equals/hashCode on @Id only.
// CODE REVIEW [Maintainability]: Missing @Table(name=...) — Hibernate uses default table name; explicit naming avoids surprises.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
    @Id
    private String fileId;

    @Column(unique = true, nullable = false, length = 6)
    private String shortCode;

    private String fileName;
    private String fileType;
    private long fileSize;
    
    @Column(nullable = false)
    private Instant expiryDateTime;

    // CODE REVIEW [Security]: Raw encryption key persisted in DB — high-value secret; encrypt column or use external KMS.
    @Column(columnDefinition = "TEXT")
    private String encryptionKey;
}