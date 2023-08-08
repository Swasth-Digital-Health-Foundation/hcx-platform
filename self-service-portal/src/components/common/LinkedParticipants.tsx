import { navigate } from "raviger";
import React, { useEffect, useState } from "react";
import { addAppData } from "../../reducers/app_data";
import { useDispatch, useSelector } from "react-redux";
import LinkedUsers from "./LinkedUsers";
import { RootState } from "../../store";
import { getLinkedUsers, serachUser } from "../../api/UserService";
import _ from "lodash";
import { getParticipant, getParticipantByCode } from "../../api/RegistryService";
import { addParticipantDetails } from "../../reducers/participant_details_reducer";


const LinkedParticipant = () => {

  const dispatch = useDispatch();
  const participantToken = useSelector((state: RootState) => state.tokenReducer.participantToken);
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  const [data, setData] = useState([{"participantcode":"","email":"","role":"","status":"","organization":""}]);

  useEffect(() => {
    serachUser(_.get(appData,"username")).then((res: any) => {
      console.log("search user res", res);
      let osOwner = res["data"]["users"][0]["osOwner"];
      let participant = res["data"]["users"][0]["tenant_roles"];
      participant.map((value: any,index: any) => {
        console.log("index aa", index);
        getParticipantByCode(value.participant_code).then(res => {
          console.log("participant info", res);
          data.push({"participantcode":value.participant_code,"email":res["data"]["participants"][0]["primary_email"],"role":value.role,"status":res["data"]["participants"][0]["status"],"organization":res["data"]["participants"][0]["participant_name"]})
          setData(_.uniqBy(data, function(elem) { return [elem.email, elem.role].join(); }).map((value, index) => { return value }));
        })
      })
     console.log("participant data updated", data); 
    });

  },[]);


  const viewParticipant =(value:any) =>{
    getParticipant(value.email).then((res :any) => {
      console.log("we are in inside get par", res);
       dispatch(addParticipantDetails(res["data"]["participants"][0]));
       dispatch(addAppData({"sidebar":"Profile"}));
    })
  }
  

  
  
    return(
      <>
      <LinkedUsers></LinkedUsers>

        <div className="p-4 sm:ml-64">
        <div className="p-4 border-2 border-gray-200 border rounded-lg dark:border-gray-700 mt-14">
        <div className="w-full p-12">
        <div className="flex flex-wrap -mx-3 mb-6 border-b-2 shadow-l shadow-bottom justify-between">
          <label
            className="lable-page-header"
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
          Status
        </th>
        <th scope="col" className="px-6 py-3">
          Role
        </th>
        <th scope="col" className="px-6 py-3">
          Action
        </th>
      </tr>
    </thead>
    <tbody>
      {data.map((value,index) => {
        if(index == 0){
          return null
        }else{
        return <tr className="bg-white border-b dark:bg-gray-800 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600">
        <th
          scope="row"
          className="px-6 py-4 font-medium text-gray-900 whitespace-nowrap dark:text-white"
        >
          {value.participantcode}
        </th>
        <td className="px-6 py-4">{value.organization}</td>
        <td className="px-6 py-4">{value.email}</td>
        <td className="px-6 py-4">
        <div className="flex items-center">
          <div className="h-2.5 w-2.5 rounded-full bg-green-500 mr-2" />{" "} {value.status}
          </div>
          </td>
          <td className="px-6 py-4">{value.role}</td>
        <td className="flex items-center px-6 py-4 space-x-3">
          <a
            href="#"
            className="font-medium text-blue-600 dark:text-blue-500 hover:underline"
            onClick={(event) => {event.preventDefault(); viewParticipant(value)}}
          >
            View
          </a>
        </td>
      </tr>
        }
      })}

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