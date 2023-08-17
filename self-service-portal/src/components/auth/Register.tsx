import { ChangeEvent, useEffect, useState } from "react";
import { useAuthActions } from "../../recoil/actions/auth.actions";
import logo from "../../swasth_logo.png";
import { useQuery } from "../../api/QueryService";
import { post } from "../../api/APIService";
import * as _ from 'lodash';
import { toast } from "react-toastify";
import { getParticipant, getParticipantSearch } from "../../api/RegistryService";
import { generateTokenUser, setPassword, setUserPassword } from "../../api/KeycloakService";
import { useHistory } from "react-router-dom";
import queryString from 'query-string';
import { navigate } from "raviger";
import { useDispatch, useSelector } from "react-redux";
import { addParticipantDetails } from "../../reducers/participant_details_reducer";
import { login } from "../../api/api";
import { addParticipantToken } from "../../reducers/token_reducer";
import TermsOfUse from "../common/TermsOfUse";
import { addAppData } from "../../reducers/app_data";
import CreateUser from "../common/CreateUser";
import { RootState } from "../../store";
import { serachUser, userInvite } from "../../api/UserService";
import Stepper from "../common/Stepper";

type Payor = {
  participant_code: string,
  participant_name: string
}

export default function Register() {

  const dispatch = useDispatch();
  const mockPayorCode = process.env.REACT_APP_MOCK_PAYOR_CODE;
  const tag = process.env.REACT_APP_PARTICIPANT_TAG;
  const env = process.env.REACT_APP_ENV;


  //const { login } = useAuthActions();
  //const query = useQuery();

  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [org, setOrg] = useState('');
  const [radioRole, setRadioRole] = useState('payor');
  const [radioOnboard, setRadioOnboard] = useState('');
  const [fields, setFields] = useState([]);
  const [isJWTPresent, setIsJWTPresent] = useState(false);
  const [invalidApplicantCode, setInvalidApplicantCode] = useState(false);
  const [payor, setPayor] = useState<Payor>();
  const [sending, setSending] = useState(false);
  const [applicantCode, setApplicantCode] = useState("1299");
  const [fetchResponse, setFetchResponse] = useState(false);
  const [payorList, setPayorList] = useState<Payor[]>();
  const [showPassword, setShowPassword] = useState(false);
  const [pass1, setPass1] = useState('');
  const [pass2, setPass2] = useState('');
  const [pass1Error, setPass1Error] = useState(false);
  const [pass2Error, setPass2Error] = useState(false);
  const [showLoader, setShowLoader] = useState(false);
  const [checkBasic, setCheckBasic] = useState(false);
  const [checkVerify, setCheckVerify] = useState(false);
  const [checkResetPass, setCheckResetPass] = useState(false);
  const [showVerify, setShowVerify] = useState(false);
  const [isEnvStaging, setIsEnvStaging] = useState(true);
  const [emailError, setEmailError] = useState(false);
  const [phoneError, setPhoneError] = useState(false);
  const [orgError, setOrgError] = useState(false);
  const [applicantError, setApplicantError] = useState(false);
  const [showUserCreate, setShowUserCreate] = useState(false);

  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  const participantDetails: Object = useSelector((state: RootState) => state.participantDetailsReducer.participantDetails);
  console.log("appData", appData, participantDetails);


  useEffect(() => {
    dispatch(addAppData({ "sidebar": "Profile" }));
    const jwtToken = _.get(queryString.parse(window.location.search), "jwt_token");
    console.log("env ", env);
    if (env !== "staging" && env !== "dev") {
      setIsEnvStaging(false);
    }
    setIsJWTPresent(jwtToken ? true : false);


    if (_.size(_.keys(jwtToken)) != 0) {
      getParticipantDetails();
    }

    (async () => {
      try {
        const participants = await getParticipantSearch({}).then(response => response.data.participants || []);
        const participantNames = participants.map((participant: { participant_name: any; }) => ({ value: participant.participant_name, ...participant }))
        setPayorList(participantNames)
        console.log("payor_list", participantNames);
      } catch (error) {
        setPayorList([])
      }
    })()
  }, []);

  const [userDetials, setUserDetails] = useState([{ "email": "", "role": "admin" }])

  const addAnotherRow = () => {
    console.log("i came here", userDetials);
    userDetials.push({ "email": "", "role": "" })
    setUserDetails(userDetials.map((value, index) => { return value }));
  }

  const updateCreateUserData = (value: string, index: number, field: string) => {
    _.update(userDetials[index], field, function (n) { return value });
    setUserDetails(userDetials.map((value, index) => { return value }));
    console.log("user details", userDetials);
  }


  const removeRow = (index: any) => {
    if (userDetials.length > 1) {
      const val = userDetials.indexOf(index);
      if (index > -1) { // only splice array when item is found
        userDetials.splice(index, 1); // 2nd parameter means remove one item only
      }
      setUserDetails(userDetials.map((value, index) => { return value }));
    }
  }

  const setShowTerms = (event: ChangeEvent<HTMLInputElement>) => {
    console.log("event.target.value", event.target.checked);
    dispatch(addAppData({ "showTerms": event.target.checked }));
    dispatch(addAppData({ "termsAccepted": event.target.checked }));
  }

  const inviteUsers = () => {
    userDetials.map((value, index) => {
      console.log("values", value);
      if (value.email !== "") {
        userInvite({ "email": value.email, "participant_code": _.get(participantDetails, "participant_code"), "role": value.role, "invited_by": email }).then(res => {
          toast.success(`${value.email} has been successfully invited`);
        }).catch(err => {
          toast.error(`${value.email} could not be invited. ` + _.get(err, 'response.data.error.message') || "Internal Server Error",);
        })
      }
    });
    toast.success("Users have been successfully invited");
    setUserDetails([{ "email": "", "role": "admin" }]);
    navigate("/onboarding/dashboard");
  }
  const resetPassword = () => {
    console.log("we are here to set the password");
    if (pass1 == '' && pass2 == '') {
      setPass1Error(true);
      setPass2Error(true);
    } else {
      if (pass1 === pass2) {
        let osOwner;
        setCheckResetPass(true);

        serachUser(email).then((res: any) => {
          osOwner = res["data"]["users"][0]["osOwner"];
          setUserPassword(osOwner[0], pass1).then((async function () {
            generateTokenUser(email, pass1).then((res: any) => {
              dispatch(addParticipantToken(res));
            })
            //navigate("/onboarding/dashboard");
            setShowUserCreate(true);
          })).catch(err => {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
              position: toast.POSITION.TOP_CENTER
            });
          }).finally(() => {
            setSending(false);
          })
        });

        getParticipant(email).then((res: any) => {
          console.log("we are in inside get par", res);
          osOwner = res["data"]["participants"][0]["osOwner"];
          dispatch(addParticipantDetails(res["data"]["participants"][0]));
        })
      } else {
        toast.error("Incorrect password", {
          position: toast.POSITION.TOP_CENTER
        });
      }
    }
  }



  const getParticipantDetails = () => {
    setSending(true)
    let payload;
    if (applicantCode == "" || applicantCode == "1299") {
      setApplicantError(true);
    } else {

      if (applicantCode && payor) {
        payload = { "applicant_code": applicantCode, "verifier_code": payor.participant_code }
      } else {
        payload = { "verification_token": _.get(queryString.parse(window.location.search), "jwt_token") }
      }
      post("/applicant/getinfo", JSON.stringify(payload)).then((data => {
        let respBody = data.data;

        const additionalFields = respBody.additionalVerification || [];
        // if (additionalFields.length != 0) {
        //     for (let i = 0; i < additionalFields.length; i++) {
        //         setFields((fields) => [...fields, { id: fields.length + 1, name: additionalFields[i].name, label: additionalFields[i].label, pattenr: additionalFields[i].pattern }])
        //     }
        // }

        //setDetails(_.get(respBody, 'applicant_name') || "", _.get(respBody, 'email') || "", _.get(respBody, 'mobile') || "")
        setOrg(_.get(respBody, 'applicant_name') || "");
        setEmail(_.get(respBody, 'email') || "");
        setPhoneNumber(_.get(respBody, 'mobile') || "");
        setFetchResponse(true);
        setCheckBasic(true);
        console.log("get info done", respBody);
        setRadioOnboard('');
      })).catch((err => {
        console.error(err)
        let errMsg = _.get(err, 'response.data.error.message')
        if (typeof errMsg === 'string' && errMsg.includes('UnknownHostException')) {
          //setFormErrors({ applicant_code: 'Payor system in unavailable, Please try later!' });
        } else {
          toast.error(errMsg || "Internal Server Error", {
            position: toast.POSITION.TOP_CENTER
          });
        }
        setInvalidApplicantCode(true);
        setFetchResponse(true);
      })).finally(() => {
        setSending(false)
      })
    }
  }






  const getPayor = (participantName: string) => {
    const participant = payorList?.find(participant => participant.participant_name === participantName);
    console.log("participant", participant);
    if (participant) {
      setPayor(participant)
    }
  }
  const onSubmit = () => {
    //setSending(true)
    const jwtToken = _.get(queryString.parse(window.location.search), "jwt_token");
    let formData: any[];

    // if (fields.length != 0) {
    //     fields.forEach(function (field) {
    //         field.value = data[field.name];
    //         delete field.id;
    //     });
    // }
    if (email == '' || phoneNumber == '' || org == '') {
      if (email == '') setEmailError(true);
      if (phoneNumber == '') setPhoneError(true);
      if (org == '') setOrgError(true);
    } else {


      if (isJWTPresent) {
        formData = [{ "type": "onboard-through-jwt", "jwt": jwtToken, additionalVerification: fields, "participant": { "participant_name": org, "primary_email": email, "primary_mobile": phoneNumber, "roles": ["provider"] } }];
      } else if (radioRole === 'payor') {
        formData = [{ "participant": { "participant_name": org, "primary_email": email, "primary_mobile": phoneNumber, "roles": [radioRole] } }];
      } else if (payor != null && !invalidApplicantCode) {
        formData = [{ "type": "onboard-through-verifier", "verifier_code": payor.participant_code, "applicant_code": applicantCode, additionalVerification: fields, "participant": { "participant_name": org, "primary_email": email, "primary_mobile": phoneNumber, "roles": ["provider"] } }];
      } else if (radioOnboard == "mock_payor") {
        console.log("in mock payor register");
        formData = [{ "type": "onboard-through-verifier", "verifier_code": mockPayorCode, "applicant_code": applicantCode, additionalVerification: fields, "participant": { "participant_name": org, "primary_email": email, "primary_mobile": phoneNumber, "roles": ["provider"] } }];
      } else {
        console.log("i came in else");
        formData = [];
      }
      if (tag) {
        formData[0].participant['tag'] = [tag]
      }
      setShowLoader(true);
      post("/participant/verify", JSON.stringify(formData))
        .then((data => {
          //setState({ ...formState, ...(formData[0]), ...{ "participant_code": _.get(data, 'data.result.participant_code'), "verifier_code": payor.participant_code, "identity_verification": _.get(data, 'data.result.identity_verification') } })
          setTimeout(() => {
            dispatch(addAppData({ username: email }));
            console.log("data in register", data);
              if(data["data"]["result"]["is_user_exists"]){
                navigate("/onboarding/dashboard")
              }else{
                setShowPassword(true);
              }
            
            setShowLoader(false);
            setCheckBasic(true);
            setCheckVerify(true);
          }, 500);
        })).catch(err => {
          if (_.get(err, 'response.data.error.message') && _.get(err, 'response.data.error.message') == "Username already invited / registered for Organisation") {
            toast.error('This email address already exists');
            setShowLoader(false);
          } else {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
              position: toast.POSITION.TOP_CENTER
            });
            setShowLoader(false);
          }
        }).finally(() => {
          setTimeout(() => {
            setSending(false)
          }, 500);
        })
    }
  }

  return (
    <div className="flex flex-col items-center justify-center h-screen">
      <TermsOfUse></TermsOfUse>
      {showLoader ?
        <div className="fixed inset-0 bg-gray-300 bg-opacity-50 flex items-center justify-center z-50">
          <div role="status">
            <svg aria-hidden="true" className="inline w-8 h-8 mr-2 text-gray-200 animate-spin dark:text-gray-600 fill-blue-600" viewBox="0 0 100 101" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z" fill="currentColor" />
              <path d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z" fill="currentFill" />
            </svg>
            <span className="sr-only">Loading...</span>
          </div>
        </div> : null}
      <div className="flex flex-row items-center justify-center w-full h-full">
        <div className="g-6 flex w-full h-full flex-wrap items-center justify-center text-neutral-800 dark:text-neutral-200">
          <div className="w-11/12">

            <div className="block rounded-lg bg-white shadow-lg dark:bg-neutral-800">


              <div className="g-0 lg:flex lg:flex-wrap">

                {/* Left column container*/}
                {showUserCreate ?
                  <div className="px-4 md:px-0 lg:w-6/12">
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
                    <div>
                      {/* Modal header */}
                      <div className="flex items-start p-6">
                        {/* <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 inline-block mr-1 border rounded-full border-green-500" fill="none" viewBox="0 0 24 24" stroke="green">
                         <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                       </svg> */}
                        <h3 className="text-l font-semibold text-grey-700 dark:text-white">
                          Congratulations! Your onboarding process is initiated. You can invite users to manage your organization
                        </h3>
                      </div>
                      <form className="w-full p-12">
                        {userDetials.map((value, index) => {
                          return <>

                            <div className="flex flex-wrap -mx-3 mb-6">
                              <div className="w-full md:w-1/2 px-3 mb-6 md:mb-0">
                                <label
                                  className="label-primary mb-2"
                                  htmlFor="grid-first-name"
                                >
                                  Email Address
                                </label>
                                <input
                                  className="input-primary"
                                  id="grid-first-name"
                                  type="text"
                                  placeholder="Email Address"
                                  value={value.email}
                                  onChange={(event) => updateCreateUserData(event.target.value, index, "email")}
                                />
                              </div>
                              <div className="w-full md:w-1/2 px-3 mb-6 md:mb-0">
                                <label
                                  className="label-primary mb-2"
                                  htmlFor="grid-last-name"
                                >
                                  Role
                                </label>
                                <select id="payordropdown"
                                  value={value.role}
                                  onChange={(event) => { updateCreateUserData(event.target.value, index, "role") }}
                                  className="input-primary" >
                                  <option value="admin">Admin</option>
                                  <option value="config-manager">Config-manager</option>
                                  <option value="viewer">Viewer</option>
                                </select>
                                {index !== 0 ?
                                  <div className="flex items-center place-content-end">
                                    <a href="#" className="text-blue-700 text-xs underline" onClick={(event) => { event.preventDefault(); removeRow(index) }}>Remove</a>
                                  </div> : null}

                              </div>

                            </div></>
                        })}

                        <div className="flex items-center justify-between -mx-3 mb-6 p-3">
                          <button
                            type="button"
                            className="button-secondary"
                            data-te-ripple-init=""
                            data-te-ripple-color="light"
                            onClick={() => addAnotherRow()}
                          >
                            Add Another User
                          </button>
                          <button
                            type="button"
                            className="button-secondary"
                            data-te-ripple-init=""
                            data-te-ripple-color="light"
                            onClick={() => { inviteUsers(); }}
                          >
                            Invite
                          </button>
                        </div>
                      </form>
                      {/* Modal footer */}
                      <div className="flex items-center p-6 justify-between space-x-2 border-t border-gray-200 rounded-b dark:border-gray-600">
                        <h5 className="text-l font-semibold text-gray-900 dark:text-white">
                          You may also invite users after onboarding completion. Click on 'Skip' button to go to participant profile.
                        </h5>
                        <button
                          data-modal-hide="defaultModal"
                          type="button"
                          className="button-secondary"
                          onClick={() => navigate("/onboarding/dashboard")}
                        >
                          Skip
                        </button>
                      </div>
                    </div>
                  </div>
                  :
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
                      {showPassword == false ?
                        <form>
                          <p className="mb-3 font-medium">Please create your account</p>
                          {/* Role Selection   */}
                          <div className="flex justify-left relative" role="radiogroup">
                            <label
                              className="label-primary mb-3"
                            >
                              Select Role :&nbsp;&nbsp;
                            </label>
                            {/*First radio*/}
                            <div className="flex mb-3">
                              <div className="flex items-center mr-4">
                                <input id="inline-radio-1" type="radio" defaultValue="payor" name="inline-radio-group" className="radio-primary"
                                  onClick={() => { setRadioRole('payor'); setRadioOnboard('') }} defaultChecked></input>
                                <label htmlFor="inline-radio-1" className="label-primary ml-2">Payor</label>
                              </div>
                              <div className="flex items-center mr-4">
                                <input id="inline-2-radio-1" type="radio" defaultValue="provider" name="inline-radio-group" className="radio-primary"
                                  onClick={() => { setRadioRole('provider'); isEnvStaging ? setRadioOnboard("mock_payor") : setRadioOnboard("actual_payor"); isEnvStaging ? setShowVerify(false) : setShowVerify(true) }} ></input>
                                <label htmlFor="inline-2-radio-1" className="label-primary ml-2">Provider</label>
                              </div>

                            </div>

                          </div>

                          {/* Onboard through Selection   */}
                          {radioRole == "provider" && isEnvStaging ? <div className="flex justify-left relative mb-3" role="radiogroup">
                            <label
                              className="label-primary mb-3"
                            >
                              Onboard through :&nbsp;&nbsp;
                            </label>
                            {/*First radio*/}
                            <div className="flex mb-3">
                              <div className="flex items-center mr-4">
                                <input id="inline-radio-2" type="radio" defaultValue="mock_payor" name="inline-radio-group1" className="radio-primary"
                                  onClick={() => { setRadioOnboard('mock_payor'); setShowVerify(false) }} defaultChecked></input>
                                <label htmlFor="inline-radio-2" className="ml-2 label-primary">Mock Payor</label>
                              </div>
                              <div className="flex items-center mr-4">
                                <input id="inline-2-radio-2" type="radio" defaultValue="actual_payor" name="inline-radio-group1" className="radio-primary"
                                  onClick={() => { setRadioOnboard('actual_payor'); setShowVerify(true) }} ></input>
                                <label htmlFor="inline-2-radio-2" className="ml-2 label-primary">Actual Payor</label>
                              </div>

                            </div>

                          </div> : ""}

                          {Stepper(checkBasic,radioRole,showVerify,checkVerify,checkResetPass)}

                          {radioOnboard !== "actual_payor" ?
                            <>
                              <div className="relative mb-4">
                                <input
                                  type="email"
                                  className={"input-primary" + (emailError ? " border-red-600" : "")}
                                  id="exampleFormControlInput1"
                                  placeholder="Email"
                                  onChange={(event) => { setEmail(event.target.value); setEmailError(false) }}
                                  value={email}
                                />
                              </div>
                              <div className="relative mb-4">
                                <input
                                  type="number"
                                  pattern="[0-9]*"
                                  className={"input-primary" + (phoneError ? " border-red-600" : "")}
                                  id="exampleFormControlInput11"
                                  placeholder="Phone Number"
                                  onChange={(event) => { setPhoneNumber(event.target.value); setPhoneError(false) }}
                                  value={phoneNumber}
                                />
                              </div>
                              <div className="relative mb-4">
                                <input
                                  type="text"
                                  className={"input-primary" + (orgError ? " border-red-600" : "")}
                                  id="exampleFormControlInput12"
                                  placeholder="Organization"
                                  onChange={(event) => { setOrg(event.target.value); setOrgError(false) }}
                                  value={org}
                                />
                              </div> </> : null}
                          {radioRole == "provider" && (radioOnboard == "actual_payor" || isEnvStaging == false) ? <div className="relative mb-4">
                            <select id="payordropdown"
                              className="input-primary"
                              onChange={(event) => getPayor(event.target.value)}>
                              <option selected value="1">Select Payor</option>
                              {payorList !== undefined ? payorList.map((value, index) => {
                                return <option value={value.participant_name}>{value.participant_name}</option>
                              }) : null}
                            </select>
                          </div> : null}
                          {radioOnboard == "actual_payor" ?
                            <>
                              <div className="relative mb-4">
                                <input
                                  type="text"
                                  className={"input-primary" + (applicantError ? " border-red-600" : "")}
                                  id="exampleFormControlInput12"
                                  placeholder="Applicant Code"
                                  onChange={(event) => { setApplicantCode(event.target.value); setApplicantError(false) }}
                                />
                              </div>
                              <div className="mb-10 pb-1 pt-1 text-center">
                                <button
                                  className="button-primary w-full"
                                  type="button"
                                  data-te-ripple-init=""
                                  data-te-ripple-color="light"
                                  onClick={() => { getParticipantDetails(); }}
                                >
                                  Fetch Details
                                </button>
                                {/*Forgot password link*/}
                                {/* <a href="#!">Forgot password?</a> */}
                              </div>
                            </>
                            : null}

                          {/*Submit button*/}
                          {radioOnboard !== "actual_payor" ?
                            <div className="mb-2 pb-1 pt-1 text-center items-center">
                              <div className="flex items-center justify-center mb-4">
                                <input id="link-checkbox" type="checkbox" value="" className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600"
                                  onChange={(event) => setShowTerms(event)}
                                  checked={_.get(appData, "termsAccepted")}></input>
                                <label htmlFor="link-checkbox" className="ml-2 label-primary">I agree with the <a href="#" onClick={() => dispatch(addAppData({ "showTerms": true }))} className="text-blue-600 dark:text-blue-500 hover:underline">terms and conditions</a>.</label>
                              </div>
                              <div className="mb-1 pb-1 pt-1 text-center w-full">
                              <button
                                className={"button-primary w-full " + (_.get(appData, "termsAccepted") ? "" : "disabled:opacity-75")}
                                type="button"
                                data-te-ripple-init=""
                                data-te-ripple-color="light"
                                disabled={!_.get(appData, "termsAccepted")}
                                onClick={() => { onSubmit(); }}
                              >
                                Register
                              </button>
                              </div>
                              {/*Forgot password link*/}
                              {/* <a href="#!">Forgot password?</a> */}
                            </div> : null}

                          {/*Register button*/}
                          <div className="flex items-center place-content-end pb-6">
                            <a href="#" onClick={(e) => { e.preventDefault(); navigate("/onboarding/login") }}><p className="underline">Already have an account?</p></a>
                            {/* <button
                            type="button"
                            className="inline-block rounded border-2 border-blue-500 px-6 pb-[6px] pt-2 text-xs font-medium uppercase leading-normal text-blue-500 transition duration-150 ease-in-out hover:border-blue-600 hover:bg-neutral-500 hover:bg-opacity-10 hover:text-blue-600 focus:border-blue-600 focus:text-blue-600 focus:outline-none focus:ring-0 active:border-blue-700 active:text-blue-700 dark:hover:bg-neutral-100 dark:hover:bg-opacity-10"
                            data-te-ripple-init=""
                            data-te-ripple-color="light"
                            onClick={() => navigate("/onboarding/login")}
                          >
                            Login
                          </button> */}
                          </div>
                        </form>
                        :
                        <form>
                          {Stepper(checkBasic,radioRole,showVerify,checkVerify,checkResetPass)}
                          <p className="mb-3">Please create your password</p>
                          <div className="relative mb-4">
                            <input
                              type="password"
                              className={"input-primary" + (pass1Error ? " border-red-600" : "")}
                              id="exampleFormControlInput1"
                              placeholder="Password"
                              onChange={(event) => { setPass1(event.target.value); setPass1Error(false) }}
                              required
                            />
                          </div>
                          {/*Password input*/}
                          <div className="relative mb-4">
                            <input
                              type="password"
                              className={"input-primary" + (pass2Error ? " border-red-600" : "")}
                              id="exampleFormControlInput11"
                              placeholder="Re-enter Password"
                              onChange={(event) => { setPass2(event.target.value); setPass2Error(false) }}
                              required
                            />
                          </div>
                          <p className="text-grey-900 text-xs italic mb-4">*Password should have min 8 characters with atleast one smallcase, uppercase, number and special character</p>

                          {/*Submit button*/}
                          <div className="mb-12 pb-1 pt-1 text-center">
                            <button
                              className="button-primary"
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
                        </form>
                      }
                    </div>
                  </div>}
                {/* Right column container with background and description*/}
                <div
                  className="flex items-center rounded-b-lg lg:w-6/12 lg:rounded-r-lg lg:rounded-bl-none gradient-blue-purple"
                >
                  <div className="px-4 py-6 text-white md:mx-6 md:p-12">
                    <h4 className="mb-6 text-xl font-semibold">
                      We are here to help you register
                    </h4>
                    <p className="text-sm">
                      Step 1:  Select the role you prefer to onborad on the HCX
                    </p>
                    <p className="text-sm">
                      Step 2: If you are a Payor then provide the basic information to proceed. If you are a Provider then select a verfier to proceed. You can onboard through an Actual Payor or Mock Payor.
                    </p>
                    <p className="text-sm">
                      Step 3: Select the payor and enter the applicant code and click on fetch details. Using the applicant code, details will fetched from the selected payor system and populated in the form.
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
