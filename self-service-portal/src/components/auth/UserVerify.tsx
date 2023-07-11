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
import { serachUser, userInviteAccept, userInviteReject } from "../../api/UserService";
import { generateTokenUser, setUserPassword } from "../../api/KeycloakService";
import queryString from "query-string";
import { showError } from "../../utils/Error";

export default function UserVerify() {
  const dispatch = useDispatch()
  const participantToken = useSelector((state: RootState) => state.tokenReducer.participantToken);
  const [invitedBy, setInvitedBy] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [participantCode, setParticipantCode] = useState('');
  const [role, setRole] = useState('');
  const [password, setPassword] = useState('');
  const [showLoader, setShowLoader] = useState(false);
  const [userError, setUserError ] = useState(false);
  const [passError, setPassError ] = useState(false);
  const [pass1, setPass1] = useState('');
  const [pass2, setPass2] = useState('');
  const [pass1Error, setPass1Error] = useState(false);
  const [pass2Error, setPass2Error] = useState(false);
  const [sending, setSending] = useState(false);
  const [setpass, setShowPass] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [fullname, setName] = useState('');
  const [existingOwner, setExistingOwner] = useState(false);
  
  useEffect(() => {

    setShowPass(false);
    const jwtToken = _.get(queryString.parse(window.location.search),"jwt_token");
    setEmail(getPayload(jwtToken) != null ? getPayload(jwtToken).email : "");
    setInvitedBy(getPayload(jwtToken) != null ? getPayload(jwtToken).invited_by : "");
    setParticipantCode(getPayload(jwtToken) != null ? getPayload(jwtToken).participant_code : "");
    setRole(getPayload(jwtToken) != null ? getPayload(jwtToken).role : "");
    serachUser(getPayload(jwtToken).email).then((res: any) => {
      let osOwner = _.get(res, "data.users[0].osOwner");
      console.log("oswoner", osOwner);
      if (osOwner !== undefined){
        console.log("user is created")
        setExistingOwner(true);
      }else{
        setExistingOwner(false);
      } 
    })
    console.log("Set show pass",setpass, existingOwner);
  },[])

  const getPayload = (token: any) => {
    try {
      const decoded = token.split(".");
      const payload = JSON.parse(atob(decoded[1]));
      console.log('payload: ', payload)
      return payload;
    } catch (err) {
      console.error(err)
      showError("Invalid JWT Token ")
    }
  }

  const Verify = () => {
    const user = {
      "user_name" : fullname,
      "email" : email,
      "mobile" : phone ? phone : '9999999999',
      "tenant_roles" : [
        {"participant_code" : participantCode,
        "role": role}
      ],
      "created_by" : invitedBy
    };
    userInviteAccept(_.get(queryString.parse(window.location.search),"jwt_token"), user).then((res :any) => {
      setSubmitted(true);
      toast.success("You have successfully accepted the invite. Please login to continue");
      if(existingOwner){
          navigate("/onboarding/login");
        }else{
          toast.success("Thank you for accepting the invite");
          setShowPass(true);
        } 
      }).catch(err => {
      showError(_.get(err, 'response.data.error.message') || "Internal Server Error");
      setSubmitted(false);
      toast.error("Sorry! We could not process the request. Please try again or contact HCX team", {
        position: toast.POSITION.TOP_CENTER
    });
    })
  }


  const Reject = () => {
    const user = {
      "email" : email,
    };
    userInviteReject(_.get(queryString.parse(window.location.search),"jwt_token"), user).then((res :any) => {
      setSubmitted(true);
      toast.success("You have declined the invite");
      navigate("/onboarding/login");
    }).catch(err => {
      showError(_.get(err, 'response.data.error.message') || "Internal Server Error");
      setSubmitted(false);
      toast.error("Sorry! We could not process the request. Please try again or contact HCX team", {
        position: toast.POSITION.TOP_CENTER
    });
    })
  }

  const resetPassword = () => {
    console.log("we are here to set the password");
    if (pass1 == '' && pass2 == '') {
      setPass1Error(true);
      setPass2Error(true);
    } else {
      if (pass1 === pass2) {
        let osOwner;
        serachUser(email).then((res: any) => {
          console.log("user search in verify", res);
          osOwner = res["data"]["users"][0]["osOwner"];
          setUserPassword(osOwner[0], pass1).then((async function () {
            generateTokenUser(email, pass1).then((res: any) => {
              dispatch(addParticipantToken(res["access_token"]));
            })
            navigate("/onboarding/login");
            toast.success("User password created successfully. Please login to continue");
          })).catch(err => {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
              position: toast.POSITION.TOP_CENTER
            });
          }).finally(() => {
            setSending(false);
          })
        });
      } else {
        toast.error("Incorrect password", {
          position: toast.POSITION.TOP_CENTER
        });
      }
    }
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
                  {setpass == false ? 
                  <>
                  <p className="mb-6 font-semibold">Please acknowledge your user invite</p>
                  {/*Username input*/}
                  <div className="relative mb-2 flex flex-wrap">
                    <p className="py-2 w-2/6 font-semibold">Recipient Email Id :</p>
                    <p className="py-2 w-4/6 font-semibold">{email}</p>
                    {/* <input
                      type="email"
                      className={"w-5/6 h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline disabled:opacity-75"}
                      id="exampleFormControlInput1"
                      placeholder="Email Address"
                      value={email}
                      disabled
                    /> */}
                  </div>
                  <div className="relative mb-2 flex flex-wrap">
                  <p className="py-2 w-2/6 font-semibold">Inviter Email Id :</p>
                  <p className="py-2 w-4/6 font-semibold">{invitedBy}</p>
                  </div>
                  {/*Password input*/}
                  <div className="relative mb-2 flex flex-wrap">
                  <p className="py-2 w-2/6 font-semibold">Role Assigned:</p>
                  <p className="py-2 w-4/6 font-semibold">{role}</p>
                  </div>
                  <div className="relative mb-2 flex flex-wrap">
                  <p className="py-2 w-2/6 font-semibold">Inviter Participant Code :</p>
                  <p className="py-2 w-4/6 font-semibold">{participantCode}</p>
                  </div>
                  {!existingOwner ? <>
                  <div className="relative mb-2 flex flex-wrap">
                  <p className="py-2 mb-4 w-2/6 font-semibold">User Name :</p>
                    <input
                      type="tel"
                      className="w-4/6 h-10 px-3 mb-2 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline"
                      id="exampleFormControlInput12"
                      placeholder="Name"
                      onChange={(event) => {setName(event.target.value)}}
                    />
                  </div>
                  <div className="relative mb-4 flex flex-wrap">
                  <p className="py-2 mb-4 w-2/6 font-semibold">Phone Number:</p>
                    <input
                      type="number"
                      pattern="[0-9]*"
                      className="w-4/6 h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline"
                      id="exampleFormControlInput12"
                      placeholder="Phone Number"
                      onChange={(event) => {setPhone(event.target.value)}}
                    />
                  </div></> : null}
                  {/*Submit button*/}

                  <div className="flex flex-wrap -mx-3 my-6 px-3 justify-between">
            <button
              className="mb-3 bg-blue-700 inline-block w-1/4 rounded px-6 pb-2 pt-2.5 text-xs font-medium uppercase leading-normal text-white shadow-[0_4px_9px_-4px_rgba(0,0,0,0.2)] transition duration-150 ease-in-out hover:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] focus:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] focus:outline-none focus:ring-0 active:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)]"
              type="button"
              data-te-ripple-init=""
              data-te-ripple-color="light"
              onClick={() => Verify()}
            >
              Accept
            </button>
            <button
              className="mb-3 inline-block w-1/5 bg-grey-200 rounded px-6 pb-2 pt-2.5 text-xs font-medium uppercase leading-normal text-grey-900 shadow-[0_4px_9px_-4px_rgba(0,0,0,0.2)] transition duration-150 ease-in-out hover:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] focus:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] focus:outline-none focus:ring-0 active:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] border-2"
              type="button"
              data-te-ripple-init=""
              data-te-ripple-color="light"
              onClick={() => Reject()}>
                Decline
            </button>

          </div>
                  </>  :
                  <>
                  <p className="mb-3">Please create your password</p>
                      <div className="relative mb-4">
                    <input
                      type="password"
                      className={"w-full h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline" + (pass1Error ? " border-red-600" : "")}
                      id="exampleFormControlInput1"
                      placeholder="Password"
                      onChange={(event) => {setPass1(event.target.value); setPass1Error(false)}}
                      required
                    />
                  </div>
                  {/*Password input*/}
                  <div className="relative mb-1">
                    <input
                      type="password"
                      className={"w-full h-10 px-3 mb-1 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline" + (pass2Error ? " border-red-600" : "")}
                      id="exampleFormControlInput11"
                      placeholder="Password"
                      onChange={(event) => {setPass2(event.target.value); setPass2Error(false)}}
                      required
                    />
                  </div>
                  <p className="text-grey-900 text-xs italic mb-4">*Password should have min 8 characters with atleast one smallcase, uppercase, number and special character</p>
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
                      onClick={() => resetPassword()}
                    >
                      Create Password
                    </button>
                    {/*Forgot password link*/}
                    {/* <a href="#!">Forgot password?</a> */}
                  </div>
                  </>}
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

