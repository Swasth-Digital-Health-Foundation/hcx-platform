import { navigate } from "raviger";
import React from "react";
import { addAppData } from "../../reducers/app_data";
import { useDispatch } from "react-redux";
import LinkedUsers from "./LinkedUsers";


const LinkedParticipant = () => {

  const dispatch = useDispatch();  
  
    return(
      <>
      <LinkedUsers></LinkedUsers>

        <div className="p-4 sm:ml-64">
        <div className="p-4 border-2 border-gray-200 border rounded-lg dark:border-gray-700 mt-14">
        <div className="w-full p-12">
        <div className="flex flex-wrap -mx-3 mb-6 border-b-2 shadow-l shadow-bottom justify-between">
          <label
            className="block uppercase tracking-wide text-gray-700 text-xs font-bold mb-2"
          >
            Participants linked to the User
          </label>
      </div> 
        <div className="relative overflow-x-auto shadow-md sm:rounded-lg">
            
  <table className="w-full text-sm text-left text-gray-500 dark:text-gray-400">
    <thead className="text-xs text-gray-700 uppercase bg-gray-50 dark:bg-gray-700 dark:text-gray-400">
      <tr>
        <th scope="col" className="px-6 py-3">
          Participant Code
        </th>
        <th scope="col" className="px-6 py-3">
          Organization
        </th>
        <th scope="col" className="px-6 py-3">
          Email ID
        </th>
        <th scope="col" className="px-6 py-3">
          Phone
        </th>
        <th scope="col" className="px-6 py-3">
          Status
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
          className="px-6 py-4 font-medium text-gray-900 whitespace-nowrap dark:text-white"
        >
          Swasth_mock_payer+swasth@hcx-staging
        </th>
        <td className="px-6 py-4">Swasth Mock Payer</td>
        <td className="px-6 py-4">Swasth_mock_payer@swasthalliance.org</td>
        <td className="px-6 py-4">9999999999</td>
        <td className="px-6 py-4">
        <div className="flex items-center">
          <div className="h-2.5 w-2.5 rounded-full bg-green-500 mr-2" />{" "} Active
          </div>
          </td>
        <td className="flex items-center px-6 py-4 space-x-3">
          <a
            href="#"
            className="font-medium text-blue-600 dark:text-blue-500 hover:underline"
            onClick={(event) => {event.preventDefault(); console.log("clicked "); navigate("/onboarding/dashboard")}}
          >
            View
          </a>
          <a
            href="#"
            className="font-medium text-blue-600 dark:text-blue-500 hover:underline"
            onClick={(event) => {event.preventDefault(); console.log("app data updatated");dispatch(addAppData({"showLinkedUsers":true}))}}
          >
            Linked Users
          </a>
        </td>
      </tr>
    </tbody>
  </table>
</div>
</div>
</div>
</div>
</>
    )
}

export default LinkedParticipant;