import { useEffect, useState } from "react";
import { useAuthActions } from "../../recoil/actions/auth.actions";
import logo from "../../swasth_logo.png";
import {login} from '../../api/api';
import { error } from "console";
import { useSelector, useDispatch } from 'react-redux'
import { addParticipantToken } from '../../reducers/token_reducer';
import { RootState } from "../../store";
import { navigate } from 'raviger';
import { getParticipant } from "../../api/RegistryService";
import { addParticipantDetails } from "../../reducers/participant_details_reducer";
import { toast } from "react-toastify";
import _ from "lodash";

export default function Login() {
  const dispatch = useDispatch()
  const participantToken = useSelector((state: RootState) => state.tokenReducer.participantToken);

  
  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [showLoader, setShowLoader] = useState(false);
  const [userError, setUserError ] = useState(false);
  const [passError, setPassError ] = useState(false);

  useEffect(() => {
    console.log("participantToken ", participantToken);
  },[participantToken])

  const Signin = (username: string, password: string) => {
    if (username == "" || password == ""){
      if(username == "") setUserError(true);
      if(password == "") setPassError(true);
    }else{
    login({username,password}).then((res) => {
        console.log("res", res);
        dispatch(addParticipantToken(res["access_token"]));
        getParticipant(userName).then((res :any) => {
          console.log("we are in inside get par", res);
           dispatch(addParticipantDetails(res["data"]["participants"][0]));
        })
        navigate("/onboarding/dashboard");
    }).catch((error) => {
      toast.error("Unable to login. Please check the credentials" || "Internal Server Error", {
        position: toast.POSITION.TOP_CENTER
    });
    })}
  }



  return (
    
    <div className="flex flex-col items-center justify-center h-screen">

      { showLoader ? 
      <div className="fixed inset-0 bg-gray-300 bg-opacity-50 flex items-center justify-center z-50">
      <div role="status">
              <svg aria-hidden="true" className="inline w-8 h-8 mr-2 text-gray-200 animate-spin dark:text-gray-600 fill-blue-600" viewBox="0 0 100 101" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z" fill="currentColor"/>
                  <path d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z" fill="currentFill"/>
              </svg>
              <span className="sr-only">Loading...</span>
          </div>
      </div> : null }

      <div className="flex flex-row items-center justify-center w-full h-full">
    <div className="g-6 flex h-full flex-wrap items-center justify-center text-neutral-800 dark:text-neutral-200">
      <div className="w-11/12">
        <div className="block rounded-lg bg-white shadow-lg dark:bg-neutral-800">
          <div className="g-0 lg:flex lg:flex-wrap">
            {/* Left column container*/}
            <div className="px-4 md:px-0 lg:w-6/12">
              <div className="md:mx-6 md:p-12">
                {/*Logo*/}
                <div className="text-center">
                  <img
                    className="mx-auto w-48"
                    src={logo}
                    alt="logo"
                  />
                  <h4 className="mb-12 mt-1 pb-1 text-xl font-semibold">
                    HCX Onboarding
                  </h4>
                </div>
                <form>
                  <p className="mb-4">Please login to your account</p>
                  {/*Username input*/}
                  <div className="relative mb-4">
                    <input
                      type="email"
                      className={"w-full h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline" + (userError ? " border-red-600" : "") }
                      id="exampleFormControlInput1"
                      placeholder="Username"
                      onChange={(event) => {setUserName(event.target.value); setUserError(false)}}
                      required
                    />
                  </div>
                  {/*Password input*/}
                  <div className="relative mb-4">
                    <input
                      type="password"
                      className={"w-full h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline" + (passError ? " border-red-600" : "" )}
                      id="exampleFormControlInput11"
                      placeholder="Password"
                      onChange={(event) => {setPassword(event.target.value); setPassError(false)}}
                      required
                    />
                  </div>
                  {/*Submit button*/}
                  <div className="mb-12 pb-1 pt-1 text-center">
                    <button
                      className="mb-3 inline-block w-full rounded px-6 pb-2 pt-2.5 text-xs font-medium uppercase leading-normal text-white shadow-[0_4px_9px_-4px_rgba(0,0,0,0.2)] transition duration-150 ease-in-out hover:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] focus:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] focus:outline-none focus:ring-0 active:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)]"
                      type="button"
                      data-te-ripple-init=""
                      data-te-ripple-color="light"
                      style={{
                        background:
                          "linear-gradient(to right, #1C4DC3, #3632BE, #1D1991, #060347)"
                      }}
                      onClick={() => Signin(userName,password)}
                    >
                      Log in
                    </button>
                    {/*Forgot password link*/}
                    <a href="#!">Forgot password?</a>
                  </div>
                  {/*Register button*/}
                  <div className="flex items-center justify-between pb-6">
                    <p className="mb-0 mr-2">Don't have an account?</p>
                    <button
                      type="button"
                      className="inline-block rounded border-2 border-blue-500 px-6 pb-[6px] pt-2 text-xs font-medium uppercase leading-normal text-blue-500 transition duration-150 ease-in-out hover:border-blue-600 hover:bg-neutral-500 hover:bg-opacity-10 hover:text-blue-600 focus:border-blue-600 focus:text-blue-600 focus:outline-none focus:ring-0 active:border-blue-700 active:text-blue-700 dark:hover:bg-neutral-100 dark:hover:bg-opacity-10"
                      data-te-ripple-init=""
                      data-te-ripple-color="light"
                      onClick={() => navigate("/onboarding/register")}
                    >
                      Register
                    </button>
                  </div>
                </form>
              </div>
            </div>
            {/* Right column container with background and description*/}
            <div
              className="flex items-center rounded-b-lg lg:w-6/12 lg:rounded-r-lg lg:rounded-bl-none"
              style={{
                background:
                  "linear-gradient(to right, #1C4DC3, #3632BE, #1D1991, #060347)"
              }}
            >
              <div className="px-4 py-6 text-white md:mx-6 md:p-12">
                <h4 className="mb-6 text-xl font-semibold">
                Health Claims Exchange
                </h4>
                <p className="text-sm">
                Health Claims Data Exchange (HCX) is an ambitious open source project that aims to define interoperable protocol specifications
                to enable a multi-party exchange of health claims information. The protocol is defined for a specialized use case of exchanging 
                health claims-related information between relevant actors.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
  );
}
