import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../store";
import _ from "lodash";
import { addAppData } from "../../reducers/app_data";


const LinkedUsers = () => {

const dispatch = useDispatch();    
const appData:Object =useSelector((state: RootState) => state.appDataReducer.appData);

useEffect(() => {
    console.log("reloaded app data", appData);
},[appData])    

const onClose = () => {
    dispatch(addAppData({"showLinkedUsers":false}));
}



return (
    <>
<div
  id="defaultModal"
  tabIndex={1}
  className={"fixed top-0 left-0 right-0 z-50 w-full p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full justify-center items-center flex " + (_.get(appData, "showLinkedUsers") ? "" : "hidden")}
  role="dialog"
>
  <div className="relative w-full max-w-2xl max-h-full">
    {/* Modal content */}
    <div className="relative bg-white rounded-lg shadow dark:bg-gray-700">
      {/* Modal header */}
      <div className="flex items-start justify-between p-4 border-b rounded-t dark:border-gray-600">
        <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
          LinkedUsers
        </h3>
        <button
          type="button"
          className="text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm p-1.5 ml-auto inline-flex items-center dark:hover:bg-gray-600 dark:hover:text-white"
          data-modal-hide="defaultModal"
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
      <div className="p-10 space-y-6">
      <table className="w-1/2 text-sm text-left text-gray-500 dark:text-gray-400">
    <thead className="text-xs text-gray-700 uppercase bg-gray-50 dark:bg-gray-700 dark:text-gray-400">
      <tr>
        <th scope="col" className="px-6 py-3">
          User Name
        </th>
        <th scope="col" className="px-6 py-3">
          User Code
        </th>
        <th scope="col" className="px-6 py-3">
          Created By
        </th>
        <th scope="col" className="px-6 py-3">
          Action
        </th>
      </tr>
    </thead>
    <tbody>
      <tr className="bg-white border-b dark:bg-gray-800 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600">
        <th
          scope="row"
          className="flex items-center px-6 py-4 text-gray-900 whitespace-nowrap dark:text-white"
        >
          <div className="pl-3">
            <div className="text-base font-semibold">User 1</div>
            <div className="font-normal text-gray-500">
              User1@Org1.com
            </div>
          </div>
        </th>
        <td className="px-6 py-4">user1-hcx@staging-swasth</td>
         <td className="px-6 py-4">admin-hcx@staging-swasth</td>

        <td className="px-6 py-4 space-x-3">
          {/* Modal toggle */}
          <a
            href="#"
            type="button"
            data-modal-target="editUserModal"
            data-modal-show="editUserModal"
            className="font-medium text-blue-600 dark:text-blue-500 hover:underline"
            //onClick={() => setShowDetails(true)}
          >
            Remove 
          </a>
        </td>
      </tr>
    </tbody>
  </table>
      </div>
      {/* Modal footer */}
      <div className="flex items-center p-6 space-x-2 border-t border-gray-200 rounded-b dark:border-gray-600">
        <button
          data-modal-hide="defaultModal"
          type="button"
          className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
          onClick={()=> onClose()}
        >
          Close
        </button>
      </div>
    </div>
  </div>
</div>
<div className={"fixed inset-0 bg-black bg-opacity-30 z-40 "  + (_.get(appData,"showLinkedUsers") ? "" : "hidden")} ></div>
</>

)
}

export default LinkedUsers;