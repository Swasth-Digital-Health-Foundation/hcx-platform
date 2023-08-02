# How to generate an access token to make use of protocol APIs?

### For every request you are sending to HCX needs access token for security reasons
### To generate the token, utilize the below curl command for access token generation:

```postman
curl --location 'http://${ENV}-hcx.swasth.app/api/v0.8/participant/auth/token/generate' \
--header 'content-type: application/x-www-form-urlencoded' \
--data-urlencode 'username=${USER_ID}' \
--data-urlencode 'participant_code=${PARTICIPANT_CODE}' \
--data-urlencode 'password=${PASSWORD}'
```
|**Config Variable Name**|**Details**|**example values**|
| :-: | :-: | :-: |
|ENV|Change the environment which user using|[dev,staging]|
|USER_ID|Add your user id |testuser@gmail.com|
|participantCode|organisation code|test-user@swasth-hcx|
|password|password sent in email|Opensaber@123