

import axios from 'axios';

const baseUrl = process.env.REACT_APP_HCX_PATH;
const apiVersion = process.env.REACT_APP_HCX_API_VERSION;
const adminToken = process.env.REACT_APP_HCX_ADMIN_TOKEN;


export const post = async (path, body, headers = {}) => {
  return axios({
    method: 'post',
    url: `${baseUrl}/api/${apiVersion}${path}`,
    data: body,
    headers:{
      "Authorization": "Bearer " + adminToken,
      "Content-Type": "application/json",
      ...headers
    }
  })
}

export const get = async (path, body = {}, headers = {}) => {
  return axios({
    method: 'get',
    url: `${baseUrl}/api/${path}`,
    data: body,
    headers:{
      "Authorization": "Bearer " + adminToken,
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


