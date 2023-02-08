import { get, post, put } from '../utils/HttpUtil';

const hcxUrl = process.env.REACT_APP_HCX_PATH;
const realmName = process.env.REACT_APP_KEYCLOAK_REALM_NAME;

export async function isPasswordSet(userId) {
    const accessToken = await generateToken();
    const headers = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
    };

    return new Promise((resolve, reject) => {
        get(`${hcxUrl}/auth/admin/realms/${realmName}/users/${userId}/credentials`, headers)
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
    const accessToken = await generateToken();
    const headers = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
    };
    const body = { 'type': 'password', 'temporary': false, 'value': password }

    return new Promise((resolve, reject) => {
        put(`${hcxUrl}/auth/admin/realms/${realmName}/users/${userId}/reset-password`, body, headers)
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

export async function generateToken(){
    
    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    };

    const body = {
        'client_id': 'admin-cli',
        'username': process.env.REACT_APP_KEYCLOAK_ADMIN_USERNAME, 
        'password': process.env.REACT_APP_KEYCLOAK_ADMIN_PASSWORD, 
        'grant_type': 'password'
    }

    return new Promise((resolve, reject) => {
        post(`${hcxUrl}/auth/realms/master/protocol/openid-connect/token`, body, headers)
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