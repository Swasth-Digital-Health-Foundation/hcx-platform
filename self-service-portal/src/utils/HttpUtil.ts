import axios from 'axios';


export const post = async (url: string, body: any, headers = {}) => {
  return axios({
    method: 'post',
    url: url,
    data: body,
    headers: headers
  })
}

export const get = async (url: string, headers = {}) => {
  return axios({
    method: 'get',
    url: url,
    headers: headers
  })
}

export const put = async (url: string, body: any, headers = {}) => {
    return axios({
      method: 'put',
      url: url,
      data: body,
      headers: headers
    })
  }