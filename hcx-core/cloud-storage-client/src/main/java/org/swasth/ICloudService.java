package org.swasth;

import java.net.URL;

public interface ICloudService {

    void putObject(String folderName, String bucketName);

    void putObject(String bucketName, String folderName, String content);

    URL getUrl(String bucketName, String path);

}