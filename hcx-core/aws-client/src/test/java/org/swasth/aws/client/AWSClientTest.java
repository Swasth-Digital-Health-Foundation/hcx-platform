//package org.swasth.aws.client;
//
//import org.junit.Test;
//import org.swasth.AWSClient;
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
//   private final AWSClient awsClient = new AWSClient();
//
//   @Test
//   public void putObjectFolderTest(){
//      awsClient.putObject(BUCKETNAME,FOLDERNAME,CONTENT);
//      assertTrue(awsClient.getClient().getObject(BUCKETNAME,FOLDERNAME).toString().contains("test-folder"));
//   }
//
//   @Test
//   public void putObjectFileTest(){
//      awsClient.putObject(FOLDERNAME,CONTENT);
//      assertTrue(awsClient.getClient().getObject(BUCKETNAME,FOLDERNAME).toString().contains("test-folder"));
//   }
//
//   @Test
//   public void getUrlTest(){
//      awsClient.putObject(FOLDERNAME,CONTENT);
//      assertTrue(awsClient.getUrl(BUCKETNAME,PATH).toString().contains(PATH));
//
//   }
//    @Test
//    public void deleteMultipleObject() {
//        awsClient.putObject(BUCKETNAME,FOLDERNAME,CONTENT);
//        if(awsClient.getClient().doesObjectExist(BUCKETNAME,FOLDERNAME)) {
//            awsClient.deleteMultipleObject(FOLDERNAME,BUCKETNAME);
//        }
//        assertFalse(awsClient.getClient().doesObjectExist(BUCKETNAME,FOLDERNAME+"/encryption_cert_path.pem"));
//        assertFalse(awsClient.getClient().doesObjectExist(BUCKETNAME,FOLDERNAME+"/encryption_cert_path.pem"));
//
//    }
//
//}