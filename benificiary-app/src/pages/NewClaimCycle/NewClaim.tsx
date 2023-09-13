import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

const NewClaim = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [insurancePlanInputRef, setInsurancePlanInputRef] = useState<any>();
  const [treatmentInputRef, setTreatmentInputRef] = useState<any>();

  const providerDetails = {
    name: location.state?.name,
    id: location.state?.id,
  };

  const handleInsurancePlanChange = (e: any) => {
    setInsurancePlanInputRef(e.target.value);
  };

  const handleServiceTypeChange = (e: any) => {
    setTreatmentInputRef(e.target.value);
  };

  console.log(insurancePlanInputRef);
  console.log(treatmentInputRef);

  return (
    <div className="w-full pt-2 sm:p-12.5 xl:p-1">
      <h2 className="mb-4 -mt-4 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
        New claim cycle
      </h2>
      <h2 className="sm:text-title-xl1 mb-4 text-2xl font-semibold text-black dark:text-white">
        Provider details
      </h2>
      <div>
        <h2 className="text-bold text-base font-bold text-black dark:text-white">
          Provider name:{' '}
          {providerDetails.id || providerDetails.name ? (
            providerDetails?.name
          ) : (
            <div>
              <input
                type="text"
                placeholder="Enter provider name"
                className="mt-2 w-full rounded-lg border-[1.5px] border-stroke bg-white py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
              />
            </div>
          )}
        </h2>
        <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
          HCX ID:{' '}
          {providerDetails.id || providerDetails.name ? (
            providerDetails?.id
          ) : (
            <div>
              <input
                type="text"
                placeholder="Enter HCX id"
                className="mt-2 w-full rounded-lg border-[1.5px] border-stroke bg-white py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
              />
            </div>
          )}
        </h2>
      </div>
      <div className="border-gray-300 my-4 border-t "></div>
      <div className="mt-4">
        <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
          Treatment/Service type: *
        </label>
        <div className="relative z-20 bg-white dark:bg-form-input">
          <select
            onChange={handleServiceTypeChange}
            required
            className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark"
          >
            <option value="OPD"></option>
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
            onChange={handleInsurancePlanChange}
            required
            className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark"
          >
            <option value="none">None</option>
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
        <p className="mt-3 text-center">OR</p>
        <div className="mt-3 text-center">
          <a
            className="cursor-pointer underline"
            onClick={() => {
              if (treatmentInputRef)
                navigate('/coverage-eligibility-request', {
                  state: treatmentInputRef,
                });
            }}
          >
            + Add health plan
          </a>
        </div>
        <div className="mb-5 mt-5">
          <button
            disabled={
              insurancePlanInputRef === undefined ||
              insurancePlanInputRef === 'none'
            }
            onClick={(event: any) => {
              event.preventDefault();
              navigate('/coverage-eligibility');
            }}
            className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
          >
            Check coverage eligibility
          </button>
        </div>
      </div>
    </div>
  );
};

export default NewClaim;
