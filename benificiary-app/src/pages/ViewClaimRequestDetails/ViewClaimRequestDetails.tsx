import React from 'react';

const ViewClaimRequestDetails = () => {
  return (
    <div className="w-full pt-2 sm:p-12.5 xl:p-1">
      <h2 className="mb-4 -mt-4 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
        Claim details : Claim ID
      </h2>
      <div className="border-gray-300 my-4 border-t "></div>
      {/* <div className="flex items-center justify-between"> */}
      <h2 className="sm:text-title-xl1 mb-4 text-right font-semibold text-success dark:text-success">
        pending verification
      </h2>
      {/* </div> */}
      <div>
        <h2 className="text-base font-bold text-black dark:text-white">
          Provider name :
        </h2>
        <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
          HCX ID :
        </h2>
        <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
          Insurance plan :
        </h2>
        <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
          Treatment/Service type :
        </h2>
        <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
          Diagnosis details :
        </h2>
        <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
          Billing details :
        </h2>
      </div>
      <div className="border-gray-300 my-4 border-t "></div>
      <div>
        <h2 className="mb-4 mt-4 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
          Action needed :
        </h2>
        <h2 className="text-1xl sm:text-title-base mb-4 mt-4 font-bold text-black dark:text-white">
          Policyholder consent : *
        </h2>
        <div>
          <label className="font-small mb-2.5 block text-left text-black dark:text-white">
            Please enter OTP shared by payor to verify claim :
          </label>
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
            Verify
          </button>
        </div>
      </div>
    </div>
  );
};

export default ViewClaimRequestDetails;
