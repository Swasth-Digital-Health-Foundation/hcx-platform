import React, { useEffect, useState } from "react";
import { addAppData } from "../../reducers/app_data";
import { useDispatch, useSelector } from "react-redux";
import { serachUser, userInvite } from "../../api/UserService";
import { RootState } from "../../store";
import { toast } from "react-toastify";
import { navigate } from "raviger";
import _ from "lodash";

const CreateUser = () => {

  const dispatch = useDispatch();
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  const participantDetails: Object = useSelector((state: RootState) => state.participantDetailsReducer.participantDetails);
  const [participantSelected, setParticipantSelected] = useState('');
  const [participantList, setParticipantList] = useState([]);
  
  useEffect(() => {
    serachUser(_.get(appData, "username")).then((res: any) => {
      setParticipantList(res["data"]["users"][0]["tenant_roles"]);
      let tempPar = res["data"]["users"][0]["tenant_roles"];
      tempPar.map((value: any, index: any) => {
        if (value.role == "admin") {
          setParticipantSelected(value.participant_code);
        }
      })
      console.log("par list  ", participantList, participantSelected);
    }).catch(err => {
      console.log("some error happened");
    });
  }, [])

  const [userDetials, setUserDetails] = useState([{ "email": "", "role": "admin" }]);
  const addAnotherRow = () => {
    console.log("i came here", userDetials);
    userDetials.push({ "email": "", "role": "" })
    setUserDetails(userDetials.map((value, index) => { return value }));
  }


  const removeRow = (index: any) => {
    if (userDetials.length > 1) {
      const val = userDetials.indexOf(index);
      if (index > -1) { // only splice array when item is found
        userDetials.splice(index, 1); // 2nd parameter means remove one item only
      }
      setUserDetails(userDetials.map((value, index) => { return value }));
    }
  }

  const updateCreateUserData = (value: string, index: number, field: string) => {
    _.update(userDetials[index], field, function (n) { return value });
    setUserDetails(userDetials.map((value, index) => { return value }));
    console.log("user details", userDetials);
  }


  const inviteUsers = () => {
    userDetials.map((value, index) => {
      console.log("values", value);
      if (value.email !== "") {
        userInvite({ "email": value.email, "participant_code": participantSelected, "role": value.role, "invited_by": _.get(appData, "username") }).then(res => {
          toast.success(`${value.email} has been successfully invited`);
        }).catch(err => {
          toast.error(`${value.email} could not be invited. ` + _.get(err, 'response.data.error.message') || "Internal Server Error",);
        })
      }
    });
    setUserDetails([{ "email": "", "role": "admin" }]);
  }

  return (
    <div className="p-4 sm:ml-64">
      <div className="p-4 border-2 border-gray-200 border rounded-lg dark:border-gray-700 mt-14">
        <form className="w-full p-12">
          <div className="flex flex-wrap -mx-3 mb-6 border-b-2 shadow-l shadow-bottom justify-between">
            <label
              className="block uppercase tracking-wide text-gray-700 text-s font-bold mb-2"
            >
              Invite Users
            </label>
            <button
              type="button"
              className="inline-block mb-2 rounded border-2 border-blue-500 px-6 pb-[6px] pt-2 text-xs font-medium uppercase leading-normal text-blue-500 transition duration-150 ease-in-out hover:border-blue-600 hover:bg-neutral-500 hover:bg-opacity-10 hover:text-blue-600 focus:border-blue-600 focus:text-blue-600 focus:outline-none focus:ring-0 active:border-blue-700 active:text-blue-700 dark:hover:bg-neutral-100 dark:hover:bg-opacity-10"
              data-te-ripple-init=""
              data-te-ripple-color="light"
              onClick={() => { dispatch(addAppData({"sidebar":"Manage Users"}))}}
            >
              Back
            </button>
          </div>
          <div className="flex flex-wrap -mx-3 mb-6 px-3">
            <p className="py-2 mb-4 w-3/6 font-semibold">Select the Participant for which you would like to invite the users:</p>
            <select id="dropdownlist"
              className="w-3/6 appearance-none block bg-gray-200 text-gray-700 border border-gray-200 rounded py-3 px-4 mb-3 leading-tight focus:outline-none focus:bg-white"
              onChange={(event) => setParticipantSelected(event.target.value)}>
              {participantList !== undefined ? participantList.map((value: any, index: any) => {
                if (value.role == "admin") {
                  return <option selected value={value.participant_code}>{value.participant_code}</option>
                } else {
                  return null
                }
              }) : null}
            </select>
          </div>
          {/* <div className="flex flex-wrap -mx-3 px-3">
              <p className="py-2 mb-4">{`You have admin access to only one user. Participant code "${participantSelected}" will be used to invite users`} </p>
          </div>  */}
          <div className="flex flex-wrap -mx-3 mb-6 border-b-2 shadow-l shadow-bottom justify-between">
            <label
              className="block uppercase tracking-wide text-gray-700 text-s font-bold mb-2"
            >

            </label>
          </div>

          {userDetials.map((value, index) => {
            return <>

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
                    placeholder="Email"
                    value={value.email}
                    onChange={(event) => updateCreateUserData(event.target.value, index, "email")}
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
                    className="appearance-none block w-full bg-gray-200 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500"
                    onChange={(event) => { updateCreateUserData(event.target.value, index, "role") }}
                    value={value.role}>
                    <option value="admin">Admin</option>
                    <option value="config-manager">Config Manager</option>
                    <option value="viewer">Viewer</option>
                  </select>
                  {index !== 0 ?
                    <div className="flex items-center place-content-end w-full">
                      <a href="#" className="text-blue-700 text-xs underline" onClick={(event) => { event.preventDefault(); removeRow(index) }}>Remove</a>
                    </div> : null}
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
              Add Another User
            </button>
            <button
              type="button"
              className="inline-block rounded border-2 border-blue-500 px-6 pb-[6px] pt-2 text-xs font-medium uppercase leading-normal text-blue-500 transition duration-150 ease-in-out hover:border-blue-600 hover:bg-neutral-500 hover:bg-opacity-10 hover:text-blue-600 focus:border-blue-600 focus:text-blue-600 focus:outline-none focus:ring-0 active:border-blue-700 active:text-blue-700 dark:hover:bg-neutral-100 dark:hover:bg-opacity-10"
              data-te-ripple-init=""
              data-te-ripple-color="light"
              onClick={() => { inviteUsers(); }}
            >
              Invite
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default CreateUser;