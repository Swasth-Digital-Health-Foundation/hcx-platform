{
  "@context": [
    "https://www.w3.org/2018/credentials/v1",
    "https://www.w3.org/2018/credentials/examples/v1"
  ],
  "id": "http://hcxprotocol.io/credentials/3732",
  "type": ["VerifiableCredential"],
  "issuer": "https://hcxprotocol.io/participant/565049",
  "issuanceDate": "${issuanceDate}",
  "expirationDate": "${expirationDate}",
  "preferredHCXPath": "http://hcx.swasth.app/api/v0.8/",
  "credentialSubject": {
    "id": "${subjectId}",
    "payload": ${payload}
  },
  "proof": {
    "type": "Ed25519Signature2020",
    "created": "${proofCreated}",
    "verificationMethod": "https://hcxprotocol.io/issuers/565049#key-1",
    "proofPurpose": "assertionMethod",
    "proofValue": "${proofValue}"
  }
}
