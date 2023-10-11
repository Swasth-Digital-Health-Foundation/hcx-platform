import { get, post, put } from '../utils/HttpUtil';
import qs from 'qs';

window.console.log("react env",process.env);
const hcxUrl = process.env.REACT_APP_HCX_PATH;
const adminRealm = process.env.REACT_APP_KEYCLOAK_ADMIN_REALM;
const hcxRealm = process.env.REACT_APP_KEYCLOAK_HCX_REALM;
const keycloakAdminUsername = process.env.REACT_APP_KEYCLOAK_ADMIN_USERNAME;
const keycloakAdminPassword = process.env.REACT_APP_KEYCLOAK_ADMIN_PASSWORD;
const keycloakAdminClientId = process.env.REACT_APP_KEYCLOAK_ADMIN_CLIENT_ID;
const hcxRealmUser = process.env.REACT_APP_KEYCLOAK_HCX_REALM_USER;
const apiVersion = process.env.REACT_APP_PARTICIPANT_API_VERSION;

export async function isPasswordSet(userId: any) {
    const accessToken = await generateKeycloakAdminToken();
    const headers = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
    };

    return new Promise((resolve, reject) => {
        get(`${hcxUrl}/auth/admin/realms/${hcxRealm}/users/${userId}/credentials`, headers)
        .then(function (response: { data: string | any[]; }) {
            let isPasswordSet = false;
            if (response.data.length != 0) {
                isPasswordSet = true;
            } 
            resolve(isPasswordSet);
          })
          .catch(function (error: any) {
            console.error(error);
            reject(error);
          });
      });
}

export async function setPassword(userId: any, password: any) {
    const accessToken = await generateKeycloakAdminToken();
    const headers = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
    };
    const body = { 'type': 'password', 'temporary': false, 'value': password }

    return new Promise((resolve, reject) => {
        put(`${hcxUrl}/auth/admin/realms/${hcxRealm}/users/${userId}/reset-password`, body, headers)
        .then(function (response: { status: number; }) {
            let isSuccessResp = false;
            if (response.status === 204) {
                isSuccessResp = true;
            } 
            resolve(isSuccessResp);
          })
          .catch(function (error: any) {
            console.error(error);
            reject(error);
          });
      });
}

export async function generateToken(realm: string | undefined, clietId: string | undefined, username: string | undefined, password: string | undefined){
    
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
        .then(function (response: { data: { access_token: any; }; }) {
            const accessToken = response.data.access_token;
            resolve(accessToken);
          })
          .catch(function (error: any) {
            console.error(error);
            reject(error);
          });
      });
}

async function generateKeycloakAdminToken(){
    return await generateToken(adminRealm, keycloakAdminClientId, keycloakAdminUsername, keycloakAdminPassword);  
}



export async function generateTokenUser(username: any, password: any){
    
    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    };
    const body = {
        'username': username, 
        'password': password, 
    }
    return new Promise((resolve, reject) => {
        post(`${hcxUrl}/api/${apiVersion}/user/auth/token/generate`, qs.stringify(body), headers)
        .then(function (response: { data: { access_token: any; }; }) {
            console.log("response", response);
            const accessToken = response.data.access_token;
            resolve(accessToken);
          })
          .catch(function (error: any) {
            console.error(error);
            reject(error);
          });
      });
}


export async function setUserPassword(userId: any, password: any) {
    const accessToken = await generateKeycloakAdminToken();
    const headers = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
    };
    const body = { 'type': 'password', 'temporary': false, 'value': password }

    return new Promise((resolve, reject) => {
        put(`${hcxUrl}/auth/admin/realms/${hcxRealmUser}/users/${userId}/reset-password`, body, headers)
        .then(function (response: { status: number; }) {
            let isSuccessResp = false;
            if (response.status === 204) {
                isSuccessResp = true;
            } 
            resolve(isSuccessResp);
          })
          .catch(function (error: any) {
            console.error(error);
            reject(error);
          });
      });
}


export async function resetPassword(userId: any) {
    const accessToken = await generateKeycloakAdminToken();
    const headers = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
    };
    const body =["UPDATE_PASSWORD"]

    return new Promise((resolve, reject) => {
        put(`${hcxUrl}/auth/admin/realms/${hcxRealmUser}/users/${userId}/execute-actions-email`, body, headers)
        .then(function (response: { status: number; }) {
            let isSuccessResp = false;
            if (response.status === 204) {
                isSuccessResp = true;
            } 
            resolve(isSuccessResp);
          })
          .catch(function (error: any) {
            console.error(error);
            reject(error);
          });
      });
}
