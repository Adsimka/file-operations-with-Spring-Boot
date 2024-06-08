package file_upload_download.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadResponse {

    String fileName;

    String contentType;

    String url;
}
