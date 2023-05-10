import axios from 'axios';

console.log("process.env.REACT_APP_HCX_PATH", process.env.REACT_APP_HCX_PATH)
export const hcx = axios.create({
    baseURL: process.env.REACT_APP_HCX_PATH,
    timeout: 1000,
    headers : {
        "Authorization": `Bearer xyz`,
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin" : "*"
    }
  });