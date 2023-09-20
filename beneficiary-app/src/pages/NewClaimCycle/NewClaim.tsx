import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { generateToken, hcxPostRequest } from '../../services/hcxService';
import LoadingButton from '../../components/LoadingButton';

const NewClaim = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [insurancePlanInputRef, setInsurancePlanInputRef] =
    useState<string>('');
  const [treatmentInputRef, setTreatmentInputRef] = useState<string>('');
  const [providerName, setProviderName] = useState<string>('');
  const [participantCode, setParticipantCode] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  const [payor, setPayor] = useState<string>('');
  const [insuranceId, setInsuranceId] = useState<string>('');

  const [token, setToken] = useState<string>('');
  const [searchResults, setSearchResults] = useState<any>();

  const [openDropdown, setOpenDropdown] = useState(false);

  const payload = {
    filters: {
      participant_name: { eq: providerName },
    },
  };

  const tokenRequestBody = {
    username: 'hcxgateway@swasth.org',
    password: 'Swasth@12345',
  };

  const config = {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };

  useEffect(() => {
    let searchParticipant = async () => {
      try {
        const tokenResponse = await generateToken(tokenRequestBody);
        setToken(tokenResponse.data.access_token);
        const response = await hcxPostRequest(
          'participant/search',
          payload,
          config
        );
        setOpenDropdown(true);
        setSearchResults(response.data?.participants);
      } catch (error) {
        setOpenDropdown(false);
        console.error('Error fetching data:', error);
      }
    };
    searchParticipant();
  }, [providerName]);

  const handleSelect = (result: any, participantCode: any) => {
    setOpenDropdown(false);
    setParticipantCode(participantCode);
    setProviderName(result);
  };

  // const providerDetails = {
  //   name: location.state?.name,
  //   id: location.state?.id,
  // };

  const sendProviderDetails = {
    name: providerName,
    id: participantCode,
  };

  const initiateClaimRequestBody = {
    providerName: providerName,
    participantCode: participantCode,
    serviceType: treatmentInputRef,
    insurancePlan: insurancePlanInputRef,
    payor: payor,
    insuranceId: insuranceId,
    mobile: location.state,
  };

  // console.log(coverageEligibilityRequestPayload);

  // const sendCoverageEligibilityRequest = async () => {
  //   try {
  //     setIsLoading(true);
  //     let response = await protocolApiPostRequest(
  //       'coverageeligibility/check',
  //       coverageEligibilityRequestPayload
  //     );
  //     setIsLoading(false);
  //     if (response?.status === 202) {
  //       navigate('/request-success');
  //     }
  //   } catch (error) {
  //     setIsLoading(false);
  //     console.log(error);
  //   }
  // };
  return (
    <div className="w-full pt-2 sm:p-12.5 xl:p-1 ">
      <h2 className="mb-4 -mt-4 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
        Provide details to initiate claim request
      </h2>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div>
          <h2 className="text-bold text-base font-bold text-black dark:text-white">
            Provider name:{' '}
            <div className="relative">
              <input
                type="text"
                placeholder="Search..."
                value={providerName}
                onChange={(e) => setProviderName(e.target.value)}
                className="mt-2 w-full rounded-lg border-[1.5px] border-stroke bg-white py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
              />
              <span
                className="absolute top-8 right-4 z-30 -translate-y-1/2"
                onClick={() => {
                  setOpenDropdown(!openDropdown);
                }}
              >
                <svg
                  className="fill-current"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <g opacity="0.8">
                    <path
                      fillRule="evenodd"
                      clipRule="evenodd"
                      d="M5.29289 8.29289C5.68342 7.90237 6.31658 7.90237 6.70711 8.29289L12 13.5858L17.2929 8.29289C17.6834 7.90237 18.3166 7.90237 18.7071 8.29289C19.0976 8.68342 19.0976 9.31658 18.7071 9.70711L12.7071 15.7071C12.3166 16.0976 11.6834 16.0976 11.2929 15.7071L5.29289 9.70711C4.90237 9.31658 4.90237 8.68342 5.29289 8.29289Z"
                      fill=""
                    ></path>
                  </g>
                </svg>
              </span>
              {openDropdown && searchResults.length !== 0 ? (
                <div className="max-h-40 overflow-y-auto">
                  <ul className="border-gray-300 left-0 w-full rounded-lg bg-white px-2 text-black">
                    {searchResults.map((result: any, index: any) => (
                      <li
                        key={index}
                        onClick={() =>
                          handleSelect(
                            result?.participant_name,
                            result?.participant_code
                          )
                        }
                        className="hover:bg-gray-200 cursor-pointer p-2"
                      >
                        {result?.participant_name || ''}
                      </li>
                    ))}
                  </ul>
                </div>
              ) : (
                <></>
              )}
            </div>
          </h2>
          <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
            Participant code:{' '}
            <div>
              <input
                onChange={(e) => {
                  setParticipantCode(e.target.value);
                }}
                value={providerName ? participantCode : ''}
                disabled
                type="text"
                placeholder="Participant code"
                className="mt-2 w-full rounded-lg border-[1.5px] border-stroke bg-white py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
              />
            </div>
          </h2>
        </div>
        <div className="border-gray-300 my-4 border-t "></div>
        <div className="mt-4">
          <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
            Treatment/Service type: *
          </label>
          <div className="relative z-20 bg-white dark:bg-form-input">
            <select
              onChange={(e) => {
                setTreatmentInputRef(e.target.value);
              }}
              required
              className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark"
            >
              <option value="">select</option>
              <option value="OPD">OPD</option>
            </select>
            <span className="absolute top-1/2 right-4 z-10 -translate-y-1/2">
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <g opacity="0.8">
                  <path
                    fillRule="evenodd"
                    clipRule="evenodd"
                    d="M5.29289 8.29289C5.68342 7.90237 6.31658 7.90237 6.70711 8.29289L12 13.5858L17.2929 8.29289C17.6834 7.90237 18.3166 7.90237 18.7071 8.29289C19.0976 8.68342 19.0976 9.31658 18.7071 9.70711L12.7071 15.7071C12.3166 16.0976 11.6834 16.0976 11.2929 15.7071L5.29289 9.70711C4.90237 9.31658 4.90237 8.68342 5.29289 8.29289Z"
                    fill="#637381"
                  ></path>
                </g>
              </svg>
            </span>
          </div>
        </div>
        <div className="mt-4">
          <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
            Select insurance plan: *
          </label>
          <div className="relative z-20 bg-white dark:bg-form-input">
            <select
              onChange={(e) => {
                setInsurancePlanInputRef(e.target.value);
              }}
              required
              className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark"
            >
              <option value="">select</option>
              <option value="none">none</option>
              <option value="ABC123">ABC123</option>
            </select>
            <span className="absolute top-1/2 right-4 z-10 -translate-y-1/2">
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <g opacity="0.8">
                  <path
                    fillRule="evenodd"
                    clipRule="evenodd"
                    d="M5.29289 8.29289C5.68342 7.90237 6.31658 7.90237 6.70711 8.29289L12 13.5858L17.2929 8.29289C17.6834 7.90237 18.3166 7.90237 18.7071 8.29289C19.0976 8.68342 19.0976 9.31658 18.7071 9.70711L12.7071 15.7071C12.3166 16.0976 11.6834 16.0976 11.2929 15.7071L5.29289 9.70711C4.90237 9.31658 4.90237 8.68342 5.29289 8.29289Z"
                    fill="#637381"
                  ></path>
                </g>
              </svg>
            </span>
          </div>
          {insurancePlanInputRef === 'none' ? (
            <>
              <h2 className="sm:text-title-xl1 mt-4 mb-2 text-2xl font-bold text-black dark:text-white">
                Insurance details
              </h2>
              <div className="flex flex-col gap-5.5 py-4">
                <div>
                  <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                    Payor Details: *
                  </label>
                  <div className="relative z-20 bg-white dark:bg-form-input">
                    <select
                      onChange={(e: any) => {
                        setPayor(e.target.value);
                      }}
                      required
                      className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark"
                    >
                      <option value="">select</option>
                      <option value="Swasth Payer">Swasth Payer</option>
                    </select>
                    <span className="absolute top-1/2 right-4 z-10 -translate-y-1/2">
                      <svg
                        width="24"
                        height="24"
                        viewBox="0 0 24 24"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <g opacity="0.8">
                          <path
                            fillRule="evenodd"
                            clipRule="evenodd"
                            d="M5.29289 8.29289C5.68342 7.90237 6.31658 7.90237 6.70711 8.29289L12 13.5858L17.2929 8.29289C17.6834 7.90237 18.3166 7.90237 18.7071 8.29289C19.0976 8.68342 19.0976 9.31658 18.7071 9.70711L12.7071 15.7071C12.3166 16.0976 11.6834 16.0976 11.2929 15.7071L5.29289 9.70711C4.90237 9.31658 4.90237 8.68342 5.29289 8.29289Z"
                            fill="#637381"
                          ></path>
                        </g>
                      </svg>
                    </span>
                  </div>
                </div>
                <div>
                  <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                    Insurance ID *
                  </label>
                  <div className="relative">
                    <input
                      onChange={(e: any) => {
                        setInsuranceId(e.target.value);
                      }}
                      required
                      type="text"
                      placeholder="Insurance ID"
                      className={
                        'w-full rounded-lg border border-stroke py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                      }
                    />
                  </div>
                </div>
              </div>
            </>
          ) : (
            <>
              {/* <p className="mt-3 text-center">OR</p>
              <div className="mt-3 text-center">
                <a
                  className="cursor-pointer underline"
                  onClick={() => {
                    navigate('/coverage-eligibility-request', {
                      state:
                        providerDetails.id || providerDetails.name !== undefined
                          ? providerDetails
                          : sendProviderDetails,
                    });
                  }}
                >
                  + Add health plan
                </a>
              </div> */}
            </>
          )}
        </div>
      </div>
      <div className="mb-5 mt-5">
        {!isLoading ? (
          <button
            disabled={
              insurancePlanInputRef === '' ||
              treatmentInputRef === '' ||
              providerName === '' ||
              (insurancePlanInputRef === 'none' &&
                (insuranceId === '' || payor === ''))
            }
            onClick={(event: any) => {
              event.preventDefault();
              navigate('/initiate-claim-request', {
                state: { initiateClaimRequestBody },
              });
              // sendCoverageEligibilityRequest();
            }}
            className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
          >
            Initiate claim request
          </button>
        ) : (
          <LoadingButton />
        )}
      </div>
    </div>
  );
};

export default NewClaim;
