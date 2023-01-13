

import axios from 'axios';

const baseUrl = process.env.REACT_APP_HCX_PATH;
const adminToken = process.env.REACT_APP_HCX_ADMIN_TOKEN;


export const sendData = async (path, body, headers = {}) => {
  return axios({
    method: 'post',
    url: `${baseUrl}${path}`,
    data: body,
    headers:{
      "Authorization": "Bearer " + adminToken,
      "Content-Type": "application/json",
      ...headers
    }
  })
}


