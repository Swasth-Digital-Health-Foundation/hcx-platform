import axios from 'axios';

async function protocolApiPostRequest(method: any, payload: any) {
  const response = await axios.post(
    `${process.env.protocol_api_base_url}create/${method}`,
    payload
  );
  return response;
}

export { protocolApiPostRequest };
