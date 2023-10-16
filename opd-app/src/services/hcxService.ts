import axios from "axios";

async function searchParticipant(payload: any, config?: any) {
  const response = await axios.post(
    `${process.env.hcx_service}/participant/search`,
    payload,
    config
  );
  return response;
}

const payload = {
  username: process.env.SEARCH_PARTICIPANT_USERNAME,
  password: process.env.SEARCH_PARTICIPANT_PASSWORD,
};

async function generateToken() {
  const response = await axios.post(
    `${process.env.hcx_service}/participant/auth/token/generate`,
    payload,
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    }
  );
  return response;
}

async function login(loginCredentials: any) {
  const response = await axios.post(
    `${process.env.hcx_service}/participant/auth/token/generate`,
    loginCredentials,
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    }
  );
  return response;
}

export { searchParticipant, generateToken ,login};
