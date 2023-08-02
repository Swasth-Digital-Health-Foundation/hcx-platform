# How to generate an access token to make use of protocol APIs?

### For every request you are sending to HCX,needs access token.
### To generate the access token, utilize the below curl command :

```postman
curl --location 'http://${env}-hcx.swasth.app/api/v0.8/participant/auth/token/generate' \
--header 'content-type: application/x-www-form-urlencoded' \
--data-urlencode 'username=${user_id}' \
--data-urlencode 'participant_code=${participant_code}' \
--data-urlencode 'password=${password}'
```
|**Variable Name**|**Details**|**example values**|
| :-: | :-: | :-: |
|env|Modify the environment your using with HCX|[dev,staging]|
|user_id|user id given to the user|testuser@gmail.com|
|participant_code|organisation code|test-user@swasth-hcx|
|password|password sent via email|Opensaber@123

### HCX instance would respond with the API token upon successful validation of the username and password values:
```java
HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: no-cache, no-store
Response-Body:
 {
   "access_token": "", // This is the token that the client can use to access protected resources on the server. The token is a JSON Web Token (JWT) that contains claims about the user and the client
   "expires_in": 6000, // The number of seconds until the access token expires. After this time, the client must obtain a new access token.
   "refresh_expires_in": 300, // The number of seconds until the refresh token expires. After this time, the client must obtain a new refresh token. 
   "refresh_token": "", // A token that the client can use to obtain a new access token after the current access token expires. The refresh token is also a JWT that contains claims about the user and the client. 
   "token_type": "Bearer", // The type of token, which is usually "bearer".
   "not-before-policy": 1607576887, // The time before which the token is not valid.
   "session_state": "a1415249-c4eb-49e1-97ea-42c7c91db874", // A unique identifier for the user's session.
   "scope": "profile email" // The scopes that the client has requested access to.
 }
```
