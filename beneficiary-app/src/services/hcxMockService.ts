import axios from 'axios';

async function generateOutgoingRequest(url: any, payload: any) {
  const response = await axios.post(
    `${process.env.hcx_mock_service}/${url}`,
    payload
  );
  return response;
}

async function isInitiated() {
  const response = await axios.get(
    'https://dev-hcx.swasth.app/hcx-mock-service/v0.7/check/communication/request?requestId=2ee1c00f-b19b-44fc-9d31-12d76efd2951'
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

export { generateOutgoingRequest, sendOTP, verifyOTP, isInitiated };
