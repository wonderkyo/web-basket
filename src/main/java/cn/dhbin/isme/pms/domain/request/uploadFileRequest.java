package cn.dhbin.isme.pms.domain.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Data
public class uploadFileRequest {
    private MultipartFile file;
}
