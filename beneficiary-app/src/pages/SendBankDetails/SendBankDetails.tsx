import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import strings from '../../utils/strings';
import {
  createCommunicationOnRequest,
  isInitiated,
} from '../../services/hcxMockService';
import { toast } from 'react-toastify';

const SendBankDetails = () => {
  const location = useLocation();
  const details = location.state;
  const navigate = useNavigate();

  const [beneficiaryName, setbeneficiaryName] = useState<string>('');
  const [accountNumber, setAccountNumber] = useState<string>('');
  const [ifscCode, setIfsc] = useState<string>('');

  const claimRequestDetails: any = [
    {
      key: 'Provider name :',
      value: details?.providerName || '',
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
      value: details?.payor || '',
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

  //   const supportingDocuments = [
  //     {
  //       key: 'Document type :',
  //       value: details?.billingDeatils?.serviceType || '',
  //     },
  //   ];

  const [bankDetails, setBankDetails] = useState(false);

  const getVerificationPayloadForBank = {
    type: 'bank_details',
    request_id: details?.apiCallId,
  };

  const getVerificationForBank = async () => {
    try {
      let res = await isInitiated(getVerificationPayloadForBank);
      console.log(res);
      if (res.status === 200) {
        setBankDetails(true);
        // toast.success('succes');
      }
    } catch (err) {
      toast.error('Communication is not initiated!');
      console.log(err);
    }
  };

  const bankDetailsPayload = {
    request_id: details?.apiCallId,
    type: 'bank_details',
    account_number: accountNumber,
    ifsc_code: ifscCode,
  };

  const submit = async () => {
    try {
      let res = await createCommunicationOnRequest(bankDetailsPayload);
      console.log(res);
      if (res.status === 202) {
        toast.success('Bank deatils submitted successfully!');
        navigate('/success');
      }
    } catch (err) {
      console.log(err);
    }
  };

  return (
    <div>
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
      <div className="mt-2 rounded-sm border border-stroke bg-white px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex items-center justify-between">
          <h2 className="sm:text-title-xl1 text-1xl mt-2 mb-4 font-semibold text-black dark:text-white">
            {strings.TREATMENT_AND_BILLING_DETAILS}
          </h2>
        </div>
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
      {/* <div className="mt-2 rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex items-center justify-between">
          <h2 className="sm:text-title-xl1 text-1xl  mb-4 font-semibold text-black dark:text-white">
            {strings.SUPPORTING_DOCS}
          </h2>
        </div>
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
      </div> */}
      <button
        className="align-center mt-3 mb-3 flex w-20 justify-center rounded bg-primary py-1 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
        onClick={() => getVerificationForBank()}
      >
        Refresh
      </button>

      {bankDetails ? (
        <>
          <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
            <h2 className="text-bold text-base font-bold text-black dark:text-white">
              Beneficiary account details :
            </h2>
            <p className="mt-2">
              Please enter beneficiary bank account details.
            </p>{' '}
            <label className="font-small mt-3 mb-2.5 block text-left text-black dark:text-white">
              Beneficiary Name
            </label>
            <div className="relative">
              <input
                required
                onChange={(e: any) => {
                  setbeneficiaryName(e.target.value);
                }}
                type="text"
                placeholder="Enter beneficiary name"
                className={
                  'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                }
              />
            </div>
            <label className="font-small mt-3 mb-2.5 block text-left text-black dark:text-white">
              Bank account no.
            </label>
            <div className="relative">
              <input
                required
                onChange={(e: any) => {
                  setAccountNumber(e.target.value);
                }}
                type="text"
                placeholder="Enter account no."
                className={
                  'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                }
              />
            </div>
            <label className="font-small mt-3 mb-2.5 block text-left text-black dark:text-white">
              IFSC code
            </label>
            <div className="relative">
              <input
                required
                onChange={(e: any) => {
                  setIfsc(e.target.value);
                }}
                type="text"
                placeholder="Enter IFSC code"
                className={
                  'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                }
              />
            </div>
            <div className="mt-3">
              <button
                disabled={accountNumber === '' || ifscCode === ''}
                onClick={(event: any) => {
                  event.preventDefault();
                  //   navigate('/home');
                  //   verifyOTP();
                  submit();
                }}
                type="submit"
                className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
              >
                Submit
              </button>
            </div>
          </div>
        </>
      ) : (
        <></>
      )}
    </div>
  );
};

export default SendBankDetails;
