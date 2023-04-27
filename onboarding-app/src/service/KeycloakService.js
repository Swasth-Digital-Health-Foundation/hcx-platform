import { get, post, put } from '../utils/HttpUtil';

const hcxUrl = process.env.REACT_APP_HCX_PATH;
const adminRealm = process.env.REACT_APP_KEYCLOAK_ADMIN_REALM;
const hcxRealm = process.env.REACT_APP_KEYCLOAK_HCX_REALM;
const keycloakAdminUsername = process.env.REACT_APP_KEYCLOAK_ADMIN_USERNAME;
const keycloakAdminPassword = process.env.REACT_APP_KEYCLOAK_ADMIN_PASSWORD;
const keycloakAdminClientId = process.env.REACT_APP_KEYCLOAK_ADMIN_CLIENT_ID;

export async function isPasswordSet(userId) {
    const accessToken = await generateKeycloakAdminToken();
    const headers = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
    };

    return new Promise((resolve, reject) => {
        get(`${hcxUrl}/auth/admin/realms/${hcxRealm}/users/${userId}/credentials`, headers)
        .then(function (response) {
            let isPasswordSet = false;
            if (response.data.length != 0) {
                isPasswordSet = true;
            } 
            resolve(isPasswordSet);
          })
          .catch(function (error) {
            console.error(error);
            reject(error);
          });
      });
}

export async function setPassword(userId, password) {
    const accessToken = await generateKeycloakAdminToken();
    const headers = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
    };
    const body = { 'type': 'password', 'temporary': false, 'value': password }

    return new Promise((resolve, reject) => {
        put(`${hcxUrl}/auth/admin/realms/${hcxRealm}/users/${userId}/reset-password`, body, headers)
        .then(function (response) {
            let isSuccessResp = false;
            if (response.status === 204) {
                isSuccessResp = true;
            } 
            resolve(isSuccessResp);
          })
          .catch(function (error) {
            console.error(error);
            reject(error);
          });
      });
}

export async function generateToken(realm, clietId, username, password){
    
    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    };

    const body = {
        'client_id': clietId,
        'username': username, 
        'password': password, 
        'grant_type': 'password'
    }

    return new Promise((resolve, reject) => {
        post(`${hcxUrl}/auth/realms/${realm}/protocol/openid-connect/token`, body, headers)
        .then(function (response) {
            const accessToken = response.data.access_token;
            resolve(accessToken);
          })
          .catch(function (error) {
            console.error(error);
            reject(error);
          });
      });
}

async function generateKeycloakAdminToken(){
    return await generateToken(adminRealm, keycloakAdminClientId, keycloakAdminUsername, keycloakAdminPassword);  
}
