package file_upload_download.controller;

import file_upload_download.dao.FileDocumentDao;
import file_upload_download.dto.FileDocumentDto;
import file_upload_download.dto.FileUploadResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class UploadDownloadWithFileDatabaseController {

    private final FileDocumentDao documentDao;

    private static final String HEADER_VALUE_INLINE = "inline;fileName=";

    @PostMapping("single/upload/DB")
    @SneakyThrows
    public FileUploadResponse singleFileUpload(@RequestParam("file") MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        FileDocumentDto documentDto = FileDocumentDto.builder()
                .fileName(fileName)
                .docFile(file.getBytes())
                .build();

        documentDao.save(documentDto);
        // http:/localhost:8081/download/{fileName}
        String url= ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/DB/")
                .path(fileName)
                .toUriString();

        String contentType = file.getContentType();

        return FileUploadResponse.builder()
                .fileName(fileName)
                .url(url)
                .contentType(contentType)
                .build();
    }

    @GetMapping("/download/DB/{fileName}")
    public ResponseEntity<byte[]> downloadSingleFile(@PathVariable String fileName,
                                                     HttpServletRequest request) {
        FileDocumentDto documentDto = documentDao.findByFileName(fileName);

        String contentType = request.getServletContext().getMimeType(documentDto.getFileName());
        String headerValues = String.format(HEADER_VALUE_INLINE, documentDto.getFileName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValues)
                .body(documentDto.getDocFile());
    }
}
