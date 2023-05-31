import axios from 'axios';
import { generateToken } from './KeycloakService';

const baseUrl = process.env.REACT_APP_HCX_PATH;
const apiVersion = process.env.REACT_APP_HCX_API_VERSION;
const adminClientId = process.env.REACT_APP_HCX_ADMIN_CLIENT_ID;
const adminUsername = process.env.REACT_APP_HCX_ADMIN_USERNAME;
const adminPassword = process.env.REACT_APP_HCX_ADMIN_PASSWORD;
const hcxRealm = process.env.REACT_APP_KEYCLOAK_HCX_REALM;

export const post = async (path, body, headers = {}) => {
  const token = await getAdminToken()
  return axios({
    method: 'post',
    url: `${baseUrl}/api/${apiVersion}${path}`,
    data: body,
    headers:{
      "Authorization": `Bearer ${token}`,
      "Content-Type": "application/json",
      ...headers
    }
  })
}

export const get = async (path, body = {}, headers = {}) => {
  const token = await getAdminToken()
  return axios({
    method: 'get',
    url: `${baseUrl}/api/${path}`,
    data: body,
    headers:{
      "Authorization": `Bearer ${token}`,
      "Content-Type": "application/json",
      ...headers
    }
  })
}

export const getToken = async (path, body, headers = {}) => {
  return axios({
    method: 'post',
    url: `${baseUrl}${path}`,
    data: body,
    headers:{
      "Content-Type": "application/x-www-form-urlencoded",
      ...headers
    }
  })
}

async function getAdminToken() {
  return await generateToken(hcxRealm, adminClientId, adminUsername, adminPassword);
}


