import { useLocation, useNavigate } from 'react-router-dom';
import { generateOutgoingRequest } from '../../services/hcxMockService';
import { useEffect, useState } from 'react';
import LoadingButton from '../../components/LoadingButton';
import { toast } from 'react-toastify';
import { postRequest } from '../../services/registryService';
import * as _ from 'lodash';
import strings from '../../utils/strings';

const CoverageEligibilityRequest = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [serviceType, setServiceType] = useState<string>('');
  const [insurancePlan, setInsurancePlan] = useState<string>('');
  const [payor, setPayor] = useState<string>('');
  const [insuranceId, setInsuranceId] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [patientName, setPatientName] = useState<string>('');

  const providerDetails = {
    providerName: location?.state?.obj?.name,
    participantCode: location?.state?.obj?.id,
  };

  let payload: any;

  if (insurancePlan === 'none') {
    payload = {
      patientName: patientName,
      mobile: _.get(location, 'state.filters.filters.mobile.eq') || '',
      serviceType: serviceType,
      insurancePlan: insurancePlan,
      payor: payor,
      insuranceId: insuranceId,
      ...providerDetails,
    };
  } else {
    payload = {
      patientName: patientName,
      mobile: _.get(location, 'state.filters.filters.mobile.eq') || '',
      serviceType: serviceType,
      insurancePlan: insurancePlan,
      ...providerDetails,
    };
  }

  const filter = {
    entityType: ['Beneficiary'],
    filters: {
      mobile: { eq: payload?.mobile },
    },
  };

  console.log(location.state);
  // console.log(_.get(location, 'state.filter.filters.mobile.eq'))

  //beneficiary search
  useEffect(() => {
    const getPatientName = async () => {
      try {
        let response = await postRequest('/search', filter);
        console.log(response);
        setPatientName(response.data[0]?.name);
      } catch (error) {
        console.log(error);
        toast.error(_.get(error, 'response.data.error.message'));
      }
    };
    getPatientName();
  }, []);

  const sendCoverageEligibilityRequest = async () => {
    try {
      setIsLoading(true);
      let response = await generateOutgoingRequest(
        'create/coverageeligibility/check',
        payload
      );
      console.log(response);
      setIsLoading(false);
      if (response?.status === 202) {
        navigate('/request-success', {
          state: {
            text: 'the coverage eligibility',
            mobileNumber: _.get(location, 'state.mobile'),
          },
        });
      }
    } catch (error) {
      setIsLoading(false);
      toast.error(_.get(error, 'response.data.error.message'));
      console.log(error);
    }
  };

  return (
    <div className="w-full pt-2 sm:p-12.5 xl:p-1">
      <h2 className="mb-4 -mt-4 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
        {strings.COVERAGE_ELIGIBILITY_REQUEST_HEADING}
      </h2>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div>
          <h2 className="text-bold text-base font-bold text-black dark:text-white">
            {strings.PROVIDER_NAME}{' '}
            {providerDetails.providerName !== undefined
              ? providerDetails.providerName
              : ''}
          </h2>
          <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
            {strings.PARTICIPANT_CODE}{' '}
            {providerDetails.participantCode !== undefined
              ? providerDetails.participantCode
              : ''}
          </h2>
        </div>
        <div className="border-gray-300 my-4 border-t "></div>
        <div className="mt-4">
          <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
            {strings.SERVICE_TYPE}
          </label>
          <div className="relative z-20 bg-white dark:bg-form-input">
            <select
              onChange={(e) => {
                setServiceType(e.target.value);
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
          <div className="mt-4">
            <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
              {strings.SELECT_INSURANCE_PLAN}
            </label>
            <div className="relative z-20 bg-white dark:bg-form-input">
              <select
                onChange={(e) => {
                  setInsurancePlan(e.target.value);
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
          </div>
          {insurancePlan === 'none' ? (
            <>
              <h2 className="sm:text-title-xl1 mt-4 mb-2 text-2xl font-bold text-black dark:text-white">
                {strings.INSURANCE_DETAILS}
              </h2>
              <div className="flex flex-col gap-5.5 py-4">
                <div>
                  <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                    {strings.PAYOR_DETAILS}
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
                    {strings.INSURANCE_ID}
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
          ) : null}
        </div>
      </div>
      <div className="mb-5 mt-5">
        {!isLoading ? (
          <button
            disabled={
              serviceType === '' ||
              insurancePlan === '' ||
              (insurancePlan === 'none' && (payor === '' || insuranceId === ''))
            }
            onClick={(event: any) => {
              event.preventDefault();
              sendCoverageEligibilityRequest();
            }}
            className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
          >
            {strings.INITIATE_COVERAGE_ELIGIBILITY}
          </button>
        ) : (
          <LoadingButton />
        )}
      </div>
    </div>
  );
};

export default CoverageEligibilityRequest;
