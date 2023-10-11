import _, { delay } from 'lodash';
import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux';
import { toast } from 'react-toastify';
import { DataTable } from 'simple-datatables'
import { getAllUser, serachUser, userInvite, userRemove } from '../api/UserService';
import { RootState } from '../store';
import ModalUserUpdate from './ModalUserUpdate';
import { getParticipantByCode } from '../api/RegistryService';
import { addAppData } from '../reducers/app_data';
import { addParticipantDetails } from '../reducers/participant_details_reducer';
import { useNavigate } from 'react-router-dom';
import Loader from '../common/Loader';


const DataTableParticipant: React.FC = () => {

  
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const participantToken = useSelector((state: RootState) => state.tokenReducer.participantToken);
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  const [data, setData] = useState([{"participantcode":"","email":"","role":"","status":"","organization":""}]);
  const [showComponent, setShowComponent] = useState(false);

  useEffect(() => {
    const delayTimeout = setTimeout(() => {
      setShowComponent(true);
    }, 2000); // 2000 milliseconds = 2 seconds

    return () => clearTimeout(delayTimeout);
  }, []);


  useEffect(() => {
    serachUser(_.get(appData,"username")).then((res: any) => {
      console.log("search user res", res);
      let participant = res["data"]["users"][0]["tenant_roles"];
      participant.map((value: any,index: any) => {
        console.log("index aa", index);
        getParticipantByCode(value.participant_code).then(res => {
          console.log("participant info", res);
          data.push({"participantcode":value.participant_code,"email":res["data"]["participants"][0]["primary_email"],"role":value.role,"status":res["data"]["participants"][0]["status"],"organization":res["data"]["participants"][0]["participant_name"]})
          setData(_.uniqBy(data, function(elem) { return [elem.email, elem.participantcode, elem.role].join(); }).map((value, index) => { return value }));
        })
      })
     console.log("participant data updated", data); 
    });

  },[]);


  const viewParticipant =(value:any) =>{
    getParticipantByCode(value.participantcode).then((res :any) => {
      console.log("we are in inside get par", res);
       dispatch(addParticipantDetails(res["data"]["participants"][0]));
    })
    navigate("/onboarding/profile")
  }

  useEffect(() => {
    if(showComponent) {
    const dataTableTwo = new DataTable('#dataTableTwo', {
      perPageSelect: [5, 10, 15, ['All', -1]],
    });
    dataTableTwo;
    }
  }, [showComponent]);

  return (
    <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
      { showComponent ? 
      <div className="data-table-common data-table-two max-w-full overflow-x-auto">
        <table className="table w-full table-auto" id="dataTableTwo">
          <thead>
            <tr>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Participant Code</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Organization</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Email ID</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Status</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
              <th data-type="date" data-format="YYYY/DD/MM">
                <div className="flex items-center justify-between gap-1.5">
                  <p>Role</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Action</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            {data.map((value, index) => {
              if(index == 0){
                return null
              }else{
              return (
                <tr key={index}>
                  <td>{_.get(value, "participantcode")}</td>
                  <td>{_.get(value, "organization")}</td>
                  <td>{_.get(value, "email")}</td>
                  <td>
                    {_.get(value, "status") == "Active" ? 
                    <p className="inline-flex rounded-full bg-success bg-opacity-10 py-1 px-3 text-sm font-medium text-success">
                     {_.get(value, "status")}
                    </p>:
                    <p className="inline-flex rounded-full bg-danger bg-opacity-10 py-1 px-3 text-sm font-medium text-danger">
                      {_.get(value, "status")}
                    </p>}</td>
                    <td>{_.get(value, "role")}</td>
                  <td className="py-5 px-4">
                  <div className="flex items-center space-x-3.5">
                  <button className="hover:text-primary"
                    onClick={(event) => {event.preventDefault(); viewParticipant(value)}}>
                    <svg
                      className="fill-current"
                      width="18"
                      height="18"
                      viewBox="0 0 18 18"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M8.99981 14.8219C3.43106 14.8219 0.674805 9.50624 0.562305 9.28124C0.47793 9.11249 0.47793 8.88749 0.562305 8.71874C0.674805 8.49374 3.43106 3.20624 8.99981 3.20624C14.5686 3.20624 17.3248 8.49374 17.4373 8.71874C17.5217 8.88749 17.5217 9.11249 17.4373 9.28124C17.3248 9.50624 14.5686 14.8219 8.99981 14.8219ZM1.85605 8.99999C2.4748 10.0406 4.89356 13.5562 8.99981 13.5562C13.1061 13.5562 15.5248 10.0406 16.1436 8.99999C15.5248 7.95936 13.1061 4.44374 8.99981 4.44374C4.89356 4.44374 2.4748 7.95936 1.85605 8.99999Z"
                        fill=""
                      />
                      <path
                        d="M9 11.3906C7.67812 11.3906 6.60938 10.3219 6.60938 9C6.60938 7.67813 7.67812 6.60938 9 6.60938C10.3219 6.60938 11.3906 7.67813 11.3906 9C11.3906 10.3219 10.3219 11.3906 9 11.3906ZM9 7.875C8.38125 7.875 7.875 8.38125 7.875 9C7.875 9.61875 8.38125 10.125 9 10.125C9.61875 10.125 10.125 9.61875 10.125 9C10.125 8.38125 9.61875 7.875 9 7.875Z"
                        fill=""
                      />
                    </svg>
                  </button>
                  </div>
                  </td>

                </tr>
              )};
            })}
          </tbody>
        </table>
      </div> : 
      <>
            <Loader></Loader>
            <label className="m-10 p-5 block text-black dark:text-white">
                    Fetching Participant Information
            </label>        
            </>        }
    </div>
  );
};

export default DataTableParticipant;
