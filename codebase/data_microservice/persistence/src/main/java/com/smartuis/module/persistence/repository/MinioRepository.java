package com.smartuis.module.persistence.repository;

import com.smartuis.module.domain.repository.StorageRepository;
import com.smartuis.module.persistence.exceptions.UploadFileException;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.SnowballObject;
import io.minio.UploadSnowballObjectsArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.List;


@Repository
public class MinioRepository implements StorageRepository {

    private MinioClient minioClient;
    @Value("${minio.bucket}")
    private String bucketName;

    public MinioRepository(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void saveFile(File file, String pathname){
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            List<SnowballObject> objects = List.of(new SnowballObject(pathname,  fileInputStream, file.length(), ZonedDateTime.now()));

            ObjectWriteResponse minioResponse = minioClient.uploadSnowballObjects(
                    UploadSnowballObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build()
            );

            System.out.println("Respuesta Minio: " + minioResponse.toString());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new UploadFileException("Hubo un error subiendo el archivo");
        }

    }

    @Override
    public void saveFile(MultipartFile file, String pathname){
        try {
            List<SnowballObject> objects = List.of(new SnowballObject(pathname,  file.getInputStream(), file.getSize(), ZonedDateTime.now()));

            ObjectWriteResponse minioResponse = minioClient.uploadSnowballObjects(
                    UploadSnowballObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build()
            );

            System.out.println("Respuesta Minio: " + minioResponse.toString());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new UploadFileException("Hubo un error subiendo el archivo");
        }

    }
}
