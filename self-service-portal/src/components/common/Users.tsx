import React, { useEffect, useState } from "react";
import 'flowbite';
import { getAllUser, serachUser, userInvite } from "../../api/UserService";
import _ from "lodash";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../store";
import { toast } from "react-toastify";
import { useDebounce } from "use-debounce";
import { eventManager } from "react-toastify/dist/core";

const Users =() => {
  const dispatch = useDispatch();
  const participantToken = useSelector((state: RootState) => state.tokenReducer.participantToken);
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);

    const [participantList, setParticipantList] = useState([]);
    const [userData, setUserData] = useState([]);
    const [participantSelected,setParticipantSelected] = useState('');
    const [roleSelected, setRoleSelected] = useState('admin');
    const [searchText, setSearchText] = useState('');
    const [searchUser] = useDebounce(searchText, 1000);
    const [createdBy, setCreatedBy] = useState('');
    const [dropdownValue, setDropDown] = useState('all');
    const [start, setStart] = useState(1);
    const [end, setEnd] = useState(10);

    
   const clickNext = () => {
    console.log("i came in next", start);
    if(start < userData.length){
    setStart(start+10)
    setEnd(end+10)
    }
   }
   
   const clickPrev = () => {
    if(start > 1){
    setStart(start-10)
    setEnd(end-10)
   }
  }


    useEffect(() => {
      console.log("dropdown to search ", createdBy, dropdownValue);
      if(searchUser !== ""){
      serachUser(searchUser).then((res:any) => {
        let user = res["data"]["users"];
        console.log("user data", user);
        if(dropdownValue == "all"){
          setUserData(user.map((value:any, index:any) => { return value }));  
        }else{
          setUserData(user.filter((user1: { created_by: string; }) => user1.created_by == createdBy).map((value:any, index:any) => { return value}));  
        }
      })}else{
        getAllUser().then((res:any) =>{
          let user = res["data"]["users"];
          console.log("user data all", user.length);
          if(dropdownValue == "all"){
            setUserData(user.map((value:any, index:any) => { return value }));  
          }else{
            setUserData(user.filter((user1: { created_by: string; }) => user1.created_by == createdBy).map((value:any, index:any) => { return value}));  
          }
        })    
      }
  },[dropdownValue])


    useEffect(() => {
        console.log("username to search ", searchUser);
        if(searchUser !== ""){
        serachUser(searchUser).then((res:any) => {
          let user = res["data"]["users"];
          console.log("user data", user);
          setUserData(user.map((value:any, index:any) => { return value }));  
        })}else{
          getAllUser().then((res:any) =>{
            let user = res["data"]["users"];
            console.log("user data", user);
            setUserData(user.map((value:any, index:any) => { return value }));
            
          })    
        }
    },[searchUser])

    useEffect(()=> {
      getAllUser().then((res:any) =>{
        let user = res["data"]["users"];
        console.log("user data", user);
        setUserData(user.map((value:any, index:any) => { return value }));
        
      })
      serachUser(_.get(appData,"username")).then((res: any) => {
        let participant = res["data"]["users"][0]["tenant_roles"];
        setCreatedBy(res["data"]["users"][0]["created_by"]);
        setParticipantList(participant);
        participant.map((value:any,index:any) => {
          if(value.role == "admin"){
            console.log("participant selected in use", value.participant_code);
            setParticipantSelected(value.participant_code);
        }
      });
      })
    },[])
    const [showDetails, setShowDetails] = useState(false);
    const [userDetails, setUserDetails] = useState({"name":"","user_code":"","email":""})



    const inviteUsers = () => {
        console.log("participant selected", participantSelected);
        userInvite({ "email": userDetails.email, "participant_code": participantSelected, "role": roleSelected, "invited_by": _.get(appData,"username") }).then(res => {
          toast.success(`${userDetails.email} has been successfully invited`);              
        }).catch(err => {
          toast.error(`${userDetails.email} could not be invited. ` + _.get(err, 'response.data.error.message') || "Internal Server Error",);
        })
      toast.success("Users have been successfully invited");
    }


    const userDetailsShow = (value:any) => {
      setUserDetails({"name":_.get(value,"user_name"),"user_code":_.get(value,"user_id"),"email":_.get(value,"email")})
    }
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
                        onChange={(event) => setDropDown(event.target.value)}
                        className="inline-flex items-center text-gray-500 bg-white border border-gray-300 focus:outline-none hover:bg-gray-100 focus:ring-4 focus:ring-gray-200 font-medium rounded-lg text-sm px-3 py-1.5 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-600 dark:hover:bg-gray-700 dark:hover:border-gray-600 dark:focus:ring-gray-700" >
                        <option selected value="all">All</option>
                        <option value="invitedby">Invited by User</option>

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
        onChange={(event) => setSearchText(event.target.value)}
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
          Created By
        </th>
        <th scope="col" className="px-6 py-3">
          Action
        </th>
      </tr>
    </thead>
    <tbody>
    { userData.map((value:any,index:any) => {
        if(index >= start-1 && index < end ){
        return <tr className="bg-white border-b dark:bg-gray-800 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600">
        <th
          scope="row"
          className="flex items-center px-6 py-4 text-gray-900 whitespace-nowrap dark:text-white"
        >
          <div className="pl-3">
            <div className="text-base font-semibold">{_.get(value,"user_name")}</div>
            <div className="font-normal text-gray-500">
              {_.get(value,"email")}
            </div>
          </div>
        </th>
        <td className="px-6 py-4">{_.get(value,"user_id")}</td>
         <td className="px-6 py-4">{_.get(value,"created_by")}</td>

        <td className="px-6 py-4 space-x-3">
          {/* Modal toggle */}
          <a
            href="#"
            type="button"
            data-modal-target="editUserModal"
            data-modal-show="editUserModal"
            className="font-medium text-blue-600 dark:text-blue-500 hover:underline"
            onClick={() => {setShowDetails(true); userDetailsShow(value)}}
          >
            Add 
          </a>
        </td>
      </tr>}
      })
      }     
    </tbody>
  </table>
  <div className="flex flex-col items-center m-2 p-2">
  {/* Help text */}
  <span className="text-sm text-gray-700 dark:text-gray-400">
    Showing{" "}
    <span className="font-semibold text-gray-900 dark:text-white">{start}</span> to{" "}
    <span className="font-semibold text-gray-900 dark:text-white">{end}</span> of{" "}
    <span className="font-semibold text-gray-900 dark:text-white">{userData.length}</span>{" "}
    Entries
  </span>
  {/* Buttons */}
  <div className="inline-flex mt-2 xs:mt-0">
    <button className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white"
      onClick={()=>clickPrev()}>
      Prev
    </button>
    <button className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white"
      onClick={()=>clickNext()}>
      Next
    </button>
  </div>
</div>



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
            onClick={(event) => {setShowDetails(false)}}
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
                placeholder="Name"
                disabled
                value={userDetails.name}

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
                placeholder="User Code"
                value={userDetails.user_code}
                disabled
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
                value={userDetails.email}
                disabled
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
                        className="shadow-sm bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-600 focus:border-blue-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                        onChange={(event) => setParticipantSelected(event.target.value)}>    
                        {participantList !== undefined ? participantList.map((value:any,index:any) => {
                          if(value.role == "admin"){
                          return <option selected value={value.participant_code}>{value.participant_code}</option>  
                          }else{
                            return null
                          }
                        }) : null}
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
                        className="shadow-sm bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-600 focus:border-blue-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                        onChange={(event) => setRoleSelected(event.target.value)}>    
                        <option selected value="admin">Admin</option>
                        <option value="config-manager">Config Manager</option>
                        <option value="viewer">Viewer</option>
                      </select>

            </div>
          </div>
        </div>
        {/* Modal footer */}
        <div className="flex items-center p-6 space-x-2 border-t border-gray-200 rounded-b dark:border-gray-600">
          <button
            type="submit"
            className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
            onClick={(event) => {event.preventDefault(); inviteUsers(); setShowDetails(false)}}
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