package file_upload_download.controller;

import file_upload_download.dto.FileUploadResponse;
import file_upload_download.service.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    @PostMapping("multiple/upload")
    List<FileUploadResponse> multipleUpload(@RequestParam("files") MultipartFile[] files) {
        if (files.length > 7) {
            throw new RuntimeException("Too many files");
        }

        List<FileUploadResponse> filesUpload = new ArrayList<>();
        Arrays.asList(files).stream()
                .forEach(file -> {
                    String fileName = fileStorageService.storeFile(file);
                    // http:/localhost:8081/download/{fileName}
                    String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/download/")
                            .path(fileName)
                            .toUriString();
                    String contentType = file.getContentType();

                    FileUploadResponse uploadResponse = FileUploadResponse.builder()
                            .fileName(fileName)
                            .url(url)
                            .contentType(contentType)
                            .build();

                    filesUpload.add(uploadResponse);
                });
        return filesUpload;
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

    @SneakyThrows
    @GetMapping("zipDownload")
    public void zipDownload(@RequestParam("fileName") String[] filesName, HttpServletResponse response) {
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            Arrays.asList(filesName).stream()
                    .forEach(file -> {
                        Resource resource = fileStorageService.download(file);
                        ZipEntry entry = new ZipEntry(resource.getFilename());
                        try {
                            entry.setSize(resource.contentLength());

                            zos.putNextEntry(entry);
                            StreamUtils.copy(resource.getInputStream(), zos);
                            zos.closeEntry();
                        } catch (IOException exception) {
                            throw new RuntimeException("Some exception while zip");
                        }
                    });
            zos.finish();
        }
        response.setStatus(200);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;fileName=zipFile");
    }
}
