import React, { useState } from "react";
import 'flowbite';

const Users =() => {

    const [showDetails, setShowDetails] = useState(false);
    return(

        <div className="p-4 sm:ml-64">
        <div className="p-4 border-2 border-gray-200 border rounded-lg dark:border-gray-700 mt-14">
        <div className="w-full p-12">
        <div className="flex flex-wrap -mx-3 mb-6 border-b-2 shadow-l shadow-bottom justify-between">
          <label
            className="block uppercase tracking-wide text-gray-700 text-xs font-bold mb-2"
          >
            View and add users to the participant
          </label>
      </div>     
        <div className="relative overflow-x-auto shadow-md sm:rounded-lg">
  <div className="flex items-center justify-between py-4 bg-white dark:bg-gray-800">
    <div>
    <select id="payordropdown" 
                        className="inline-flex items-center text-gray-500 bg-white border border-gray-300 focus:outline-none hover:bg-gray-100 focus:ring-4 focus:ring-gray-200 font-medium rounded-lg text-sm px-3 py-1.5 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-600 dark:hover:bg-gray-700 dark:hover:border-gray-600 dark:focus:ring-gray-700" >
                        <option selected value="1">All</option>
                        <option value="2">Created by User</option>
                      </select>
     </div>                 
    <label htmlFor="table-search" className="sr-only">
      Search
    </label>
    <div className="relative">
      <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
        <svg
          className="w-5 h-5 text-gray-500 dark:text-gray-400"
          aria-hidden="true"
          fill="currentColor"
          viewBox="0 0 20 20"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            fillRule="evenodd"
            d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
            clipRule="evenodd"
          />
        </svg>
      </div>
      <input
        type="text"
        id="table-search-users"
        className="block p-2 pl-10 text-sm text-gray-900 border border-gray-300 rounded-lg w-80 bg-gray-50 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
        placeholder="Search for users"
      />
    </div>
  </div>
  <table className="w-full text-sm text-left text-gray-500 dark:text-gray-400">
    <thead className="text-xs text-gray-700 uppercase bg-gray-50 dark:bg-gray-700 dark:text-gray-400">
      <tr>
        <th scope="col" className="px-6 py-3">
          User Name
        </th>
        <th scope="col" className="px-6 py-3">
          User Code
        </th>
        <th scope="col" className="px-6 py-3">
          Mobile
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
        <td className="px-6 py-4">
          <div className="flex items-center">
            {/* <div className="h-2.5 w-2.5 rounded-full bg-green-500 mr-2" />{" "} */}
            9999999999
          </div>
        </td>
         <td className="px-6 py-4">admin-hcx@staging-swasth</td>

        <td className="px-6 py-4 space-x-3">
          {/* Modal toggle */}
          <a
            href="#"
            type="button"
            data-modal-target="editUserModal"
            data-modal-show="editUserModal"
            className="font-medium text-blue-600 dark:text-blue-500 hover:underline"
            onClick={() => setShowDetails(true)}
          >
            Add 
          </a>
          <a
            href="#"
            type="button"
            data-modal-target="editUserModal"
            data-modal-show="editUserModal"
            className="font-medium text-red-600 dark:text-blue-500 hover:underline"
            onClick={() => setShowDetails(true)}
          >
            Delete
          </a>
        </td>
      </tr>
      <tr className="bg-white border-b dark:bg-gray-800 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600">
        <th
          scope="row"
          className="flex items-center px-6 py-4 text-gray-900 whitespace-nowrap dark:text-white"
        >
          <div className="pl-3">
            <div className="text-base font-semibold">User 1</div>
            <div className="font-normal text-gray-500">
              User3@Org3.com
            </div>
          </div>
        </th>
        <td className="px-6 py-4">user3-hcx@staging-swasth</td>
        <td className="px-6 py-4">
          <div className="flex items-center">
            {/* <div className="h-2.5 w-2.5 rounded-full bg-green-500 mr-2" />{" "} */}
            9999999999
          </div>
        </td>
         <td className="px-6 py-4">admin3-hcx@staging-swasth</td>

        <td className="px-6 py-4 space-x-3">
          {/* Modal toggle */}
          <a
            href="#"
            type="button"
            data-modal-target="editUserModal"
            data-modal-show="editUserModal"
            className="font-medium text-blue-600 dark:text-blue-500 hover:underline"
            onClick={() => setShowDetails(true)}
          >
            Add 
          </a>
        </td>
      </tr>
    </tbody>
  </table>
  {/* Edit user modal */}
  <div className={"fixed inset-0 bg-black bg-opacity-30 z-40 "  + (showDetails ? "" : "hidden")} ></div>
  <div
    id="editUserModal"
    tabIndex={-1}
    className={"fixed top-0 left-0 right-0 z-50 w-full p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full justify-center items-center flex " + (showDetails? "" : "hidden")}
  >
    <div className="relative w-full max-w-2xl max-h-full">
      {/* Modal content */}
      <form
        action="#"
        className="relative bg-white rounded-lg shadow dark:bg-gray-700"
      >
        {/* Modal header */}
        <div className="flex items-start justify-between p-4 border-b rounded-t dark:border-gray-600">
          <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
            Add User to Participant
          </h3>
          <button
            type="button"
            className="text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm p-1.5 ml-auto inline-flex items-center dark:hover:bg-gray-600 dark:hover:text-white"
            data-modal-hide="editUserModal"
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
          </button>
        </div>
        {/* Modal body */}
        <div className="p-6 space-y-6">
          <div className="grid grid-cols-6 gap-6">
            <div className="col-span-6 sm:col-span-3">
              <label
                htmlFor="first-name"
                className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >
                Name
              </label>
              <input
                type="text"
                name="first-name"
                id="first-name"
                className="shadow-sm bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-600 focus:border-blue-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                placeholder="Bonnie"
              />
            </div>
            <div className="col-span-6 sm:col-span-3">
              <label
                htmlFor="last-name"
                className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >
                User Code
              </label>
              <input
                type="text"
                name="last-name"
                id="last-name"
                className="shadow-sm bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-600 focus:border-blue-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                placeholder="Green"
              />
            </div>
            <div className="col-span-6 sm:col-span-3">
              <label
                htmlFor="email"
                className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >
                Email
              </label>
              <input
                type="email"
                name="email"
                id="email"
                className="shadow-sm bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-600 focus:border-blue-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                placeholder="example@company.com"
              />
            </div>
            <div className="col-span-6 sm:col-span-3">
              <label
                htmlFor="phone-number"
                className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >
                Phone Number
              </label>
              <input
                type="number"
                name="phone-number"
                id="phone-number"
                className="shadow-sm bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-600 focus:border-blue-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                placeholder="e.g. +(12)3456 789"
              />
            </div>
            <div className="col-span-6 sm:col-span-3">
              <label
                htmlFor="current-password"
                className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >
                Participant
              </label>
                  <select id="payordropdown" 
                        className="shadow-sm bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-600 focus:border-blue-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" >
                        <option selected value="1">Particpant 1</option>
                        <option selected value="2">Particpant 2</option>
                      </select>
            </div>
            <div className="col-span-6 sm:col-span-3">
              <label
                htmlFor="new-password"
                className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >
                Role
              </label>
              <select id="payordropdown" 
                        className="shadow-sm bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-600 focus:border-blue-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" >
                        <option selected value="1">Admim</option>
                        <option selected value="2">Edit</option>
                        <option selected value="2">View</option>
                      </select>

            </div>
          </div>
        </div>
        {/* Modal footer */}
        <div className="flex items-center p-6 space-x-2 border-t border-gray-200 rounded-b dark:border-gray-600">
          <button
            type="submit"
            className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
            onClick={(event) => {event.preventDefault(); setShowDetails(false)}}
          >
            Add User
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
</div>
</div>
</div>
    )
}
 
export default Users;