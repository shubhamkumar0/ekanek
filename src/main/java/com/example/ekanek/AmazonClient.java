package com.example.ekanek;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static com.amazonaws.services.s3.internal.Constants.MB;

@Service
public class AmazonClient {

    private AmazonS3 s3client;

    private Logger logger = LoggerFactory.getLogger(AmazonClient.class);

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    @Value("${amazonProperties.accessKey}")
    private String accessKey;

    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = new AmazonS3Client(credentials);
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    @Async
    public String uploadFile(MultipartFile multipartFile,String folderName) {

        String completeFileName = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            completeFileName = folderName + "/" + fileName;
//            fileUrl = endpointUrl + "/" + bucketName + "/" + folderName + "/" + fileName;
            uploadFileTos3bucket(completeFileName, file);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("uploadfileexception",e);
        }
        return completeFileName;
    }

    public String deleteFileFromS3Bucket(String fileUrl) {
        try {
            s3client.deleteObject(new DeleteObjectRequest(bucketName , fileUrl));
        } catch(AmazonServiceException ase) {
            logger.info("Caught an AmazonServiceException from PUT requests, rejected reasons:");
            logger.info("Error Message:    " + ase.getMessage());
            logger.info("HTTP Status Code: " + ase.getStatusCode());
            logger.info("AWS Error Code:   " + ase.getErrorCode());
            logger.info("Error Type:       " + ase.getErrorType());
            logger.info("Request ID:       " + ase.getRequestId());
            return null;
        } catch (SdkClientException sce) {
            logger.error("Caught an SdkClientException: ");
            logger.error("Error Message: " + sce.getMessage());
            return null;
        }
        return "Successfully deleted";
    }

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

    @Async
    public String downloadFile(final String keyName) {
        byte[] content = null;
        final S3Object s3Object = s3client.getObject(bucketName, keyName);
        return s3client.getUrl(bucketName,keyName).toString();
    }

    public List<String> listAllFiles(String email){
        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest()
                        .withBucketName(bucketName)
                        .withPrefix(email+"/");

        List<String> keys = new ArrayList<>();
        ObjectListing objects = s3client.listObjects(listObjectsRequest);
        while (true) {
            List<S3ObjectSummary> summaries = objects.getObjectSummaries();
            if (summaries.size() < 1) {
                break;
            }

            for (S3ObjectSummary item : summaries) {
                if (!item.getKey().endsWith("/"))
                    keys.add(item.getKey());
            }

            objects = s3client.listNextBatchOfObjects(objects);
        }
        return keys;
    }

    @Bean
    public TransferManager transferManager() {

        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(s3client)
                .withDisableParallelDownloads(false)
                .withMinimumUploadPartSize(Long.valueOf(5 * MB))
                .withMultipartUploadThreshold(Long.valueOf(16 * MB))
                .withMultipartCopyPartSize(Long.valueOf(5 * MB))
                .withMultipartCopyThreshold(Long.valueOf(100 * MB))
                .withExecutorFactory(() -> createExecutorService(20))
                .build();
        int oneDay = 1000 * 60 * 60 * 24;
        Date oneDayAgo = new Date(System.currentTimeMillis() - oneDay);

        try {

            tm.abortMultipartUploads(bucketName, oneDayAgo);

        } catch (AmazonClientException e) {
            logger.error("Unable to upload file, upload was aborted, reason: " + e.getMessage());
        }

        return tm;
    }
    private ThreadPoolExecutor createExecutorService(int threadNumber) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int threadCount = 1;

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("jsa-amazon-s3-transfer-manager-worker-" + threadCount++);
                return thread;
            }
        };
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNumber, threadFactory);
    }
}