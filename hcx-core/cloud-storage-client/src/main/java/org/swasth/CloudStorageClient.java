package org.swasth;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.net.URL;

public class CloudStorageClient implements ICloudService {

    private final String accessKey;
    private final String secretKey;

    public CloudStorageClient(String accesskey, String secretKey) {
        this.accessKey = accesskey;
        this.secretKey = secretKey;
    }

    public AmazonS3 getClient() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withRegion(Regions.AP_SOUTH_1)
                .build();
    }

    public void putObject(String folderName, String bucketName) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName + "/", new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
    }

    public void putObject(String bucketName, String folderName, String content) {
        getClient().putObject(bucketName, folderName, content);
    }

    public URL getUrl(String bucketName, String path) {
        return getClient().getUrl(bucketName, path);
    }
}
