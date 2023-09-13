import axios from 'axios';

const base_url = "http://aa5c04ed467c04ea89789cead03e4275-352660981.ap-south-1.elb.amazonaws.com:8081/api/v1/Beneficiary";

async function postRequest(method: any, payload: any) {
  const response = await axios.post(`${base_url}/${method}`, payload);
  return response;
}

export { postRequest };
