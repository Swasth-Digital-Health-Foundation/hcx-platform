import axios from 'axios';

async function postRequest(url: any, payload?: any) {
  console.log("url",`${process.env.registry_url}/${url}`)
  const response = await axios.post(
    `${process.env.registry_url}/${url}`,
    payload
  );
  return response;
}

export { postRequest };