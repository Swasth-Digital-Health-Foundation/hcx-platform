import _, { delay } from 'lodash';
import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux';
import { toast } from 'react-toastify';
import { DataTable } from 'simple-datatables'
import { getAllUser, serachUser, userInvite, userRemove } from '../api/UserService';
import { RootState } from '../store';
import ModalUserUpdate from './ModalUserUpdate';
import Loader from '../common/Loader';
import ModalConfirmBack from './ModalConfrimBack';


const DataTableUsers: React.FC = () => {
  const dispatch = useDispatch();
  const participantToken = useSelector((state: RootState) => state.tokenReducer.participantToken);
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  const [userData, setUserData] = useState([]);
  const [adminRoleParticipants, setAdminRoleParticipants] = useState([]);
  const [showDetails, setShowDetails] = useState(false);
  const [userDetails, setUserDetails] = useState({ "name": "", "user_code": "", "email": "", "participant_code": "", "role": "" })
  const [showComponent, setShowComponent] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    const delayTimeout = setTimeout(() => {
      setShowComponent(true);
    }, 2000); // 2000 milliseconds = 2 seconds

    return () => clearTimeout(delayTimeout);
  }, []);

  useEffect(() => {
    getAllUser(participantToken).then((res: any) => {
      let user = res["data"]["users"];
      console.log("user data", user);
      let dataUser: any = [];
      user.map((value: any, index: any) => {
        value.tenant_roles.map((val: any, ind: any) => {
          dataUser.push({ ...value, "participant_code": val.participant_code, "role": val.role })
        });
      });

      setUserData(dataUser);
      console.log("data user", dataUser);
    })
    serachUser(_.get(appData, "username")).then((res: any) => {
      let participant = res["data"]["users"][0]["tenant_roles"];
      let adminPartRole: any = [];
      participant.map((value: any, index: any) => {
        if (value.role == "admin") {
          adminPartRole.push(value.participant_code);
        }
      });
      setAdminRoleParticipants(adminPartRole);
    })
  }, [])

  
  const removeUser = (userDetails:any) => {
    userRemove(userDetails.participant_code, [{ "user_id": userDetails.email, "role": userDetails.role }]).then(res => {
      getAllUser(participantToken).then((res: any) => {
        setShowComponent(false);
        let user = res["data"]["users"];
        console.log("user data", user);
        let dataUser: any = [];
        user.map((value: any, index: any) => {
          value.tenant_roles.map((val: any, ind: any) => {
            dataUser.push({ ...value, "participant_code": val.participant_code, "role": val.role })
          });
        });
        setUserData(dataUser);
        setShowComponent(true);
      })
      toast.success("User successfully removed.")
    }).catch(err => {
      toast.error("Something went wrong. Please try again")
    })
  }

  const userDetailsShow = (value: any) => {
    setUserDetails({ "name": _.get(value, "user_name"), "user_code": _.get(value, "user_id"), "email": _.get(value, "email"), "participant_code": _.get(value, "participant_code"), "role": _.get(value, "role") })
  }

  const inviteUsers = (role:string) => {
    userInvite({ "email": userDetails.email, "participant_code": userDetails.participant_code, "role": role, "invited_by": _.get(appData, "username") }).then(res => {
      toast.success(`${userDetails.email} has been successfully invited`);
    }).catch(err => {
      toast.error(`${userDetails.email} could not be invited with new role assigned. ` + _.get(err, 'response.data.error.message') || "Internal Server Error",);
    })
    setShowDetails(false);
  }

  const deleteUserCancel = () => {
    setShowDeleteModal(false);
  }

  const deleteUserApproved = () => {
    setShowDeleteModal(false);
    removeUser(userDetails);
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
      {showDetails  ==  true?
      <ModalUserUpdate show={showDetails} values={userDetails} onSubmitClick={(role) => inviteUsers(role)} onCancelClick={() => setShowDetails(false)}></ModalUserUpdate> :
      null }
      {showDeleteModal == true?
      <ModalConfirmBack show={showDeleteModal} title="Remove User from Organization" body="You are about to remove the user from the participant organization. User once removed can only be added through invite again." 
      onCancelClick={deleteUserCancel} onSubmitClick={deleteUserApproved}></ModalConfirmBack> :
      null }
      { showComponent ? 
      <div className="data-table-common data-table-two max-w-full overflow-x-auto">
        <table className="table w-full table-auto" id="dataTableTwo">
          <thead>
            <tr>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Name</p>
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
                  <p>User Code</p>
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
                  <p>Created By</p>
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
                  <p>Tenant</p>
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
            {userData.map((value, index) => {
              return (
                <tr key={index}>
                  <td>{_.get(value, "user_name")}</td>
                  <td>{_.get(value, "user_id")}</td>
                  <td>{_.get(value, "created_by")}</td>
                  <td>{_.get(value, "participant_code")}</td>
                  <td>{_.get(value, "role")}</td>
                  <td className="py-5 px-4">
                <div className="flex items-center space-x-3.5">
                  {adminRoleParticipants.find(element => element == _.get(value, "participant_code")) !== undefined &&  _.get(appData,"username") != _.get(value, "email")?
                  <>
                  <button className="hover:text-primary"
                    onClick={(event) => { event.preventDefault(); userDetailsShow(value); setShowDeleteModal(true); console.log("i amc delete");  }}>
                    <svg
                      className="fill-danger"
                      width="18"
                      height="18"
                      viewBox="0 0 18 18"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M13.7535 2.47502H11.5879V1.9969C11.5879 1.15315 10.9129 0.478149 10.0691 0.478149H7.90352C7.05977 0.478149 6.38477 1.15315 6.38477 1.9969V2.47502H4.21914C3.40352 2.47502 2.72852 3.15002 2.72852 3.96565V4.8094C2.72852 5.42815 3.09414 5.9344 3.62852 6.1594L4.07852 15.4688C4.13477 16.6219 5.09102 17.5219 6.24414 17.5219H11.7004C12.8535 17.5219 13.8098 16.6219 13.866 15.4688L14.3441 6.13127C14.8785 5.90627 15.2441 5.3719 15.2441 4.78127V3.93752C15.2441 3.15002 14.5691 2.47502 13.7535 2.47502ZM7.67852 1.9969C7.67852 1.85627 7.79102 1.74377 7.93164 1.74377H10.0973C10.2379 1.74377 10.3504 1.85627 10.3504 1.9969V2.47502H7.70664V1.9969H7.67852ZM4.02227 3.96565C4.02227 3.85315 4.10664 3.74065 4.24727 3.74065H13.7535C13.866 3.74065 13.9785 3.82502 13.9785 3.96565V4.8094C13.9785 4.9219 13.8941 5.0344 13.7535 5.0344H4.24727C4.13477 5.0344 4.02227 4.95002 4.02227 4.8094V3.96565ZM11.7285 16.2563H6.27227C5.79414 16.2563 5.40039 15.8906 5.37227 15.3844L4.95039 6.2719H13.0785L12.6566 15.3844C12.6004 15.8625 12.2066 16.2563 11.7285 16.2563Z"
                        fill=""
                      />
                      <path
                        d="M9.00039 9.11255C8.66289 9.11255 8.35352 9.3938 8.35352 9.75942V13.3313C8.35352 13.6688 8.63477 13.9782 9.00039 13.9782C9.33789 13.9782 9.64727 13.6969 9.64727 13.3313V9.75942C9.64727 9.3938 9.33789 9.11255 9.00039 9.11255Z"
                        fill=""
                      />
                      <path
                        d="M11.2502 9.67504C10.8846 9.64692 10.6033 9.90004 10.5752 10.2657L10.4064 12.7407C10.3783 13.0782 10.6314 13.3875 10.9971 13.4157C11.0252 13.4157 11.0252 13.4157 11.0533 13.4157C11.3908 13.4157 11.6721 13.1625 11.6721 12.825L11.8408 10.35C11.8408 9.98442 11.5877 9.70317 11.2502 9.67504Z"
                        fill=""
                      />
                      <path
                        d="M6.72245 9.67504C6.38495 9.70317 6.1037 10.0125 6.13182 10.35L6.3287 12.825C6.35683 13.1625 6.63808 13.4157 6.94745 13.4157C6.97558 13.4157 6.97558 13.4157 7.0037 13.4157C7.3412 13.3875 7.62245 13.0782 7.59433 12.7407L7.39745 10.2657C7.39745 9.90004 7.08808 9.64692 6.72245 9.67504Z"
                        fill=""
                      />
                    </svg>
                  </button>
                  <button className="hover:text-primary"
                  onClick={(event) => { event.preventDefault(); userDetailsShow(value); setShowDetails(true); console.log("i amc licked") }}>
                    <svg
                      className="fill-white"
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      stroke="gray"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >  
                    <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
                    <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
                    </svg>
                  </button>
                  </>
                  :
                   "" }
                </div>
              </td>

                </tr>
              );
            })}
          </tbody>
        </table>
      </div> : 
      <>
      <Loader></Loader>
      <label className="m-10 p-5 block text-black dark:text-white">
              Fetching Users Information
      </label>        
      </>
      }
    </div>
  );
};

export default DataTableUsers;
