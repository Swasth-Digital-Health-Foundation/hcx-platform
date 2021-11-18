package org.swasth.job.data;

public interface ParticipantDetails {

    String senderDetails = "{ \"participantCode\": \"123456\", \"hfrCode\": \"0001\", \"participantName\": \"Test Provider\", \"roles\": \"admin\", \"address\": { \"plot\": \"2-340\", \"street\": \"Baker street\", \"landmark\": \"clock tower\", \"locality\": \"Nagar\", \"village\": \"Miyapur\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500045\" }, \"email\": [ \"testprovider@gmail.com\" ], \"phone\": [ \"040-98765345\" ], \"mobile\": [ \"987654321\" ], \"status\": \"Created\", \"signingCertPath\": \"urn:isbn:0-476-27557-4\", \"encryptionCert\": \"urn:isbn:0-4234\", \"endpointUrl\": \"/testurl\", \"paymentDetails\": { \"accountNumber\":\"123465\", \"ifscCode\":\"HDFC\" } }";
    String receiverDetails = "{ \"participantCode\": \"123456\", \"hfrCode\": \"0001\", \"participantName\": \"Test Payor\", \"roles\": \"admin\", \"address\": { \"plot\": \"123\", \"street\": \"plaza street\", \"landmark\": \"clock tower\", \"locality\": \"Nagar\", \"village\": \"banjara hills\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500090\" }, \"email\": [ \"testpayor@gmail.com\" ], \"phone\": [ \"040-45345634\" ], \"mobile\": [ \"3254345345345\" ], \"status\": \"Created\", \"signingCertPath\": \"urn:isbn:0-476-27557-4\", \"encryptionCert\": \"urn:isbn:0-4234\", \"endpointUrl\": \"/testurl\", \"paymentDetails\": { \"accountNumber\":\"345435345345\", \"ifscCode\":\"ICICI\" } }";

}
