import axios from 'axios';

async function generateOutgoingRequest(method: any, payload: any) {
  const response = await axios.post(
    `${process.env.hcx_mock_service}/create/${method}`,
    payload
  );
  return response;
}

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

export { generateOutgoingRequest, sendOTP, verifyOTP };
