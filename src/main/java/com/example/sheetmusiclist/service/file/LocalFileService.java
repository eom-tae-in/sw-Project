package com.example.sheetmusiclist.service.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.sheetmusiclist.exception.FileUploadFailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
@PropertySource("classpath:pdf.properties")
@RequiredArgsConstructor
public class LocalFileService implements FileService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;



    @Override
    public void upload(MultipartFile file, String filename) {
        try {

            ObjectMetadata obj = new ObjectMetadata();
            obj.setContentLength(file.getInputStream().available());
            amazonS3.putObject(bucket, filename, file.getInputStream(), obj);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            throw new FileUploadFailureException();
        }
    }

    @Override
    public void delete(String filename) {
        amazonS3.deleteObject(bucket,filename);

    }
}


