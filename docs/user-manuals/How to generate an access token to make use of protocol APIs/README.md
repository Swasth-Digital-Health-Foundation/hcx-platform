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