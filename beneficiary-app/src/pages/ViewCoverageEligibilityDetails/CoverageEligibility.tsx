import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import strings from '../../utils/strings';
import { generateToken, searchParticipant } from '../../services/hcxService';

const CoverageEligibility = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [selectedValue, setSelectedValue] = useState<string>('');
  const [token, setToken] = useState<string>();
  const [providerName, setProviderName] = useState<string>();
  const [payorName, setPayorName] = useState<string>('');

  const handleRadioChange = (event: any) => {
    setSelectedValue(event.target.value);
  };

  const requestDetails = {
    providerName: providerName,
    ...location.state,
  };

  console.log(requestDetails)

  const claimRequestDetails: any = [
    {
      key: 'Provider name :',
      value: providerName || '',
    },
    {
      key: 'Participant code :',
      value: requestDetails?.participantCode || '',
    },
    {
      key: 'Treatment/Service type :',
      value: requestDetails?.serviceType || '',
    },
    {
      key: 'Payor name :',
      value: payorName,
    },
    {
      key: 'Insurance ID :',
      value: requestDetails?.insuranceId || '',
    },
  ];

  const participantCodePayload = {
    filters: {
      participant_code: { eq: location.state?.participantCode },
    },
  };

  const payorCodePayload = {
    filters: {
      participant_code: { eq: location.state?.payorCode },
    },
  };

  const tokenRequestBody = {
    username: process.env.TOKEN_GENERATION_USERNAME,
    password: process.env.TOKEN_GENERATION_PASSWORD,
  };

  const config = {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };

  useEffect(() => {
    const search = async () => {
      try {
        const tokenResponse = await generateToken(tokenRequestBody);
        if (tokenResponse.statusText === 'OK') {
          console.log(tokenResponse.data.access_token);
          setToken(tokenResponse.data.access_token);
        }
      } catch (err) {
        console.log(err);
      }
    };
    search();
  }, []);

  useEffect(() => {
    try {
      if (token !== undefined) {
        const search = async () => {
          const response = await searchParticipant(
            participantCodePayload,
            config
          );
          setProviderName(response.data?.participants[0].participant_name);

          const payorResponse = await searchParticipant(
            payorCodePayload,
            config
          );
          setPayorName(payorResponse.data?.participants[0].participant_name);
        };
        search();
      }
    } catch (err) {
      console.log(err);
    }
  }, [token]);

  return (
    <div className="-pt-2 w-full sm:p-12.5 xl:p-1">
      <h2 className="sm:text-title-xl1 mb-1 text-end font-semibold text-success dark:text-success">
        {requestDetails?.status !== 'eligible' ? (
          <div className="text-warning">{requestDetails.status}</div>
        ) : (
          <>&#10004; Eligible</>
        )}
      </h2>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="items-center justify-between">
          <h2 className="sm:text-title-xl1 text-2xl font-semibold text-black dark:text-white">
            {strings.CLAIM_REQUEST_DETAILS}
          </h2>
          <h2 className="text-bold mt-2 text-base font-bold text-black dark:text-white">
            Claim ID :
          </h2>
          <span className="text-base font-medium">
            {requestDetails.claim_id}
          </span>
        </div>
        <div>
          {claimRequestDetails.map((ele: any, index: any) => {
            return (
              <div key={index}>
                <h2 className="text-bold text-base font-bold text-black dark:text-white">
                  {ele.key}
                </h2>
                <span className="text-base font-medium">{ele.value}</span>
              </div>
            );
          })}
        </div>
      </div>
      <div className="border-gray-300 my-4 border-t "></div>

      <div className="mt-4">
        <h2 className="text-bold text-1xl mt-1 font-bold text-black dark:text-white">
          {strings.NEXT_STEP}
        </h2>
        <div className="mb-3 mt-4 flex items-center">
          <input
            onChange={handleRadioChange}
            id="default-radio-1"
            type="radio"
            value="Initiate pre-auth request"
            name="default-radio"
            className="text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 dark:bg-gray-700 dark:border-gray-600 h-4 w-4 focus:ring-2"
          />
          <label
            htmlFor="default-radio-1"
            className="text-gray-900 dark:text-gray-300 ml-2 text-sm font-medium"
          >
            {strings.INITIATE_PREAUTH_REQUEST}
          </label>
        </div>
        <div className="mt-2 mb-2 flex items-center">
          <input
            onChange={handleRadioChange}
            id="default-radio-2"
            type="radio"
            value="Initiate new claim request"
            name="default-radio"
            className="text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 dark:bg-gray-700 dark:border-gray-600 h-4 w-4 focus:ring-2"
          />
          <label
            htmlFor="default-radio-2"
            className="text-gray-900 dark:text-gray-300 ml-2 text-sm font-medium"
          >
            {strings.INITIATE_NEW_CLAIM_REQUEST}
          </label>
        </div>

        <div className="mb-5 mt-5">
          <button
            disabled={selectedValue === ''}
            onClick={(event: any) => {
              event.preventDefault();
              if (selectedValue === 'Initiate new claim request') {
                navigate('/initiate-claim-request', { state: requestDetails });
              } else {
                navigate('/initiate-preauth-request', {
                  state: requestDetails,
                });
              }
            }}
            className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
          >
            {strings.PROCEED}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CoverageEligibility;
