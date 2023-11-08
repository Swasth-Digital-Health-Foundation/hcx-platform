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
    `${process.env.hcx_mock_service}/check/communication/request`,
    payload
  );
  return response;
}

async function createCommunicationOnRequest(payload: any) {
  const response = await axios.post(
    `${process.env.hcx_mock_service}/create/communication/on_request`,
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

async function getConsultationDetails(workflow_id: any) {
  const response = await axios.get(
    `${process.env.hcx_mock_service}/consultation/${workflow_id}`
  );
  return response;
}

const getCoverageEligibilityRequestList = async (setLoading: any, requestPayload: any, setActiveRequests: any, setFinalData: any, setDisplayedData: any) => {
  try {
    setLoading(true);
    let response = await generateOutgoingRequest(
      "bsp/request/list",
      requestPayload
    );
    const data = response.data.entries;
    setActiveRequests(data);

    const coverageArray = [];
    const claimArray = [];

    for (const entry of data) {
      // Iterate through each entry in the input data.
      const key = Object.keys(entry)[0];
      const objects = entry[key];

      if (objects.length === 1 && objects[0].type === "claim") {
        // If there's only one object and its type is "claim," add it to claimArray.
        claimArray.push(objects[0]);
      } else {
        // If there's more than one object or any object with type "coverageeligibility," add them to coverageArray.
        coverageArray.push(
          ...objects.filter((obj: any) => obj.type === "coverageeligibility")
        );
      }
    }
    // Create a new array containing both claimArray and coverageArray.
    const newArray = [...claimArray, ...coverageArray];
    const sortedData = newArray.slice().sort((a: any, b: any) => {
      return b.date - a.date;
    });

    setFinalData(sortedData);
    setDisplayedData(sortedData.slice(0, 5));
    setLoading(false);
  } catch (err) {
    setLoading(false);
  }
};

export {
  generateOutgoingRequest,
  sendOTP,
  verifyOTP,
  isInitiated,
  createCommunicationOnRequest,
  getConsultationDetails,
  getCoverageEligibilityRequestList
};
