import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import strings from '../../utils/strings';
import { generateToken, searchParticipant } from '../../services/hcxService';
import { generateOutgoingRequest } from '../../services/hcxMockService';
import LoadingButton from '../../components/LoadingButton';

const CoverageEligibility = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [selectedValue, setSelectedValue] = useState<any>();
  const [token, setToken] = useState<string>();
  const [providerName, setProviderName] = useState<string>();
  const [payorName, setPayorName] = useState<string>('');
  const [preauthOrClaimList, setpreauthOrClaimList] = useState<any>([]);
  const [loading, setisLoading] = useState(false);

  const handleRadioChange = (event: any) => {
    setSelectedValue(event.target.value);
  };

  const requestDetails = {
    providerName: providerName,
    ...location.state,
  };

  const [type, setType] = useState<any>([]);

  console.log(requestDetails);
  // console.log(preauthOrClaimList);

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
    {
      key: 'API call ID :',
      value: requestDetails?.apiCallId || '',
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

  const preauthOrClaimListPayload = {
    workflow_id: requestDetails?.workflowId || '',
  };

  const coverageEligibilityPayload = {
    mobile: localStorage.getItem('mobile'),
  };

  const [coverageDetails, setCoverageDetails] = useState<any>([]);

  const getActivePlans = async () => {
    try {
      setisLoading(true);
      let response = await generateOutgoingRequest(
        'bsp/request/list',
        preauthOrClaimListPayload
      );
      let statusCheckCoverageEligibility = await generateOutgoingRequest(
        'bsp/request/list',
        coverageEligibilityPayload
      );
      setCoverageDetails(statusCheckCoverageEligibility.data?.entries);
      setisLoading(false);
      setpreauthOrClaimList(response.data?.entries);
      setType(
        preauthOrClaimList.map((ele: any) => {
          return ele.type;
        })
      );
      if (preauthOrClaimList.length === 2) {
        setSelectedValue(false);
      } else {
        return;
      }
    } catch (err) {
      setisLoading(false);
      console.log(err);
    }
  };

  useEffect(() => {
    getActivePlans();
  }, []);

  const filteredArray = coverageDetails.filter(
    (obj: any) => obj.workflow_id === requestDetails?.workflowId || ''
  );

  const treatmentDetails = [
    {
      key: 'Service type :',
      value: preauthOrClaimList[0]?.claimType || '',
    },
    {
      key: 'Bill amount :',
      value: preauthOrClaimList[0]?.billAmount || '',
    },
  ];

  const treatmentDetailsClaim = [
    {
      key: 'Service type :',
      value: preauthOrClaimList[1]?.claimType || '',
    },
    {
      key: 'Bill amount :',
      value: preauthOrClaimList[1]?.billAmount || '',
    },
  ];

  const supportingDocuments = [
    {
      key: 'Document type :',
      value: preauthOrClaimList[0]?.billingDeatils?.serviceType || '',
    },
  ];

  console.log(type);

  return (
    <div className="-pt-2">
      <div className="mb-3 flex items-center justify-between">
        <button
          disabled={loading}
          onClick={(event: any) => {
            event.preventDefault();
            getActivePlans();
          }}
          className="align-center flex w-20 justify-center rounded bg-primary py-1 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
        >
          Refresh
        </button>
        {loading ? 'Please wait...' : ''}
        <h2 className="sm:text-title-xl1 mb-1 text-end font-semibold text-success dark:text-success">
          {filteredArray[0]?.status === 'Approved' ? (
            <div className="text-success">&#10004; {requestDetails.status}</div>
          ) : (
            <div className="text-warning">Pending</div>
          )}
        </h2>
      </div>
      <h2 className="sm:text-title-xl1 mb-2 text-2xl font-semibold text-black dark:text-white">
        {strings.CLAIM_REQUEST_DETAILS}
      </h2>
      <span className="text-base font-medium">
        {requestDetails.workflowId || ''}
      </span>
      <div className="mt-4 rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="items-center justify-between"></div>
        <div>
          {claimRequestDetails.map((ele: any, index: any) => {
            return (
              <div key={index} className="mb-2">
                <h2 className="text-bold text-base font-bold text-black dark:text-white">
                  {ele.key}
                </h2>
                <span className="text-base font-medium">{ele.value}</span>
              </div>
            );
          })}
        </div>
      </div>
      {preauthOrClaimList.map((ele: any) => {
        return (
          <>
            <h2 className="sm:text-title-xl1 mt-3 text-2xl font-semibold text-black dark:text-white">
              {ele?.type} details :
            </h2>
            <div className="mt-4 rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
              <div className="flex items-center justify-between">
                <h2 className="sm:text-title-xl1 text-1xl mt-2 mb-4 font-semibold text-black dark:text-white">
                  {strings.TREATMENT_AND_BILLING_DETAILS}
                </h2>
              </div>
              <div>
                <div className="mb-2 ">
                  <h2 className="text-bold text-base font-bold text-black dark:text-white">
                    Service type : {ele.claimType}
                  </h2>
                  <h2 className="text-bold text-base font-bold text-black dark:text-white">
                    Bill amount : {ele.billAmount}
                  </h2>
                  {/* <span className="text-base font-medium">
                    Bill amount :{ele.billAmount}
                  </span> */}
                </div>
              </div>
            </div>
          </>
        );
      })}

      {/* {preauthOrClaimList.length === 2 ? (
        <>
          <h2 className="sm:text-title-xl1 mt-3 text-2xl font-semibold text-black dark:text-white">
            Preauth details :
          </h2>
          <div className="mt-4 rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
            <div className="flex items-center justify-between">
              <h2 className="sm:text-title-xl1 text-1xl mt-2 mb-4 font-semibold text-black dark:text-white">
                {strings.TREATMENT_AND_BILLING_DETAILS}
              </h2>
            </div>
            <div>
              {treatmentDetailsClaim.map((ele: any, index: any) => {
                return (
                  <div key={index} className="mb-2 flex gap-1">
                    <h2 className="text-bold text-base font-bold text-black dark:text-white">
                      {ele.key}
                    </h2>
                    <span className="text-base font-medium">{ele.value}</span>
                  </div>
                );
              })}
            </div>
          </div>
        </>
      ) : null} */}

      <div>
        {/* <h2 className="text-bold text-1xl mt-3 font-bold text-black dark:text-white">
          {strings.NEXT_STEP}
        </h2> */}

        {/* Conditionally render radio buttons based on the preauthOrClaimList */}
        {preauthOrClaimList.length === 0 && (
          <>
            <div className="mt-2 mb-2 flex items-center">
              <input
                onChange={handleRadioChange}
                id="default-radio-1"
                type="radio"
                value="Initiate new claim request"
                name="default-radio"
                className="text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 dark:bg-gray-700 dark:border-gray-600 h-4 w-4 focus:ring-2"
              />
              <label
                htmlFor="default-radio-1"
                className="text-gray-900 dark:text-gray-300 ml-2 text-sm font-medium"
              >
                {strings.INITIATE_NEW_CLAIM_REQUEST}
              </label>
            </div>
            <div className="mt-2 mb-2 flex items-center">
              <input
                onChange={handleRadioChange}
                id="default-radio-2"
                type="radio"
                value="Initiate pre-auth request"
                name="default-radio"
                className="text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 dark:bg-gray-700 dark:border-gray-600 h-4 w-4 focus:ring-2"
              />
              <label
                htmlFor="default-radio-2"
                className="text-gray-900 dark:text-gray-300 ml-2 text-sm font-medium"
              >
                {strings.INITIATE_PREAUTH_REQUEST}
              </label>
            </div>
          </>
        )}

        {type.includes('claim') ? (
          // Navigate to home page or take any appropriate action here
          // You can add a link or a button to navigate to the home page
          <button onClick={() => (window.location.href = '/')}>
            Go to Home
          </button>
        ) : type.includes('preauth') ? (
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
              {strings.INITIATE_CLAIM_REQUEST}
            </label>
          </div>
        ) : null}
      </div>

      {/* {preauthOrClaimList.map((ele: any) => {
          if (ele.type === 'claim') {
            return <></>;
          } else {
            return (
              <>
                // <div className="mt-2 mb-2 flex items-center">
                //   <input
                //     onChange={handleRadioChange}
                //     id="default-radio-2"
                //     type="radio"
                //     value="Initiate new claim request"
                //     name="default-radio"
                //     className="text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 dark:bg-gray-700 dark:border-gray-600 h-4 w-4 focus:ring-2"
                //   />
                //   <label
                //     htmlFor="default-radio-2"
                //     className="text-gray-900 dark:text-gray-300 ml-2 text-sm font-medium"
                //   >
                //     {strings.INITIATE_NEW_CLAIM_REQUEST}
                //   </label>
                // </div>

              //   <div className="mt-2 mb-2 flex items-center">
              //     <input
              //       onChange={handleRadioChange}
              //       id="default-radio-2"
              //       type="radio"
              //       value="Initiate pre-auth request"
              //       name="default-radio"
              //       className="text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 dark:bg-gray-700 dark:border-gray-600 h-4 w-4 focus:ring-2"
              //     />
              //     <label
              //       htmlFor="default-radio-2"
              //       className="text-gray-900 dark:text-gray-300 ml-2 text-sm font-medium"
              //     >
              //       {strings.INITIATE_PREAUTH_REQUEST}
              //     </label>
              //   </div>
              // </>
            );
          }
        })} */}
      {/* {preauthOrClaimList.lenght === 2 || preauthOrClaimList.length === 0 ? (
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
        ) : null} */}

      <div className="mb-5 mt-5">
        <button
          disabled={selectedValue === ''}
          onClick={(event: any) => {
            event.preventDefault();
            if (selectedValue === 'Initiate new claim request') {
              navigate('/initiate-claim-request', { state: requestDetails });
            } else if (selectedValue === 'Initiate pre-auth request') {
              navigate('/initiate-preauth-request', {
                state: requestDetails,
              });
            } else {
              navigate('/view-active-request', {
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
  );
};

export default CoverageEligibility;
