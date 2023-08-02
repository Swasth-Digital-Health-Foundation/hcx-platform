# How to generate an access token to make use of protocol APIs?

### For every request you are sending to HCX,needs access token.
### To generate the access token, utilize the below curl command :

```postman
curl --location 'http://${ENV}-hcx.swasth.app/api/v0.8/participant/auth/token/generate' \
--header 'content-type: application/x-www-form-urlencoded' \
--data-urlencode 'username=${USER_ID}' \
--data-urlencode 'participant_code=${PARTICIPANT_CODE}' \
--data-urlencode 'password=${PASSWORD}'
```
|**Variable Name**|**Details**|**example values**|
| :-: | :-: | :-: |
|ENV|Change the environment which user using|[dev,staging]|
|USER_ID|Add your user id |testuser@gmail.com|
|PARTICIPANT_CODE|organisation code|test-user@swasth-hcx|
|PASSWORD|password sent via email|Opensaber@123