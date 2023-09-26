import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import strings from '../../utils/strings';
import { generateToken, searchParticipant } from '../../services/hcxService';
import axios from 'axios';
import {
  createCommunicationOnRequest,
  isInitiated,
} from '../../services/hcxMockService';
import { toast } from 'react-toastify';

const ViewClaimRequestDetails = () => {
  const location = useLocation();
  const details = location.state;
  const navigate = useNavigate();

  const [token, setToken] = useState<string>('');

  const [providerName, setProviderName] = useState<string>('');
  const [payorName, setPayorName] = useState<string>('');

  const [initiated, setInitiated] = useState(false);

  const [OTP, setOTP] = useState<any>();

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

  const sendInfo = {
    ...details,
    payor: payorName,
    providerName: providerName,
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

  const claimRequestDetails: any = [
    {
      key: 'Provider name :',
      value: providerName || '',
    },
    {
      key: 'Participant code :',
      value: details?.participantCode || '',
    },
    {
      key: 'Treatment/Service type :',
      value: details?.serviceType || '',
    },
    {
      key: 'Payor name :',
      value: payorName || '',
    },
    {
      key: 'Insurance ID :',
      value: details?.insuranceId || '',
    },
  ];

  const treatmentDetails = [
    {
      key: 'Service type :',
      value: details?.serviceType || '',
    },
    {
      key: 'Bill amount :',
      value: details?.billAmount || '',
    },
  ];

  const supportingDocuments = [
    {
      key: 'Document type :',
      value: details?.billingDeatils?.serviceType || '',
    },
  ];

  const getVerificationPayload = {
    type: 'otp_verification',
    request_id: details?.apiCallId,
  };

  console.log(getVerificationPayload);

  const getVerification = async () => {
    try {
      let res = await isInitiated(getVerificationPayload);
      console.log(res);
      if (res.status === 200) {
        setInitiated(true);
      }
    } catch (err) {
      console.log(err);
      toast.error('Communication is not initiated.');
    }
  };

  const payload = {
    request_id: details?.apiCallId,
    mobile: localStorage.getItem('mobile'),
    otp_code: OTP,
    type: 'otp',
  };

  const verifyOTP = async () => {
    try {
      const res = await createCommunicationOnRequest(payload);
      console.log(res);
      setInitiated(false);
      toast.success(res.data?.message);
      navigate('/bank-details', { state: sendInfo });
    } catch (err) {
      toast.error('Enter valid OTP!');
      console.log(err);
    }
  };

  


  return (
    <>
      <div className="flex items-center justify-between">
        <h2 className="sm:text-title-xl1 mb-4 text-2xl font-semibold text-black dark:text-white">
          {strings.CLAIM_REQUEST_DETAILS}
        </h2>
      </div>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
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
      <div className="flex items-center justify-between">
        <h2 className="sm:text-title-xl1 text-1xl mt-2 mb-4 font-semibold text-black dark:text-white">
          {strings.TREATMENT_AND_BILLING_DETAILS}
        </h2>
      </div>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div>
          {treatmentDetails.map((ele: any) => {
            return (
              <div className="flex gap-2">
                <h2 className="text-bold text-base font-bold text-black dark:text-white">
                  {ele.key}
                </h2>
                <span className="text-base font-medium">{ele.value}</span>
              </div>
            );
          })}
        </div>
      </div>
      <div className="flex items-center justify-between">
        <h2 className="sm:text-title-xl1 text-1xl mt-2 mb-4 font-semibold text-black dark:text-white">
          {strings.SUPPORTING_DOCS}
        </h2>
      </div>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div>
          {supportingDocuments.map((ele: any) => {
            return (
              <>
                <h2 className="text-bold text-base font-bold text-black dark:text-white">
                  {ele.key}
                </h2>
                <span className="text-base font-medium">{ele.value}</span>
              </>
            );
          })}
        </div>
      </div>

      {!initiated ? (
        <div
          onClick={() => getVerification()}
          className="align-center flex w-20 justify-center rounded bg-primary py-1 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
        >
          <span>Refresh</span>
        </div>
      ) : null}

      {initiated ? (
        <>
          <div className="flex items-center justify-between">
            <h2 className="sm:text-title-xl1 text-1xl mt-2 mb-4 font-semibold text-black dark:text-white">
              {strings.NEXT_STEP}
            </h2>
          </div>
          <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
            <div>
              <h2 className="text-bold text-base font-bold text-black dark:text-white">
                {strings.POLICYHOLDER_CONSENT}
              </h2>
              <label className="font-small mb-2.5 block text-left text-black dark:text-white">
                {strings.ENTER_OTP_TO_VERIFY_CLAIM}
              </label>
            </div>
            <div>
              <div className="relative">
                <input
                  required
                  onChange={(e: any) => {
                    setOTP(e.target.value);
                  }}
                  type="number"
                  placeholder="OTP"
                  className={
                    'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                  }
                />
              </div>
            </div>
            <div className="mb-5">
              <button
                onClick={(event: any) => {
                  event.preventDefault();
                  verifyOTP();
                }}
                type="submit"
                className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray"
              >
                {strings.VERIFY_OTP_BUTTON}
              </button>
            </div>
          </div>
        </>
      ) : null}
    </>
  );
};

export default ViewClaimRequestDetails;
