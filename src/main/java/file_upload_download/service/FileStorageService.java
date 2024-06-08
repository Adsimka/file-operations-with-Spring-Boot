package file_upload_download.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path fileStoragePath;
    private final String fileStorageLocation;

    private Resource resource;

    public FileStorageService(@Value("${file.storage.location:temp}") String fileStorageLocation) {
        this.fileStorageLocation = fileStorageLocation;
        fileStoragePath = Paths.get(fileStorageLocation)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(fileStoragePath);
        } catch (IOException exception) {
            throw new RuntimeException("Issue in creating file directory", exception);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        Path filePath = Paths.get(fileStoragePath + "/" + fileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RuntimeException("Issue in creating file directory", exception);
        }
        return fileName;
    }

    public Resource download(String fileName) {
        Path path = Paths.get(fileStorageLocation)
                .toAbsolutePath()
                .resolve(fileName);

        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Issue in creating file directory", exception);
        }

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("The file doesn't exist or not readable");
        }
    }
}
