import axios from 'axios';

async function hcxPostRequest(method: any, payload: any, config: any) {
  const response = await axios.post(
    `${process.env.hcx_service}/${method}`,
    payload,
    config
  );
  return response;
}

async function generateToken(payload: any) {
  const response = await axios.post(
    `${process.env.hcx_service}/participant/auth/token/generate`,
    payload,
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }
  );
  return response;
}

export { hcxPostRequest, generateToken };
