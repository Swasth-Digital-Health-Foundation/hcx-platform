import axios from 'axios';


export const post = async (url, body, headers = {}) => {
  return axios({
    method: 'post',
    url: url,
    data: body,
    headers: headers
  })
}

export const get = async (url, headers = {}) => {
  return axios({
    method: 'get',
    url: url,
    headers: headers
  })
}

export const put = async (url, body, headers = {}) => {
    return axios({
      method: 'put',
      url: url,
      data: body,
      headers: headers
    })
  }