

import axios from 'axios';

const baseUrl = process.env.REACT_APP_HCX_PATH;
const apiVersion = process.env.REACT_APP_HCX_API_VERSION;
//const adminToken = process.env.REACT_APP_HCX_ADMIN_TOKEN;
const adminToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJMYU9HdVRrYVpsVEtzaERwUng1R25JaXUwV1A1S3VGUUoyb29WMEZnWGx3In0.eyJleHAiOjE2NzgyNjA0NTEsImlhdCI6MTY3NTY2ODQ1MSwianRpIjoiMzM0N2YzMzYtZTEzMi00NjYwLThhMzAtY2ZkNThkNjg3YTNlIiwiaXNzIjoiaHR0cDovL3N0YWdpbmctaGN4LnN3YXN0aC5hcHAvYXV0aC9yZWFsbXMvc3dhc3RoLWhlYWx0aC1jbGFpbS1leGNoYW5nZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIwYzU3NjNkZS03MzJkLTRmZDQtODU0Ny1iMzk2MGMxMzIwZjUiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJyZWdpc3RyeS1mcm9udGVuZCIsInNlc3Npb25fc3RhdGUiOiI2NjlmYjg2ZC0wNGNkLTQxZjAtOTQ2Zi1kZTA4MjczYjkwZDEiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwczovL2xvY2FsaG9zdDo0MjAwIiwiaHR0cHM6Ly9uZGVhci54aXYuaW4iLCJodHRwOi8vbG9jYWxob3N0OjQyMDAiLCJodHRwOi8vbmRlYXIueGl2LmluIiwiaHR0cDovLzIwLjE5OC42NC4xMjgiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkhJRS9ISU8uSENYIiwiZGVmYXVsdC1yb2xlcy1uZGVhciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6ImhjeCBhZG1pbiIsInByZWZlcnJlZF91c2VybmFtZSI6ImhjeC1hZG1pbiIsImdpdmVuX25hbWUiOiJoY3ggYWRtaW4iLCJmYW1pbHlfbmFtZSI6IiIsImVtYWlsIjoiaGN4LWFkbWluQGdtYWlsLmNvbSJ9.OX0XRVGJI21Cdejuf4t1p2rcTAx7unWtJFB0oJE-13PO-gayJJ-IBaOxk_Td0K3O6eoqeHtRBmfCzhso7ahBlEWl7stjNN2eRcLRaXcuXnEdgoblMRyKSAkqFSkKRAmgjAvYcgGTkBax-buuULw-ASijTD4DY98T7L0KYAupupqZ71lHnwwF1CmMKzxGaBg0rCgzRFTgCLzdUUkpKfYDXDhB9G0NmHGFUC5mq6HGM0tWtgchW7wc3v_BPH97BQP_p3pHlidIiF2F14hfh1D_vrpxNbLm_IghelKcJMBcpZX6KNoGjbBelPcrdyqhMr1A2_-9A6aHHkN66eEZOjwB2Q";

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


