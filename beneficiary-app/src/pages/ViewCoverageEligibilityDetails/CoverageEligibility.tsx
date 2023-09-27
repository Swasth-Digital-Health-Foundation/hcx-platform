import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import strings from '../../utils/strings';
import { generateToken, searchParticipant } from '../../services/hcxService';
import { generateOutgoingRequest } from '../../services/hcxMockService';
import LoadingButton from '../../components/LoadingButton';
import TransparentLoader from '../../components/TransparentLoader';

const CoverageEligibility = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [selectedValue, setSelectedValue] = useState<any>();
  const [token, setToken] = useState<string>();
  const [providerName, setProviderName] = useState<string>();
  const [payorName, setPayorName] = useState<string>('');
  const [preauthOrClaimList, setpreauthOrClaimList] = useState<any>([]);
  const [loading, setisLoading] = useState(false);
  const [coverageDetails, setCoverageDetails] = useState<any>([]);
  const [apicallIds, setapicallIds] = useState<any>([]);

  const handleRadioChange = (event: any) => {
    setSelectedValue(event.target.value);
  };

  // Now, apicallIds will contain an array of apicallId values from the "claim" objects

  const requestDetails = {
    providerName: providerName,
    ...location.state,
    billAmount: location.state?.billAmount || preauthOrClaimList[0]?.billAmount,
    apiCallId: preauthOrClaimList[0]?.apiCallId,
  };

  const [type, setType] = useState<string[]>([]);

  console.log(location.state?.apiCallId);

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
      value: location.state?.apiCallId || '',
    },
  ];

  let workflowId = requestDetails?.workflowId;
  console.log(workflowId);

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

  const tokenGeneration = async () => {
    try {
      const tokenResponse = await generateToken(tokenRequestBody);
      if (tokenResponse.statusText === 'OK') {
        setToken(tokenResponse.data.access_token);
      }
    } catch (err) {
      console.log(err);
    }
  };

  const search = async () => {
    const response = await searchParticipant(participantCodePayload, config);
    setProviderName(response.data?.participants[0].participant_name);

    const payorResponse = await searchParticipant(payorCodePayload, config);
    setPayorName(payorResponse.data?.participants[0].participant_name);
  };

  useEffect(() => {
    try {
      if (token !== undefined) {
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

  // console.log(coverageDetails);

  const getActivePlans = async () => {
    try {
      setisLoading(true);
      let statusCheckCoverageEligibility = await generateOutgoingRequest(
        'bsp/request/list',
        coverageEligibilityPayload
      );
      let response = await generateOutgoingRequest(
        'bsp/request/list',
        preauthOrClaimListPayload
      );
      let preAuthAndClaimList = response.data?.entries;
      setpreauthOrClaimList(preAuthAndClaimList);
      setType(
        response.data?.entries.map((ele: any) => {
          return ele.type;
        })
      );

      let coverageData = statusCheckCoverageEligibility.data?.entries;
      setCoverageDetails(coverageData);

      const entryKey = Object?.keys(coverageDetails[0])[0];
      console.log('entrykey', entryKey);

      // Filter the objects with type "claim"
      const claimObjects = coverageDetails[0][entryKey].filter(
        (obj: any) => obj.type === 'claim'
      );

      // Extract the apicallId values from the "claim" objects
      const apicallIds = claimObjects.map((obj: any) => obj.apiCallId);
      setapicallIds(apicallIds);

      setisLoading(false);

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
    tokenGeneration();
    getActivePlans();
  }, []);

  // const filteredArray = coverageDetails.filter(
  //   (obj: any) => obj.workflow_id === requestDetails?.workflowId || ''
  // );

  console.log('pre-claim', preauthOrClaimList);

  return (
    <>
      {!loading ? (
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
              {coverageDetails[0]?.[workflowId].some(
                (obj: any) => 
                  obj.type === 'coverageeligibility' &&
                  obj.status === 'Approved'
              ) ? (
                <div className="text-success">&#10004; Eligible</div>
              ) : (
                <div className="text-warning">Pending</div>
              )}
            </h2>
          </div>
          <h2 className="sm:text-title-xl1 text-2xl font-semibold text-black dark:text-white">
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
                <div className=" flex items-center justify-between">
                  <h2 className="sm:text-title-xl1 mt-3 text-2xl font-semibold text-black dark:text-white">
                    {ele?.type.charAt(0).toUpperCase() + ele?.type.slice(1)}{' '}
                    details :
                  </h2>
                  {ele?.status === 'Approved' ? (
                    <div className="sm:text-title-xl1 mb-1 text-end font-semibold text-success dark:text-success">
                      &#10004; Approved
                    </div>
                  ) : (
                    <div className="sm:text-title-xl1 mb-1 text-end font-semibold text-warning dark:text-success">
                      Pending
                    </div>
                  )}
                </div>
                <div className="mt-4 rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
                  <div className="flex items-center justify-between">
                    <h2 className="sm:text-title-xl1 text-1xl mt-2 mb-4 font-semibold text-black dark:text-white">
                      {strings.TREATMENT_AND_BILLING_DETAILS}
                    </h2>
                  </div>
                  <div>
                    <div className="mb-2 ">
                      <div className="flex gap-2">
                        <h2 className="text-bold text-base font-bold text-black dark:text-white">
                          Service type :
                        </h2>
                        <span className="text-base font-medium">
                          {ele.claimType}
                        </span>
                      </div>
                      <div className="flex gap-2">
                        <h2 className="text-bold text-base font-bold text-black dark:text-white">
                          Bill amount :
                        </h2>
                        <span className="text-base font-medium">
                          {ele.billAmount}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </>
            );
          })}

          <div>
            {preauthOrClaimList.length === 0 && (
              <>
                <h2 className="text-bold text-1xl mt-3 font-bold text-black dark:text-white">
                  {strings.NEXT_STEP}
                </h2>
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
              <></>
            ) : type.includes('preauth') ? (
              <>
                <h2 className="text-bold text-1xl mt-3 font-bold text-black dark:text-white">
                  {strings.NEXT_STEP}
                </h2>
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
              </>
            ) : null}
          </div>

          <div className="mb-5 mt-5">
            <button
              disabled={selectedValue === ''}
              onClick={(event: any) => {
                event.preventDefault();
                if (selectedValue === 'Initiate new claim request') {
                  navigate('/initiate-claim-request', {
                    state: requestDetails,
                  });
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
      ) : (
        <TransparentLoader />
      )}
    </>
  );
};

export default CoverageEligibility;
