package com.example.ekanek;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Services {
    public String uploadFile(MultipartFile multipartFile, String folderName) throws IOException;
    public void downloadFile(String keyName, String downloadFilePath);
}
