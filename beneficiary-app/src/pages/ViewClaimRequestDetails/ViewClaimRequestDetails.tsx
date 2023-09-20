import React from 'react';
import { useLocation } from 'react-router-dom';
import strings from '../../utils/strings';

const ViewClaimRequestDetails = () => {
  const location = useLocation();
  const details = location.state.initiateClaimRequestBody;
  console.log(details);

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
      key: 'Select insurance plan :',
      value: details?.insurancePlan || '',
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
      value: details?.billingDeatils?.serviceType || '',
    },
    {
      key: 'Bill amount :',
      value: details?.billingDeatils?.billAmount || '',
    },
  ];

  const supportingDocuments = [
    {
      key: 'Document type :',
      value: details?.billingDeatils?.serviceType || '',
    },
    {
      key: 'Bill amount :',
      value: details?.billingDeatils?.billAmount || '',
    },
  ];
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
              //   navigate('/home');
            }}
            type="submit"
            className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray"
          >
            {strings.VERIFY_OTP_BUTTON}
          </button>
        </div>
      </div>
    </>
  );
};

export default ViewClaimRequestDetails;
