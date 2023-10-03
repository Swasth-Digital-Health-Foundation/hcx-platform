
import React, { useState, useRef, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { addAppData } from '../reducers/app_data';
import { RootState } from '../store';
import _ from 'lodash';


interface BasicInformationProps {
  onInfoSubmit : () => void;
}

const BasicInformation: React.FC<BasicInformationProps> = ({onInfoSubmit}:BasicInformationProps) => {
  const dispatch = useDispatch();
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);

  const [email, setEmail] = useState(_.get(appData,"emailRegister") || "");
  const [phoneNumber, setPhoneNumber] = useState(_.get(appData,"phoneRegister") || "");
  const [org, setOrg] = useState(_.get(appData,"organizationNameRegister") || "");
  const [emailError, setEmailError] = useState(false);
  const [phoneError, setPhoneError] = useState(false);
  const [orgError, setOrgError] = useState(false);
  
  const onSubmit = () => {
    if (email == '' || phoneNumber == '' || org == '') {
      if (email == '') setEmailError(true);
      if (phoneNumber == '') setPhoneError(true);
      if (org == '') setOrgError(true);
    } else {
    onInfoSubmit();
    }
  }

  return (
    <>
      
      <div className="mb-4">
        <label className="mb-2.5 block font-medium text-black dark:text-white">
          Email
        </label>
        <div className="relative">
          <input
            type="email"
            placeholder="Enter your email"
            className={"w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary" + (emailError ? " !border-danger" : "")}
            value={email}
            onChange={(event) => { setEmailError(false); setEmail(event.target.value); dispatch(addAppData({ "emailRegister": event.target.value })) }}
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
                  d="M19.2516 3.30005H2.75156C1.58281 3.30005 0.585938 4.26255 0.585938 5.46567V16.6032C0.585938 17.7719 1.54844 18.7688 2.75156 18.7688H19.2516C20.4203 18.7688 21.4172 17.8063 21.4172 16.6032V5.4313C21.4172 4.26255 20.4203 3.30005 19.2516 3.30005ZM19.2516 4.84692C19.2859 4.84692 19.3203 4.84692 19.3547 4.84692L11.0016 10.2094L2.64844 4.84692C2.68281 4.84692 2.71719 4.84692 2.75156 4.84692H19.2516ZM19.2516 17.1532H2.75156C2.40781 17.1532 2.13281 16.8782 2.13281 16.5344V6.35942L10.1766 11.5157C10.4172 11.6875 10.6922 11.7563 10.9672 11.7563C11.2422 11.7563 11.5172 11.6875 11.7578 11.5157L19.8016 6.35942V16.5688C19.8703 16.9125 19.5953 17.1532 19.2516 17.1532Z"
                  fill=""
                />
              </g>
            </svg>
          </span>
        </div>
        {emailError ? <p className='text-danger italic'>* Please enter valid email address</p> : null }
      </div>

      <div className="mb-4">
        <label className="mb-2.5 block font-medium text-black dark:text-white">
          Phone
        </label>
        <div className="relative">
          <input
            type="tel"
            placeholder="Enter your phone number"
            className={"w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"  + (phoneError ? " !border-danger" : "")}
            value={phoneNumber}
            onChange={(event) => { setPhoneError(false); setPhoneNumber(event.target.value); dispatch(addAppData({ "phoneRegister": event.target.value })) }}
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
                  d="M405.333,298.667c-38.4,0-76.8-14.933-106.667-44.8c-17.067-17.067-42.667-17.067-59.733,0   c-29.867,29.867-68.267,44.8-106.667,44.8c-64,0-123.733-34.133-170.667-85.333C8.533,189.867,0,162.133,0,128   C0,57.6,57.6,0,128,0c34.133,0,61.867,8.533,84.267,25.6c12.8,8.533,25.6,17.067,38.4,25.6c10.667,6.4,25.6,6.4,34.133,0   c12.8-8.533,25.6-17.067,38.4-25.6c22.4-17.067,50.133-25.6,81.067-25.6c70.4,0,128,57.6,128,128   c0,34.133-8.533,61.867-25.6,84.267C440.533,264.533,419.2,298.667,405.333,298.667z"
                />
                <path
                  d="M13.2352 11.0687H8.76641C5.08828 11.0687 2.09766 14.0937 2.09766 17.7719V20.625C2.09766 21.0375 2.44141 21.4156 2.88828 21.4156C3.33516 21.4156 3.67891 21.0719 3.67891 20.625V17.7719C3.67891 14.9531 5.98203 12.6156 8.83516 12.6156H13.2695C16.0883 12.6156 18.4258 14.9187 18.4258 17.7719V20.625C18.4258 21.0375 18.7695 21.4156 19.2164 21.4156C19.6633 21.4156 20.007 21.0719 20.007 20.625V17.7719C19.9039 14.0937 16.9133 11.0687 13.2352 11.0687Z"
                  fill=""
                />
              </g>
            </svg>
          </span>
        </div>
        {phoneError ? <p className='text-danger italic'>* Please enter valid phone number</p> : null }
      </div>
      <div className="mb-4">
        <label className="mb-2.5 block font-medium text-black dark:text-white">
          Organization
        </label>
        <div className="relative">
          <input
            type="text"
            placeholder="Enter your organization name"
            className={"w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"  + (orgError ? " !border-danger" : "")}
            value={org}
            onChange={(event) => { setOrgError(false); setOrg(event.target.value); dispatch(addAppData({ "organizationNameRegister": event.target.value })) }}
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
        {orgError ? <p className='text-danger italic'>* Please enter valid organization name</p> : null }
      </div>

      <div className="mb-5">
        <input
          type="submit"
          value="Next"
          className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
          onClick={(event) => { event.preventDefault(); onSubmit() }}
        />
      </div>
    </>
  )
}

export default BasicInformation;