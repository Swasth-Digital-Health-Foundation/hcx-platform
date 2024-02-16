import React, { useState, useRef, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { addAppData } from '../reducers/app_data';
import { RootState } from '../store';
import { userInvite } from '../api/UserService';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import _ from 'lodash';


const InviteUserRegister: React.FC = () => {
    const participantDetails: Object = useSelector((state: RootState) => state.participantDetailsReducer.participantDetails);
    const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const [userDetials, setUserDetails] = useState([{ "email": "", "role": "admin" }])

    const addAnotherRow = () => {
        userDetials.push({ "email": "", "role": "" })
        setUserDetails(userDetials.map((value, index) => { return value }));
    }

    const updateCreateUserData = (value: string, index: number, field: string) => {
        _.update(userDetials[index], field, function (n) { return value });
        setUserDetails(userDetials.map((value, index) => { return value }));
    }


    const removeRow = (index: any) => {
        if (userDetials.length > 1) {
            if (index > -1) { // only splice array when item is found
                userDetials.splice(index, 1); // 2nd parameter means remove one item only
            }
            setUserDetails(userDetials.map((value, index) => { return value }));
        }
    }

    const inviteUsers = () => {
        userDetials.map((value, index) => {
            console.log("values", value);
            if (value.email !== "") {
                userInvite({ "email": value.email, "participant_code": _.get(participantDetails, "participant_code"), "role": value.role, "invited_by": _.get(appData, "emailRegister") }).then(res => {
                    toast.success(`${value.email} has been successfully invited`);
                }).catch(err => {
                    toast.error(`${value.email} could not be invited. ` + _.get(err, 'response.data.error.message') || "Internal Server Error",);
                })
            }
        });
        setUserDetails([{ "email": "", "role": "admin" }]);
    }

    return (
        <>
            {userDetials.map((value, index) => {
                return <>
                    <div className="mb-4">
                        <label className="mb-2.5 block font-medium text-black dark:text-white">
                            Email
                        </label>
                        <div className="relative">
                            <input
                                type="text"
                                placeholder="Enter email"
                                value={value.email}
                                className="w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                                onChange={(event) => updateCreateUserData(event.target.value, index, "email")}
                            />

                            <span className="absolute right-4 top-4">
                                <svg
                                    className="fill-current"
                                    width="22"
                                    height="22"
                                    viewBox="0 0 22 22"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <g opacity="0.5">
                                        <path
                                            d="M11.0008 9.52185C13.5445 9.52185 15.607 7.5281 15.607 5.0531C15.607 2.5781 13.5445 0.584351 11.0008 0.584351C8.45703 0.584351 6.39453 2.5781 6.39453 5.0531C6.39453 7.5281 8.45703 9.52185 11.0008 9.52185ZM11.0008 2.1656C12.6852 2.1656 14.0602 3.47185 14.0602 5.08748C14.0602 6.7031 12.6852 8.00935 11.0008 8.00935C9.31641 8.00935 7.94141 6.7031 7.94141 5.08748C7.94141 3.47185 9.31641 2.1656 11.0008 2.1656Z"
                                            fill=""
                                        />
                                        <path
                                            d="M13.2352 11.0687H8.76641C5.08828 11.0687 2.09766 14.0937 2.09766 17.7719V20.625C2.09766 21.0375 2.44141 21.4156 2.88828 21.4156C3.33516 21.4156 3.67891 21.0719 3.67891 20.625V17.7719C3.67891 14.9531 5.98203 12.6156 8.83516 12.6156H13.2695C16.0883 12.6156 18.4258 14.9187 18.4258 17.7719V20.625C18.4258 21.0375 18.7695 21.4156 19.2164 21.4156C19.6633 21.4156 20.007 21.0719 20.007 20.625V17.7719C19.9039 14.0937 16.9133 11.0687 13.2352 11.0687Z"
                                            fill=""
                                        />
                                    </g>
                                </svg>
                            </span>
                        </div>
                    </div>
                    <div className="mb-4">
                        <label className="mb-2.5 block font-medium text-black dark:text-white">
                            Role
                        </label>
                        <div className="relative z-20 bg-transparent dark:bg-form-input">
                            <select className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-3 px-5 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                                onChange={(event) => { updateCreateUserData(event.target.value, index, "role") }}>
                                <option value="admin">Admin</option>
                                <option value="config-manager">Config-manager</option>
                                <option value="viewer">Viewer</option>
                            </select>
                            <span className="absolute top-1/2 right-4 z-30 -translate-y-1/2">
                                <svg
                                    className="fill-current"
                                    width="24"
                                    height="24"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <g opacity="0.8">
                                        <path
                                            fillRule="evenodd"
                                            clipRule="evenodd"
                                            d="M5.29289 8.29289C5.68342 7.90237 6.31658 7.90237 6.70711 8.29289L12 13.5858L17.2929 8.29289C17.6834 7.90237 18.3166 7.90237 18.7071 8.29289C19.0976 8.68342 19.0976 9.31658 18.7071 9.70711L12.7071 15.7071C12.3166 16.0976 11.6834 16.0976 11.2929 15.7071L5.29289 9.70711C4.90237 9.31658 4.90237 8.68342 5.29289 8.29289Z"
                                            fill=""
                                        ></path>
                                    </g>
                                </svg>
                            </span>
                            {index !== 0 ?
                                <div className="flex items-center place-content-end">
                                    <a href="#" className="text-blue-700 text-xs underline" onClick={(event) => { event.preventDefault(); removeRow(index) }}>Remove</a>
                                </div> : null}
                        </div>
                    </div>
                </>
            })}

            <div className="my-5 flex justify-between">
                <input
                    type="submit"
                    value="Add Another User"
                    className="w-1/3 cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
                    onClick={(event) => { event.preventDefault();addAnotherRow(); }}
                />
                <input
                    type="submit"
                    value="Invite"
                    className="w-1/3 cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
                    onClick={(event) => { event.preventDefault(); inviteUsers(); }}
                />
            </div>
            <div className="flex mb-5 justify-end">
            <a href="/onboarding/profile" className="text-m text-primary underline">
                    Skip to Home
                  </a>
            </div>
        </>
    )
}

export default InviteUserRegister;


