export const API_BASE_URL = process.env.REACT_APP_HCX_PATH;

const request = (options: any) => {
  const headers = new Headers({
    "Content-Type": "application/json",
  });

  const defaults = { headers: headers };
  options = Object.assign({}, defaults, options);

  return fetch(options.url, options).then((response) =>
    response.json().then((json) => {
      if (!response.ok) {
        return Promise.reject(json);
      }
      return json;
    })
  );
};

const urlParams = (params: any) => {
  console.log("Parameterizing", Object.entries(params));
  return Object.entries(params).reduce(
    (url, [key, value]) =>
      value
        ? url === ""
          ? `?${key}=${value}`
          : `${url}&${key}=${value}`
        : url,
    ""
  );
};

export function clearAccessTokens() {
  localStorage.removeItem("ACCESS_TOKEN");
}

// export function login(data: { username: string; password: string }) {
//   return request({
//     url: API_BASE_URL + "/auth/login",
//     method: "POST",
//     body: JSON.stringify(data),
//   });
// }

const formRequest = (options: any) => {

  const headers = {
    'Content-Type': 'application/x-www-form-urlencoded'
  };

  const defaults = { headers: headers };
  options = Object.assign({}, defaults, options);

  return fetch(options.url, options).then((response) =>
    response.text().then((text) => {
      if (!response.ok) {
        const error = {
          status: response.status,
          statusText: response.statusText,
          message: text
        };
        return Promise.reject(error);
      }
      const data = text ? JSON.parse(text) : {};
      return data;
    })
  );
};

export function login(data: { username: string; password: string }) {

const formData = new URLSearchParams();
  formData.append('client_id', 'registry-frontend');
  formData.append('username', data.username);
  formData.append('password', data.password);
  formData.append('grant_type', 'password');

  return formRequest({
    url: API_BASE_URL + "/auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token",
    method: "POST",
    body: formData,
  });
}

export function claims(data: { username: string; password: string }) {
  return request({
    url: API_BASE_URL + "/claims?" + urlParams(data),
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function listRequest(data: { type: string }) {
  return request({
    url: API_BASE_URL + "/request/list",
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function approveCoverageEligibilityRequest(data: {
  request_id: string;
}) {
  return request({
    url: API_BASE_URL + "/coverageeligibility/approve",
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function rejectCoverageEligibilityRequest(data: { request_id: string }) {
  return request({
    url: API_BASE_URL + "/coverageeligibility/reject",
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function approvePreauth(data: {
  request_id: string;
  type: string;
  remarks: string;
  approved_amount: number;
}) {
  return request({
    url: API_BASE_URL + "/preauth/approve",
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function rejectPreauth(data: { request_id: string; type: string }) {
  return request({
    url: API_BASE_URL + "/preauth/reject",
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function approveClaim(data: {
  request_id: string;
  type: string;
  remarks: string;
  approved_amount: number;
}) {
  return request({
    url: API_BASE_URL + "/claim/approve",
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function rejectClaim(data: { request_id: string; type: string }) {
  return request({
    url: API_BASE_URL + "/claim/reject",
    method: "POST",
    body: JSON.stringify(data),
  });
}
