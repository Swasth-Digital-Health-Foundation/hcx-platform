import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import _ from "lodash";
import { RootState } from "../store";
import { addAppData } from "../reducers/app_data";


const TermsOfUse = () => {

const dispatch = useDispatch();    
const appData:Object =useSelector((state: RootState) => state.appDataReducer.appData);

useEffect(() => {
    console.log("reloaded app data", appData);
},[appData])    

const onAccept = () => {
    dispatch(addAppData({"showTerms":false, "termsAccepted":true}));
}

const onDecline = () => {
  dispatch(addAppData({"showTerms":false, "termsAccepted":false}));
}


return (
    <>
<div
  id="defaultModal"
  tabIndex={1}
  className={"fixed top-0 left-0 right-0 z-50 w-full p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full justify-center items-center flex " + (_.get(appData, "showTerms") ? "" : "hidden")}
  role="dialog"
>
  <div className="relative w-full max-w-2xl max-h-full">
    {/* Modal content */}
    <div className="relative bg-white rounded-lg shadow dark:bg-gray-700">
      {/* Modal header */}
      <div className="flex items-start justify-between p-4 border-b rounded-t dark:border-gray-600">
        <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
          Terms of Service
        </h3>
        <button
          type="button"
          className="text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm p-1.5 ml-auto inline-flex items-center dark:hover:bg-gray-600 dark:hover:text-white"
          data-modal-hide="defaultModal"
          onClick={()=> onDecline()}
        >
          <svg
            aria-hidden="true"
            className="w-5 h-5"
            fill="currentColor"
            viewBox="0 0 20 20"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              fillRule="evenodd"
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
              clipRule="evenodd"
            />
          </svg>
          <span className="sr-only">Close modal</span>
        </button>
      </div>
      {/* Modal body */}
      <div className="w-full h-[32rem] p-4">
      <embed src="https://dev-hcx.s3.ap-south-1.amazonaws.com/terms-and-conditions/1.0.pdf" className="w-full h-full" type="application/pdf"></embed>
      </div>
      {/* Modal footer */}
      <div className="flex items-center p-6 space-x-2 border-t border-gray-200 rounded-b dark:border-gray-600">
        <button
          data-modal-hide="defaultModal"
          type="button"
          className="text-gray-500 bg-white hover:bg-gray-100 focus:ring-4 focus:outline-none focus:ring-blue-300 rounded-lg border border-gray-200 text-sm font-medium px-5 py-2.5 hover:text-gray-900 focus:z-10 dark:bg-gray-700 dark:text-gray-300 dark:border-gray-500 dark:hover:text-white dark:hover:bg-gray-600 dark:focus:ring-gray-600"
          onClick={()=> onAccept()}
        >
          I accept
        </button>
        <button
          data-modal-hide="defaultModal"
          type="button"
          className="text-gray-500 bg-white hover:bg-gray-100 focus:ring-4 focus:outline-none focus:ring-blue-300 rounded-lg border border-gray-200 text-sm font-medium px-5 py-2.5 hover:text-gray-900 focus:z-10 dark:bg-gray-700 dark:text-gray-300 dark:border-gray-500 dark:hover:text-white dark:hover:bg-gray-600 dark:focus:ring-gray-600"
          onClick={()=> onDecline()}
        >
          Decline
        </button>
      </div>
    </div>
  </div>
</div>
<div className={"fixed inset-0 bg-black bg-opacity-30 z-40 "  + (_.get(appData,"showTerms") ? "" : "hidden")} ></div>
</>

)
}

export default TermsOfUse;