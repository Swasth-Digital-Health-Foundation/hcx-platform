import axios from 'axios';

// const base_url = 'https://dev-hcx.swasth.app/hcx-mock-service/v0.7/';

async function sendOTP(method: any, payload: any) {
  const response = await axios.post(
    `${process.env.hcx_mock_service}${method}`,
    payload
  );
  return response;
}

async function verifyOTP(method: any, payload: any) {
  const response = await axios.post(
    `${process.env.hcx_mock_service}${method}`,
    payload
  );
  return response;
}

export { sendOTP, verifyOTP };
