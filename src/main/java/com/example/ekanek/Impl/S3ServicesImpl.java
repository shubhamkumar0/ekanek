package com.example.ekanek.Impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.example.ekanek.Interfaces.S3Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

@Service
public class S3ServicesImpl implements S3Services {

    private Logger logger = LoggerFactory.getLogger(S3ServicesImpl.class);

    @Autowired
    protected TransferManager transferManager;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }

    @Override
    public String uploadFile(MultipartFile multipartFile, String folderName) throws IOException {
        String completeFileName = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            completeFileName = folderName + "/" + fileName;
            final PutObjectRequest request = new PutObjectRequest(bucketName, completeFileName, file).withCannedAcl(CannedAccessControlList.PublicRead);

            request.setGeneralProgressListener(new ProgressListener() {
                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    String transferredBytes = "Uploaded bytes: " + progressEvent.getBytesTransferred();
                    logger.info(transferredBytes);
                }
            });

            Upload upload = transferManager.upload(request);
            upload.waitForCompletion();

        } catch (AmazonServiceException e) {
            logger.info(e.getMessage());
        } catch (AmazonClientException e) {
            logger.info(e.getMessage());
        } catch (InterruptedException e) {
            logger.info(e.getMessage());
        } catch (Exception e){
            logger.info(e.getMessage());
        }
        return completeFileName;
    }

    @Override
    public void downloadFile(String keyName, String downloadFilePath) {
        final GetObjectRequest request = new GetObjectRequest(bucketName, keyName);

        request.setGeneralProgressListener(new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                String transferredBytes = "Downloaded bytes: " + progressEvent.getBytesTransferred();
                logger.info(transferredBytes);
            }
        });

        Download download = transferManager.download(request, new File(downloadFilePath));

        try {

            download.waitForCompletion();

        } catch (AmazonServiceException e) {
            logger.info(e.getMessage());
        } catch (AmazonClientException e) {
            logger.info(e.getMessage());
        } catch (InterruptedException e) {
            logger.info(e.getMessage());
        }
    }


}
