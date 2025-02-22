package com.ws.common.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.ws.common.config.OSSConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;

/**
 * 用于将超分辨率后的图片上传
 */
public class FileUpload {
    
    @Autowired
    private OSSConfig ossConfig;


    /** 生成一个唯一的文件名 */
    private static String generateUniqueFileName(String prefix) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        Random random = new Random();
        int randomNum = random.nextInt(10000);
        return prefix + "-" + timestamp + "-" + randomNum;
    }

    /**
     * 传入file文件（要上传的image.jpg），将其上传到阿里云oss存储，并返回该图片的url
     * @param file 要上传的文件
     * @return String url 返回可访问的图片URL
     * @throws Exception 上传过程中的异常
     */
    public String uploadJPGFile(File file) throws Exception {
        DefaultCredentialProvider credentialsProvider = new DefaultCredentialProvider(
            ossConfig.getAccessKeyId(), 
            ossConfig.getAccessKeySecret()
        );

        String originalName = file.getName();
        String uniqueName = generateUniqueFileName(originalName);
        String objectName = uniqueName + ".jpg";

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(ossConfig.getEndpoint())
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(ossConfig.getRegion())
                .build();

        try {
            InputStream inputStream = new FileInputStream(file);
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), objectName, inputStream);
            ossClient.putObject(putObjectRequest);
            return String.format("https://%s.%s/%s", 
                ossConfig.getBucketName(),
                ossConfig.getEndpoint().replace("https://", ""),
                objectName);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            throw oe;
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            throw ce;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
} 