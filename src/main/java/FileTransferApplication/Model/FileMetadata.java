package FileTransferApplication.Model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
    @Id
    private String url;
    private String name;
    private String size;
    private Date expiryDate;
}
