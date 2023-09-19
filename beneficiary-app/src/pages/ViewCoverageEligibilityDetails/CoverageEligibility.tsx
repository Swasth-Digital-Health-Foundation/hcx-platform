import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const CoverageEligibility = () => {
  const navigate = useNavigate();
  const [selectedValue, setSelectedValue] = useState<string>(''); // Initialize with an empty string or the default value you want

  const handleRadioChange = (event: any) => {
    setSelectedValue(event.target.value);
  };

  console.log(selectedValue);
  return (
    <div className="-pt-2 w-full sm:p-12.5 xl:p-1">
      <h2 className="sm:text-title-xl1 mb-1 text-end font-semibold text-success dark:text-success">
        Eligible
      </h2>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex items-center justify-between">
          <h2 className="sm:text-title-xl1 mb-4 text-2xl font-semibold text-black dark:text-white">
            Claim request details : ID
          </h2>
        </div>
        <div>
          <h2 className="text-bold text-base font-bold text-black dark:text-white">
            Provider name :
          </h2>
          <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
            Participant code :
          </h2>
          <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
            Select insurance plan :
          </h2>
          <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
            Treatment/Service type :
          </h2>
          <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
            Payor name :
          </h2>
          <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
            Insurance ID :
          </h2>
        </div>
      </div>
      <div className="border-gray-300 my-4 border-t "></div>

      <div className="mt-4">
        <h2 className="text-bold text-1xl mt-1 font-bold text-black dark:text-white">
          Next step :
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
            Initiate new claim request
          </label>
        </div>
        <div className="mb-3 flex items-center">
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
            Initiate pre-auth request
          </label>
        </div>
        <div className="mb-5 mt-5">
          <button
            disabled={selectedValue === ''}
            onClick={(event: any) => {
              event.preventDefault();
              if (selectedValue === 'Initiate new claim request') {
                navigate('/initiate-claim-request');
              } else {
                navigate('/initiate-preauth-request');
              }
            }}
            className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
          >
            Proceed
          </button>
        </div>
      </div>
    </div>
  );
};

export default CoverageEligibility;
