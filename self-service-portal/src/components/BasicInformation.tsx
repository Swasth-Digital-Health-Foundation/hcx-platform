import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { addAppData } from '../reducers/app_data';
import { RootState } from '../store';
import _ from 'lodash';

interface BasicInformationProps {
  onInfoSubmit: () => void;
}

const BasicInformation: React.FC<BasicInformationProps> = ({ onInfoSubmit }: BasicInformationProps) => {
  const dispatch = useDispatch();
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);

  const [email, setEmail] = useState(_.get(appData, "emailRegister") || "");
  const [phoneNumber, setPhoneNumber] = useState(_.get(appData, "phoneRegister") || "");
  const [org, setOrg] = useState(_.get(appData, "organizationNameRegister") || "");
  const [emailError, setEmailError] = useState(false);
  const [phoneError, setPhoneError] = useState(false);
  const [orgError, setOrgError] = useState(false);
  const [emailPopup, setEmailPopup] = useState(false);
  const [phonePopup, setPhonePopup] = useState(false);

  const onSubmit = () => {
    if (email === '' || phoneNumber === '' || org === '') {
      if (email === '') setEmailError(true);
      if (phoneNumber === '') setPhoneError(true);
      if (org === '') setOrgError(true);
    } else {
      onInfoSubmit();
    }
  };

  return (
    <>
      <div className="mb-4">
        <label className="mb-2.5 block font-medium text-black dark:text-white">
          <div className="flex items-center">
            <span>Email</span>
            <div className="relative inline-block ml-1">
              <span
                className="cursor-pointer"
                onClick={() => setEmailPopup(!emailPopup)}
                onMouseEnter={() => setEmailPopup(true)}
                onMouseLeave={() => setEmailPopup(false)}
              >
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5 inline">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z" />
                </svg>
              </span>
              {emailPopup && (
                <div className="absolute left-6 top-0 z-30 p-2 mt-1 text-xs text-inherit bg-gray-800 shadow-lg w-72 h-8">
                  Text saying why email address.
                </div>
              )}
            </div>
          </div>
        </label>
        <div className="relative">
          <input
            type="email"
            placeholder="Enter your email"
            className={"w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary" + (emailError ? " !border-danger" : "")}
            value={email}
            onChange={(event) => { setEmailError(false); setEmail(event.target.value); dispatch(addAppData({ "emailRegister": event.target.value })) }}
          />
        </div>
        {emailError ? <p className='text-danger italic'>* Please enter a valid email address</p> : null}
      </div>

      <div className="mb-4">
        <label className="mb-2.5 block font-medium text-black dark:text-white">
          <div className="flex items-center">
            <span>Phone</span>
            <div className="relative inline-block ml-1">
              <span
                className="cursor-pointer"
                onClick={() => setPhonePopup(!phonePopup)}
                onMouseEnter={() => setPhonePopup(true)}
                onMouseLeave={() => setPhonePopup(false)}
              >
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5 inline">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z" />
                </svg>
              </span>
              {phonePopup && (
                <div className="absolute left-6 top-0 z-30 p-2 mt-1  text-xs text-inherit bg-gray-800 shadow-lg w-72 h-8">
                  Text saying why phone number.
                </div>
              )}
            </div>
          </div>
        </label>
        <div className="relative">
          <input
            type="tel"
            placeholder="Enter your phone number"
            className={"w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary" + (phoneError ? " !border-danger" : "")}
            value={phoneNumber}
            onChange={(event) => { setPhoneError(false); setPhoneNumber(event.target.value); dispatch(addAppData({ "phoneRegister": event.target.value })) }}
          />
        </div>
        {phoneError ? <p className='text-danger italic'>* Please enter a valid phone number</p> : null}
      </div>

      <div className="mb-4">
        <label className="mb-2.5 block font-medium text-black dark:text-white">
          Organization
        </label>
        <div className="relative">
          <input
            type="text"
            placeholder="Enter your organization name"
            className={"w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary" + (orgError ? " !border-danger" : "")}
            value={org}
            onChange={(event) => { setOrgError(false); setOrg(event.target.value); dispatch(addAppData({ "organizationNameRegister": event.target.value })) }}
          />
        </div>
        {orgError ? <p className='text-danger italic'>* Please enter a valid organization name</p> : null}
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
  );
}

export default BasicInformation;
