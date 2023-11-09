import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import strings from '../../utils/strings';
import { generateToken, searchParticipant } from '../../services/hcxService';
import { generateOutgoingRequest } from '../../services/hcxMockService';
import TransparentLoader from '../../components/TransparentLoader';
import * as _ from "lodash";
import { ArrowDownTrayIcon, ArrowPathIcon } from "@heroicons/react/24/outline";

const CoverageEligibility = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [token, setToken] = useState<string>();
  const [providerName, setProviderName] = useState<string>();
  const [payorName, setPayorName] = useState<string>('');
  const [preauthOrClaimList, setpreauthOrClaimList] = useState<any>([]);
  const [loading, setisLoading] = useState(false);
  const [coverageDetails, setCoverageDetails] = useState<any>([]);
  const [coverageEligibilityStatus, setcoverageStatus] = useState<any>([]);
  const [apicallIdForClaim, setApicallID] = useState<any>();

  const requestDetails = {
    ...location.state,
    providerName: providerName,
    billAmount: location.state?.billAmount || preauthOrClaimList[0]?.billAmount,
    apiCallId: apicallIdForClaim,
  };

  const [type, setType] = useState<string[]>([]);

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

  const participantCodePayload = {
    filters: {
      participant_code: { eq: location.state?.participantCode },
    },
  };

  const payorCodePayload = {
    filters: {
      participant_code: {
        eq: location.state?.payorCode || location.state?.payor,
      },
    },
  };

  const config = {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };

  const tokenGeneration = async () => {
    try {
      const tokenResponse = await generateToken();
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
    app: 'BSP',
  };

  const coverageEligibilityPayload = {
    mobile: localStorage.getItem('mobile'),
    app: 'BSP',
  };

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
      for (const entry of preAuthAndClaimList) {
        if (entry.type === 'claim') {
          setApicallID(entry.apiCallId);
          break;
        }
      }
      setType(
        response.data?.entries.map((ele: any) => {
          return ele.type;
        })
      );
      let coverageData = statusCheckCoverageEligibility.data?.entries;
      setCoverageDetails(coverageData);
      setisLoading(false);
    } catch (err) {
      setisLoading(false);
      console.log(err);
    }
  };

  useEffect(() => {
    tokenGeneration();
    getActivePlans();
  }, []);

  useEffect(() => {
    for (const entry of preauthOrClaimList) {
      if (entry.type === 'claim') {
        setApicallID(entry.apiCallId);
        break;
      }
    }
  }, []);

  let coverageStatus = coverageDetails.find(
    (entryObj: any) => Object.keys(entryObj)[0] === workflowId
  );

  useEffect(() => {
    if (
      coverageStatus &&
      coverageStatus[workflowId].some(
        (obj: any) => obj.type === 'coverageeligibility'
      )
    ) {
      // If it exists, find the object with type "coverageeligibility" and return its status
      const eligibilityObject = coverageStatus[workflowId].find(
        (obj: any) => obj.type === 'coverageeligibility'
      );
      const status = eligibilityObject.status;
      setcoverageStatus(status);
    } else {
      console.log(
        "Object with type 'coverageeligibility' not found for the given ID."
      );
    }
  }, [coverageStatus]);

  const hasClaimApproved = preauthOrClaimList.some(
    (entry: any) => entry.type === 'claim' && entry.status === 'Approved'
  );

  return (
    <>
      {!loading ? (
        <div className="-pt-2">
          <div className="relative flex pb-5">
            <ArrowPathIcon
              onClick={() => {
                getActivePlans();
              }}
              className={
                loading ? "animate-spin h-7 w-7 absolute right-0 " : "h-7 w-7 absolute right-0 "
              }
              aria-hidden="true"
            />
            {loading ? 'Please wait...' : ''}
          </div>
          <div className="mt-4 flex items-center justify-between">
            <h2 className="sm:text-title-xl1 text-2xl font-semibold text-black dark:text-white">
              {strings.CLAIM_REQUEST_DETAILS}
            </h2>
            <h2 className="sm:text-title-xl1 text-end font-semibold text-success dark:text-success">
              {coverageEligibilityStatus === 'Approved' ? (
                <div className="text-success">&#10004; Eligible</div>
              ) : (
                <div className="mr-3 text-warning ">Pending</div>
              )}
            </h2>
          </div>
          <span className="text-base font-medium">
            {requestDetails.workflowId || ''}
          </span>

          <div className="mt-4 rounded-lg border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
            <div className="items-center justify-between"></div>
            <div>
              {_.map(claimRequestDetails, (ele: any) => {
                return (
                  <div className="mb-2">
                    <h2 className="text-bold text-base font-bold text-black dark:text-white">
                      {ele.key}
                    </h2>
                    <span className="text-base font-medium">{ele.value}</span>
                  </div>
                );
              })}
            </div>
          </div>
          {_.map(preauthOrClaimList, (ele: any) => {
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
                    <div className="sm:text-title-xl1 mb-1 text-end font-semibold text-warning dark:text-warning">
                      Pending
                    </div>
                  )}
                </div>
                <div className="mt-4 rounded-lg border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
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
                          INR {ele.billAmount}
                        </span>
                      </div>
                    </div>
                  </div>
                  {ele.supportingDocuments.length === 0 ? null : <>
                    <h2 className="text-bold mb-3 text-base font-bold text-black dark:text-white">
                      Supporting documents :
                    </h2>
                    <div className="flex flex-wrap gap-2">
                      {_.map(ele.supportingDocuments, (ele: string) => {
                        const parts = ele.split('/');
                        const fileName = parts[parts.length - 1];
                        return (
                          <>
                            <a
                              href={ele}
                              download
                              className="flex flex-col w-50 shadow-sm border border-gray-300 hover:border-gray-400 rounded-md px-3 py-1 font-medium text-gray-700 hover:text-black"
                            >
                              <span className="text-center">{fileName}</span>
                              <img src={ele} alt="" />
                              <div className="flex items-center justify-center">
                                <ArrowDownTrayIcon className="h-5 w-5 flex-shrink-0 mr-2 text-indigo-400" />
                                <span>Download</span>
                              </div>
                            </a>
                          </>
                        );
                      })}
                    </div>
                  </>}
                </div>
              </>
            );
          })}

          <div>
            {preauthOrClaimList.length === 0 && (
              <>
                <div className="flex gap-3">
                  <button
                    onClick={() => navigate("/initiate-preauth-request", {
                      state: requestDetails,
                    })}
                    className="align-center mt-4 flex w-full justify-center rounded bg-lightBlue py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
                  >
                    Initiate pre-auth request
                  </button>
                  <button
                    onClick={() => navigate("/initiate-claim-request", {
                      state: requestDetails,
                    })}
                    className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
                  >
                    Initiate new claim request
                  </button>
                </div>
              </>
            )}

            {type.includes('claim') ? (
              <></>
            ) : type.includes('preauth') ? (
              <>
                <div className="mt-2 mb-2 flex items-center">
                  <button
                    onClick={() => navigate("/initiate-claim-request", {
                      state: requestDetails,
                    })}
                    className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
                  >
                    Initiate new claim request
                  </button>
                </div>
              </>
            ) : null}
          </div>

          {type.includes('preauth') && type.includes('claim') && !hasClaimApproved ? (
            <div className="mb-5 mt-5">
              <button
                disabled={false}
                onClick={() => {
                  navigate('/view-active-request', {
                    state: requestDetails,
                  });
                }}
                className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
              >
                {strings.PROCEED}
              </button>
            </div>
          ) : hasClaimApproved ? (
            <button
              onClick={() => navigate('/home')}
              className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
            >
              Home
            </button>
          ) : <></>}
        </div>
      ) : (
        <TransparentLoader />
      )}
    </>
  );
};

export default CoverageEligibility;
