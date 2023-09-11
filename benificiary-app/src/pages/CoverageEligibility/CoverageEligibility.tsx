import React from 'react';
import { useNavigate } from 'react-router-dom';

const CoverageEligibility = () => {
  const navigate = useNavigate();
  return (
    <div className="w-full pt-2 sm:p-12.5 xl:p-1">
      <h2 className="mb-4 -mt-4 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
        Coverage eligibility details
      </h2>
      <div className="border-gray-300 my-4 border-t "></div>
      <div className="flex items-center justify-between">
        <h2 className="sm:text-title-xl1 mb-4 text-2xl font-semibold text-black dark:text-white">
          Provider details
        </h2>
        <h2 className="sm:text-title-xl1 mb-4 font-semibold text-success dark:text-success">
          Eligible
        </h2>
      </div>
      <div>
        <h2 className="text-base text-bold font-bold text-black dark:text-white">
          Provider name :
        </h2>
        <h2 className="text-base text-bold mt-1 font-bold text-black dark:text-white">
          HCX ID :
        </h2>
        <h2 className="text-base text-bold mt-1 font-bold text-black dark:text-white">
          Cashless enabled :
        </h2>
        <h2 className="text-base text-bold mt-1 font-bold text-black dark:text-white">
          Treatment/Service type :
        </h2>
        <h2 className="text-base text-bold mt-1 font-bold text-black dark:text-white">
          Insurance plan :
        </h2>
      </div>
      <div className="border-gray-300 my-4 border-t "></div>

      <div className="mt-4">
        <h2 className="text-bold mt-1 text-2xl font-bold text-black dark:text-white">
          Action needed :
        </h2>
        <div className="mb-5 mt-5">
          <button
            onClick={(event: any) => {
              event.preventDefault();
              navigate('/create-claim-request');
            }}
            className="align-center text-1xl mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray"
          >
            Initiate claim request
          </button>
        </div>
      </div>
    </div>
  );
};

export default CoverageEligibility;
