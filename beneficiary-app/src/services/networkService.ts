import axios from 'axios';

const registry_url =
  'http://aa5c04ed467c04ea89789cead03e4275-352660981.ap-south-1.elb.amazonaws.com:8081/api/v1/Beneficiary';
// const mockService_api_url = 'https://dev-hcx.swasth.app/hcx-mock-service/v0.7/';

async function postRequest(method: any, payload: any) {
  const response = await axios.post(`${registry_url}/${method}`, payload);
  return response;
}

async function hcxRegistryPostRequest(method: any, payload: any, config: any) {
  const response = await axios.post(
    `https://dev-hcx.swasth.app/api/v0.8/${method}`,
    payload,
    config
  );
  return response;
}

async function generateToken(payload: any) {
  const response = await axios.post(
    `https://dev-hcx.swasth.app/api/v0.8/participant/auth/token/generate`,
    payload,
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }
  );
  return response;
}

export {
  postRequest,
  hcxRegistryPostRequest,
  generateToken,
};
