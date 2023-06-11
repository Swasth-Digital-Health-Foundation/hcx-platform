import { useEffect, useState } from "react";
import { useAuthActions } from "../../recoil/actions/auth.actions";
import logo from "../../swasth_logo_1.jpg";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../store";
import _ from 'lodash';
import { toast } from "react-toastify";
import { post } from "../../api/APIService";
import { addParticipantToken } from "../../reducers/token_reducer";
import { getParticipant } from "../../api/RegistryService";
import { addParticipantDetails } from "../../reducers/participant_details_reducer";
import 'flowbite';
import { navigate } from "raviger";
import Sidebar from "../common/SideTab";
import ParticipantInfo from "../common/ParticipantInfo";

export default function Dashboard() {

  const dispatch = useDispatch()
  const participantDetails : Object = useSelector((state: RootState) => state.participantDetailsReducer.participantDetails);
  const authToken = useSelector((state: RootState) => state.tokenReducer.participantToken);  

  const handleChildData = (data: string) => {
    console.log("Side Bar clicked", data);
  };

  return (
<>
  <nav className="fixed top-0 z-50 w-full bg-white border-b border-gray-200 dark:bg-gray-800 dark:border-gray-700" 
                            style={{
                                background:
                                  "linear-gradient(to right, #1C4DC3, #3632BE, #1D1991, #060347)"
                              }}>
    <div className="px-3 py-3 lg:px-5 lg:pl-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center justify-start">
          <button
            data-drawer-target="logo-sidebar"
            data-drawer-toggle="logo-sidebar"
            aria-controls="logo-sidebar"
            type="button"
            className="inline-flex items-center p-2 text-sm text-gray-500 rounded-lg sm:hidden hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:text-gray-400 dark:hover:bg-gray-700 dark:focus:ring-gray-600"
          >
            <span className="sr-only">Open sidebar</span>
            <svg
              className="w-6 h-6"
              aria-hidden="true"
              fill="currentColor"
              viewBox="0 0 20 20"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                clipRule="evenodd"
                fillRule="evenodd"
                d="M2 4.75A.75.75 0 012.75 4h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 4.75zm0 10.5a.75.75 0 01.75-.75h7.5a.75.75 0 010 1.5h-7.5a.75.75 0 01-.75-.75zM2 10a.75.75 0 01.75-.75h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 10z"
              />
            </svg>
          </button>
          <a href="https://docs.hcxprotocol.io/" target="_blank" className="flex ml-2 md:mr-24">
            <img
              src={logo}
              className="h-8 mr-3"
              alt="Swasth"
            />
            <span className="self-center text-xl font-semibold sm:text-2xl whitespace-nowrap text-white ">
              Swasth HCX
            </span>
          </a>
        </div>
        <div className="flex items-center">
          <div className="flex items-center ml-3">
          <button className="flex items-center gap-2 px-4 py-2 text-white rounded-md focus:outline-none"
          onClick={() => navigate("/onboarding/login")}>
  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
    <path fill-rule="evenodd" d="M10 0a10 10 0 1 0 10 10A10 10 0 0 0 10 0zM7.707 5.293a1 1 0 1 1 1.414-1.414l4 4a1 1 0 0 1 0 1.414l-4 4a1 1 0 1 1-1.414-1.414L10.586 11H3a1 1 0 1 1 0-2h7.586l-2.879-2.879z" clip-rule="evenodd" />
  </svg>
  Logout
</button>

          </div>
        </div>
      </div>
    </div>
  </nav>
  <Sidebar onDataUpdate={handleChildData}></Sidebar>
  <ParticipantInfo></ParticipantInfo>
</>
  );
}
