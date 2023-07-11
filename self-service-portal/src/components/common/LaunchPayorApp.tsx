
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../store";
import { useAuthActions } from "../../recoil/actions/auth.actions";
import _ from "lodash";
import { toast } from "react-toastify";
import { getParticipant, getParticipantByCode } from "../../api/RegistryService";
import { addParticipantDetails } from "../../reducers/participant_details_reducer";
import { post } from "../../api/APIService";
import { reverifyLink, serachUser } from "../../api/UserService";
import { navigate } from "raviger";
import { addAppData } from "../../reducers/app_data";
import { addParticipantToken } from "../../reducers/token_reducer";

const LaunchPayorApp = () => {

  const dispatch = useDispatch();
  const appLink = process.env.REACT_APP_PAYOR_APP;
  console.log("process env react app", appLink, process.env.REACT_PAYOR_APP);
  const participantDetails: Object = useSelector((state: RootState) => state.participantDetailsReducer.participantDetails);
  const authToken = useSelector((state: RootState) => state.tokenReducer.participantToken);
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  const [email, setEmail] =useState('');
  const [pass, setPass] =useState('');
  const [code, setCode] =useState('');
  console.log("part details in dash", participantDetails, authToken);

  //const { login } = useAuthActions();
  useEffect(() => {
    getParticipant(_.get(appData,"username")).then(res => { 
        console.log("verifier details",res);
        const partData = res["data"]["participants"][0];
        console.log("part data in launch", partData, btoa(_.get(partData,"mock_payor.primary_email")));
        setEmail(_.get(partData,"mock_payor.primary_email") ? _.get(partData,"mock_payor.primary_email") : '');
        setPass(_.get(partData,"mock_payor.password") ? _.get(partData,"mock_payor.password") : '');
        setCode(_.get(partData,"mock_payor.participant_code") ? _.get(partData,"mock_payor.participant_code") : '');
  })
  }, []);


  const onSubmit =() => {
    if(email == "" && pass == ""){
      toast.error("You dont have a participant account to launch the app.")
    }else{
    window.open(`${appLink}?user_token=${authToken}&email=${btoa(email)}&password=${btoa(pass)}`);
    }
  }

  return (
    <div className="p-4 sm:ml-64">
      <div className="p-4 border-2 border-gray-200 border rounded-lg dark:border-gray-700 mt-14">
        <form className="w-full p-12">
        <div className="flex flex-wrap -mx-3 mt-2">
            <label
              className="block uppercase tracking-wide text-gray-700 text-s font-bold mb-2"
            >
              Payor App Launch Details
            </label>
          </div>
          <div className="flex flex-wrap -mx-3 mb-10 border-b-2 shadow-l shadow-bottom">
            <p className="text-gray-700 text-sm italic">
                Information below will be used to launch the Payor App. Please use the given participant code for the payor when making API calls as Provider.
            </p>
          </div>
          <div className="flex flex-wrap w-full px-3 mb-2">
            <label
              className="w-1/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              Participant Code :
            </label>
            <label
              className="w-5/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              {code}
            </label>
            {/* <p className="text-red-500 text-sm italic">Please fill out this field.</p> */}
          </div>
          <div className="flex flex-wrap w-full px-3 mb-2">
            <label
              className="w-1/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              Primary Email :
            </label>
            <label
              className="w-5/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              {email}
            </label>
            {/* <p className="text-red-500 text-sm italic">Please fill out this field.</p> */}
          </div>
          <div className="flex flex-wrap w-full px-3 mb-10">
            <label
              className="w-1/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              Client Secret :
            </label>
            <label
              className="w-5/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              {pass}
            </label>
            {/* <p className="text-red-500 text-sm italic">Please fill out this field.</p> */}
          </div>

          <div className="flex flex-wrap -mx-3 mb-6 justify-between">
            <button
              className="mb-3 inline-block w-1/4 rounded px-6 pb-2 pt-2.5 text-sm font-medium  leading-normal text-white shadow-[0_4px_9px_-4px_rgba(0,0,0,0.2)] transition duration-150 ease-in-out hover:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] focus:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] focus:outline-none focus:ring-0 active:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)]"
              type="button"
              data-te-ripple-init=""
              data-te-ripple-color="light"
              style={{
                background:
                  "linear-gradient(to right, #1C4DC3, #3632BE, #1D1991, #060347)"
              }}
              onClick={() => onSubmit()}
            >
              Launch Payer App
            </button>            
          </div>
        </form>

      </div>
    </div>
  );


}

export default LaunchPayorApp;