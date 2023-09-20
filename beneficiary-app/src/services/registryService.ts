import axios from 'axios';

async function postRequest(method: any, payload: any) {
  const response = await axios.post(
    `${process.env.registry_url}/${method}`,
    payload
  );
  return response;
}

export { postRequest };
