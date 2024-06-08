package file_upload_download.controller;

import file_upload_download.dto.FileUploadResponse;
import file_upload_download.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class UploadDownloadWithFileSystemController {

    private final FileStorageService fileStorageService;

    @PostMapping("single/upload")
    public FileUploadResponse singleFileUpload(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        // http:/localhost:8081/download/{fileName}
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(fileName)
                .toUriString();

        String contentType = file.getContentType();

        return FileUploadResponse.builder()
                .fileName(fileName)
                .url(url)
                .contentType(contentType)
                .build();
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadSingleFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.download(fileName);
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;fileName=" + resource.getFilename())
                .body(resource);
    }
}
