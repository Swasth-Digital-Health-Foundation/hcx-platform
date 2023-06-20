import React, { useEffect, useState } from "react";

const CreateUser = () => {

const [userDetials, setUserDetails] = useState([{"username":"","phone":"","email":"","role":""}])

const addAnotherRow = () => {
    console.log("i came here", userDetials);
    userDetials.push({"username":"","phone":"","email":"","role":""})
    setUserDetails(userDetials.map((value,index) => {return value}));
}
    return(
        <div className="p-4 sm:ml-64">
        <div className="p-4 border-2 border-gray-200 border rounded-lg dark:border-gray-700 mt-14">
        <form className="w-full p-12">
        {userDetials.map((value,index)=> {
            return <>
            <div className="flex flex-wrap -mx-3 mb-6 border-b-2 shadow-l shadow-bottom justify-between">
            <label
              className="block uppercase tracking-wide text-gray-700 text-xs font-bold mb-2"
            >
              Create User {index+1}
            </label>
          </div> 
          <div className="flex flex-wrap -mx-3 mb-6">
          <div className="w-full md:w-1/2 px-3 mb-6 md:mb-0">
            <label
              className="block uppercase tracking-wide text-gray-700 text-xs font-bold mb-2"
              htmlFor="grid-first-name"
            >
              Email Address
            </label>
            <input
              className="appearance-none block w-full bg-gray-200 text-gray-700 border border-gray-200 rounded py-3 px-4 mb-3 leading-tight focus:outline-none focus:bg-white"
              id="grid-first-name"
              type="text"
              placeholder="Jane"
            />
            {/* <p className="text-red-500 text-xs italic">Please fill out this field.</p> */}
          </div>
          <div className="w-full md:w-1/2 px-3 mb-6 md:mb-0">
            <label
              className="block uppercase tracking-wide text-gray-700 text-xs font-bold mb-2"
              htmlFor="grid-last-name"
            >
              Role
            </label>
                          <select id="payordropdown" 
                        className="appearance-none block w-full bg-gray-200 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500" >
                        <option selected value="admin">Admin</option>
                        <option value="config-manager">Config Manager</option>
                        <option value="view">Viewer</option>
                      </select>
          </div>
   
        </div></>
          })}    
        
      <div className="flex items-center justify-between -mx-3 mb-6 p-3">
                    <button
                      type="button"
                      className="inline-block rounded border-2 border-blue-500 px-6 pb-[6px] pt-2 text-xs font-medium uppercase leading-normal text-blue-500 transition duration-150 ease-in-out hover:border-blue-600 hover:bg-neutral-500 hover:bg-opacity-10 hover:text-blue-600 focus:border-blue-600 focus:text-blue-600 focus:outline-none focus:ring-0 active:border-blue-700 active:text-blue-700 dark:hover:bg-neutral-100 dark:hover:bg-opacity-10"
                      data-te-ripple-init=""
                      data-te-ripple-color="light"
                      onClick={() => addAnotherRow()}   
                    >
                      Add Another
                    </button>
                    <button
                      type="button"
                      className="inline-block rounded border-2 border-blue-500 px-6 pb-[6px] pt-2 text-xs font-medium uppercase leading-normal text-blue-500 transition duration-150 ease-in-out hover:border-blue-600 hover:bg-neutral-500 hover:bg-opacity-10 hover:text-blue-600 focus:border-blue-600 focus:text-blue-600 focus:outline-none focus:ring-0 active:border-blue-700 active:text-blue-700 dark:hover:bg-neutral-100 dark:hover:bg-opacity-10"
                      data-te-ripple-init=""
                      data-te-ripple-color="light"
                      
                    >
                      Submit
                    </button>
                  </div>
      </form>
      </div>
      </div>
    )
}

export default CreateUser;