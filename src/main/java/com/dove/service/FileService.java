package com.dove.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class FileService {
    @Value("${file.upload-path}")
    private String uploadDirectory;

    public boolean saveFile(MultipartFile multipartFile) {
        //파일이 없으면 별도의 처리를 한다.
        if (multipartFile == null || multipartFile.isEmpty()) {
            return false;
        }

        String fileName = multipartFile.getOriginalFilename();
        File targetFile = new File(uploadDirectory, fileName);
        try{
            FileCopyUtils.copy(multipartFile.getInputStream(), new FileOutputStream(targetFile));

        }catch (IOException ioException){
            //파일 저장 실패시 에러 핸들링.
            log.error(ioException.toString(),ioException);
            return false;
        }

        return true;

    }
}
