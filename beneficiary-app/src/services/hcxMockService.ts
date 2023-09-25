import axios from 'axios';

async function generateOutgoingRequest(url: any, payload: any) {
  const response = await axios.post(
    `${process.env.hcx_mock_service}/${url}`,
    payload
  );
  return response;
}

async function isInitiated(payload: any) {
  const response = await axios.post(
    `https://dev-hcx.swasth.app/hcx-mock-service/v0.7/check/communication/request`,payload
  );
  return response;
}

async function createCommunicationOnRequest(payload: any) {
  const response = await axios.post(
    'https://dev-hcx.swasth.app/hcx-mock-service/v0.7/create/communication/on_request',
    payload
  );
  return response;
}

async function sendOTP(payload: any) {
  const response = await axios.post(
    `${process.env.hcx_mock_service}/send/otp`,
    payload,
    {
      headers: {
        'Access-Control-Allow-Origin': '*',
      },
    }
  );
  return response;
}

async function verifyOTP(payload: any) {
  const response = await axios.post(
    `${process.env.hcx_mock_service}/verify/otp`,
    payload
  );
  return response;
}

export {
  generateOutgoingRequest,
  sendOTP,
  verifyOTP,
  isInitiated,
  createCommunicationOnRequest,
};
