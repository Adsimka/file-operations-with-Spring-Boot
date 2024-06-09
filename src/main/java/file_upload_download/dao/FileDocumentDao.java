package file_upload_download.dao;

import file_upload_download.dto.FileDocumentDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDocumentDao extends JpaRepository<FileDocumentDto, Long> {

    FileDocumentDto findByFileName(String fileName);
}
