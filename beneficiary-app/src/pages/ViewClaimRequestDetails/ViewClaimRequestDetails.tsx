import React from 'react';

const ViewClaimRequestDetails = () => {
  return (
    <>
      <div className="flex items-center justify-between">
        <h2 className="sm:text-title-xl1 mb-4 text-2xl font-semibold text-black dark:text-white">
          Claim request details :
        </h2>
      </div>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
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
      <div className="flex items-center justify-between">
        <h2 className="sm:text-title-xl1 text-1xl mt-2 mb-4 font-semibold text-black dark:text-white">
          Treatment and billing details :
        </h2>
      </div>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div>
          <h2 className="text-bold text-base font-bold text-black dark:text-white">
            Service type :
          </h2>
          <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
            Bill amount :
          </h2>
        </div>
      </div>
      <div className="flex items-center justify-between">
        <h2 className="sm:text-title-xl1 text-1xl mt-2 mb-4 font-semibold text-black dark:text-white">
          Supporting documents :
        </h2>
      </div>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div>
          <h2 className="text-bold text-base font-bold text-black dark:text-white">
            Document type :
          </h2>
          <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
            Bill amount :
          </h2>
        </div>
      </div>
      <div className="flex items-center justify-between">
        <h2 className="sm:text-title-xl1 text-1xl mt-2 mb-4 font-semibold text-black dark:text-white">
          Next step :
        </h2>
      </div>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div>
          <h2 className="text-bold text-base font-bold text-black dark:text-white">
            Policyholder consent :
          </h2>
          <label className="font-small mb-2.5 block text-left text-black dark:text-white">
            Please enter OTP shared by payor to verify claim :
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
            Verify
          </button>
        </div>
      </div>
    </>
  );
};

export default ViewClaimRequestDetails;
