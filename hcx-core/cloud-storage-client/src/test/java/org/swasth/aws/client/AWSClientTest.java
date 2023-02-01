//package org.swasth.aws.client;
//
//import org.junit.Test;
//import org.swasth.CloudStorageClient;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//
//public class AWSClientTest {
//
//   public static String FOLDERNAME = "test-folder";
//   public static String BUCKETNAME = "dev-hcx-certificates";
//   public static String CONTENT = "test-content";
//
//   public static String PATH = FOLDERNAME + CONTENT;
//
//   private final CloudStorageClient cloudStorageClient = new CloudStorageClient();
//
//   @Test
//   public void putObjectFolderTest(){
//       cloudStorageClient.putObject(BUCKETNAME,FOLDERNAME,CONTENT);
//      assertTrue(cloudStorageClient.getClient().getObject(BUCKETNAME,FOLDERNAME).toString().contains("test-folder"));
//   }
//
//   @Test
//   public void putObjectFileTest(){
//       cloudStorageClient.putObject(FOLDERNAME,CONTENT);
//      assertTrue(cloudStorageClient.getClient().getObject(BUCKETNAME,FOLDERNAME).toString().contains("test-folder"));
//   }
//
//   @Test
//   public void getUrlTest(){
//       cloudStorageClient.putObject(FOLDERNAME,CONTENT);
//      assertTrue(cloudStorageClient.getUrl(BUCKETNAME,PATH).toString().contains(PATH));
//
//   }
//    @Test
//    public void deleteMultipleObject() {
//        cloudStorageClient.putObject(BUCKETNAME,FOLDERNAME,CONTENT);
//        if(cloudStorageClient.getClient().doesObjectExist(BUCKETNAME,FOLDERNAME)) {
//            cloudStorageClient.deleteMultipleObject(FOLDERNAME,BUCKETNAME);
//        }
//        assertFalse(cloudStorageClient.getClient().doesObjectExist(BUCKETNAME,FOLDERNAME+"/encryption_cert_path.pem"));
//        assertFalse(cloudStorageClient.getClient().doesObjectExist(BUCKETNAME,FOLDERNAME+"/encryption_cert_path.pem"));
//
//    }
//
//}