import { useEffect, useState } from "react";
import { useAuthActions } from "../../recoil/actions/auth.actions";
import logo from "../../swasth_logo.png";
import { useQuery } from "../../api/QueryService";
import { post } from "../../api/APIService";
import * as _ from 'lodash';
import { toast } from "react-toastify";
import { getParticipant, getParticipantSearch } from "../../api/RegistryService";
import { setPassword } from "../../api/KeycloakService";
import { useHistory } from "react-router-dom";
import queryString from 'query-string';
import { navigate } from "raviger";
import { useDispatch } from "react-redux";
import { addParticipantDetails } from "../../reducers/participant_details_reducer";
import { login } from "../../api/api";
import { addParticipantToken } from "../../reducers/token_reducer";

type Payor = {
  participant_code: string,
  participant_name :string 
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
  const [showLoader, setShowLoader] = useState(false);
  const [checkBasic, setCheckBasic] =  useState(false);
  const [checkVerify, setCheckVerify] =  useState(false);
  const [checkResetPass, setCheckResetPass] =  useState(false);
  const [showVerify, setShowVerify] = useState(false);
  const [isEnvStaging, setIsEnvStaging] = useState(true);
  const [emailError, setEmailError] = useState(false);
  const [phoneError, setPhoneError] = useState(false);
  const [orgError, setOrgError] = useState(false);
  const [applicantError, setApplicantError] = useState(false);
  const [pass1Error, setPass1Error] = useState(false);
  const [pass2Error, setPass2Error] = useState(false);



  useEffect(() => {
    const jwtToken = _.get(queryString.parse(window.location.search),"jwt_token");
    console.log("env ",env);
    if(env !== "staging"){
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


  const resetPassword = () => {
      console.log("we are here to set the password");
      if(pass1 == '' && pass2 == ''){
        setPass1Error(true);
        setPass2Error(true);
      }else{
      if (pass1 === pass2) {
        let osOwner;
        setCheckResetPass(true);
        getParticipant(email).then((res :any) => {
          console.log("we are in inside get par", res);
           osOwner = res["data"]["participants"][0]["osOwner"];
           dispatch(addParticipantDetails(res["data"]["participants"][0]));
          setPassword(osOwner[0], pass1).then((async function () {
            login({username : email,password : pass1}).then((res) => {
              dispatch(addParticipantToken(res["access_token"]));
            })
            navigate("/onboarding/dashboard");
        })).catch(err => {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                position: toast.POSITION.TOP_CENTER
            });
        }).finally(() => {
            setSending(false);
        })
        })
    } else {
        toast.error("Incorrect password", {
            position: toast.POSITION.TOP_CENTER
        });
    }}
  }
  

  const stepper = () => {
    return (
        <ol className="flex items-center w-full text-sm font-medium text-center text-gray-500 dark:text-gray-400 sm:text-base mb-4">
              <li className="flex md:w-full items-center text-blue-600 dark:text-blue-500 sm:after:content-[''] after:w-full after:h-1 after:border-b after:border-gray-200 after:border-1 after:hidden sm:after:inline-block after:mx-1 xl:after:mx-3 dark:after:border-gray-700">
                  <span className="flex items-center after:content-['/'] sm:after:hidden after:mx-2 after:text-gray-200 dark:after:text-gray-500">
                      {checkBasic ? 
                      <svg aria-hidden="true" className="w-4 h-4 mr-2 sm:w-5 sm:h-5" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg>
                      : <span className="mr-2">1</span> }
                      Account <span className="hidden sm:inline-flex sm:ml-2">Info</span>
                  </span>
              </li>
              {radioRole == "provider"  && showVerify ?
              <li className="flex md:w-full items-center after:content-[''] after:w-full after:h-1 after:border-b after:border-gray-200 after:border-1 after:hidden sm:after:inline-block after:mx-6 xl:after:mx-3 dark:after:border-gray-700">
                  <span className={"flex items-center after:content-['/'] sm:after:hidden after:mx-2 after:text-gray-200 dark:after:text-gray-500" + (checkVerify ? " text-blue-600": '')} >
                      {checkVerify ? 
                      <svg aria-hidden="true" className="w-4 h-4 mr-2 sm:w-5 sm:h-5" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg>
                      : <span className="mr-2">2</span> }
                      Verify <span className="hidden sm:inline-flex sm:ml-2">Info</span>
                  </span>
              </li> : null }
              <li className={"flex items-center" + (checkResetPass ? " text-blue-600": '')} >
                    {checkResetPass ? 
                      <svg aria-hidden="true" className="w-4 h-4 mr-2 sm:w-5 sm:h-5" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path></svg>
                      : <span className="mr-2">{radioRole == "provider" && showVerify ? 3 : 2}</span> }
                  Change<span className="hidden sm:inline-flex sm:ml-2">Password</span>
              </li>
          </ol>)
      }



  const getParticipantDetails = () => {
    setSending(true)
    let payload;
    if (applicantCode == "" || applicantCode == "1299"){
      setApplicantError(true);
    }else{

    if (applicantCode && payor) {
        payload = { "applicant_code": applicantCode, "verifier_code": payor.participant_code }
    } else {
        payload = { "verification_token": _.get(queryString.parse(window.location.search),"jwt_token") }
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
            setEmail( _.get(respBody, 'email') || "");
            setPhoneNumber(_.get(respBody, 'mobile') || "");
            setFetchResponse(true);
            setCheckBasic(true);
            console.log("get info done");
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
  console.log("participant" , participant);
  if (participant) {
      setPayor(participant)
  }
}
  const onSubmit = () => {
    //setSending(true)
    const jwtToken = _.get(queryString.parse(window.location.search),"jwt_token");
    let formData: any[];

    // if (fields.length != 0) {
    //     fields.forEach(function (field) {
    //         field.value = data[field.name];
    //         delete field.id;
    //     });
    // }
    if(email == '' || phoneNumber == '' || org == ''){
        if(email == '') setEmailError(true);
        if(phoneNumber == '') setPhoneError(true);
        if(org == '') setOrgError(true);
    }else{
    

    if (isJWTPresent) {
        formData = [{ "type": "onboard-through-jwt", "jwt": jwtToken, additionalVerification: fields, "participant": { "participant_name": org, "primary_email": email, "primary_mobile": phoneNumber, "roles": ["provider"] } }];
    } else if (radioRole === 'payor') {
        formData = [{ "participant": { "participant_name": org, "primary_email": email, "primary_mobile": phoneNumber, "roles": [radioRole] } }];
    } else if (payor != null && !invalidApplicantCode) {
        formData = [{ "type": "onboard-through-verifier", "verifier_code": payor.participant_code, "applicant_code": applicantCode, additionalVerification: fields, "participant": { "participant_name": org, "primary_email": email, "primary_mobile": phoneNumber, "roles": ["provider"] } }];
    }else if (radioOnboard == "mock_payor") {
      console.log("in mock payor register");
      formData = [{ "type": "onboard-through-verifier", "verifier_code": mockPayorCode, "applicant_code": applicantCode, additionalVerification: fields, "participant": { "participant_name": org, "primary_email": email, "primary_mobile": phoneNumber, "roles": ["provider"] } }];
    }else{
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
                setShowPassword(true);
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
                    {showPassword == false ? 
                    <form>
                      <p className="mb-3">Please create your account</p>
                      {/* Role Selection   */}
                      <div className="flex justify-left relative mb-3" role="radiogroup">
                        <label
                          className="dark:text-neutral-200 dark:peer-focus:text-primary"
                        >
                          Select Role :&nbsp;&nbsp;
                        </label>
                        {/*First radio*/}
                        <div className="mb-[0.125rem] mr-4 inline-block min-h-[1.5rem] pl-[1.5rem]">
                          <input
                            className="form-radio relative float-left -ml-[1.5rem] mr-1 mt-0.5 h-5 w-5 appearance-none rounded-full border-2 border-solid border-neutral-300 before:pointer-events-none before:absolute before:h-4 before:w-4 before:scale-0 before:rounded-full before:bg-transparent before:opacity-0 before:shadow-[0px_0px_0px_13px_transparent] before:content-[''] after:absolute after:z-[1] after:block after:h-4 after:w-4 after:rounded-full after:content-[''] checked:border-primary checked:before:opacity-[0.16] checked:after:absolute checked:after:left-1/2 checked:after:top-1/2 checked:after:h-[0.625rem] checked:after:w-[0.625rem] checked:after:rounded-full checked:after:border-primary checked:after:bg-white checked:after:content-[''] checked:after:[transform:translate(-50%,-50%)] hover:cursor-pointer hover:before:opacity-[0.04] hover:before:shadow-[0px_0px_0px_13px_rgba(0,0,0,0.6)] focus:shadow-none focus:outline-none focus:ring-0 focus:before:scale-100 focus:before:opacity-[0.12] focus:before:shadow-[0px_0px_0px_13px_rgba(0,0,0,0.6)] focus:before:transition-[box-shadow_0.2s,transform_0.2s] checked:focus:border-primary checked:focus:before:scale-100 checked:focus:before:shadow-[0px_0px_0px_13px_#3b71ca] checked:focus:before:transition-[box-shadow_0.2s,transform_0.2s] dark:border-neutral-600 dark:checked:border-primary dark:checked:after:border-primary dark:checked:after:bg-primary dark:focus:before:shadow-[0px_0px_0px_13px_rgba(255,255,255,0.4)] dark:checked:focus:border-primary dark:checked:focus:before:shadow-[0px_0px_0px_13px_#3b71ca]"
                            type="radio"
                            name="role"
                            id="inlineRadio1"
                            defaultValue="payor"
                            defaultChecked
                            onClick={() => {setRadioRole('payor'); setRadioOnboard('')}}
                          />
                          <label
                            className="mt-px inline-block pl-[0.15rem] hover:cursor-pointer"
                            htmlFor="inlineRadio1"
                          >
                            Payor
                          </label>
                        </div>
                        {/*Second radio*/}
                        <div className="mb-[0.125rem] mr-4 inline-block min-h-[1.5rem] pl-[1.5rem]">
                          <input
                            className="form-radio relative float-left -ml-[1.5rem] mr-1 mt-0.5 h-5 w-5 appearance-none rounded-full border-2 border-solid border-neutral-300 before:pointer-events-none before:absolute before:h-4 before:w-4 before:scale-0 before:rounded-full before:bg-transparent before:opacity-0 before:shadow-[0px_0px_0px_13px_transparent] before:content-[''] after:absolute after:z-[1] after:block after:h-4 after:w-4 after:rounded-full after:content-[''] checked:border-primary checked:before:opacity-[0.16] checked:after:absolute checked:after:left-1/2 checked:after:top-1/2 checked:after:h-[0.625rem] checked:after:w-[0.625rem] checked:after:rounded-full checked:after:border-primary checked:after:bg-white checked:after:content-[''] checked:after:[transform:translate(-50%,-50%)] hover:cursor-pointer hover:before:opacity-[0.04] hover:before:shadow-[0px_0px_0px_13px_rgba(0,0,0,0.6)] focus:shadow-none focus:outline-none focus:ring-0 focus:before:scale-100 focus:before:opacity-[0.12] focus:before:shadow-[0px_0px_0px_13px_rgba(0,0,0,0.6)] focus:before:transition-[box-shadow_0.2s,transform_0.2s] checked:focus:border-primary checked:focus:before:scale-100 checked:focus:before:shadow-[0px_0px_0px_13px_#3b71ca] checked:focus:before:transition-[box-shadow_0.2s,transform_0.2s] dark:border-neutral-600 dark:checked:border-primary dark:checked:after:border-primary dark:checked:after:bg-primary dark:focus:before:shadow-[0px_0px_0px_13px_rgba(255,255,255,0.4)] dark:checked:focus:border-primary dark:checked:focus:before:shadow-[0px_0px_0px_13px_#3b71ca]"
                            type="radio"
                            name="role"
                            id="inlineRadio2"
                            defaultValue="provider"
                            onClick={() => {setRadioRole('provider'); isEnvStaging ? setRadioOnboard("mock_payor") : setRadioOnboard("actual_payor"); isEnvStaging ? setShowVerify(false) : setShowVerify(true)}}
                          />
                          <label
                            className="mt-px inline-block pl-[0.15rem] hover:cursor-pointer"
                            htmlFor="inlineRadio2"
                          >
                            Provider
                          </label>
                        </div>

                      </div>

                      {/* Onboard through Selection   */}
                      {radioRole == "provider" && isEnvStaging ? <div className="flex justify-left relative mb-3" role="radiogroup">
                        <label
                          className="dark:text-neutral-200 dark:peer-focus:text-primary"
                        >
                          Onboard through :&nbsp;&nbsp;
                        </label>
                        {/*First radio*/}
                        <div className="mb-[0.125rem] mr-4 inline-block min-h-[1.5rem] pl-[1.5rem]">
                          <input
                            className="form-radio relative float-left -ml-[1.5rem] mr-1 mt-0.5 h-5 w-5 appearance-none rounded-full border-2 border-solid border-neutral-300 before:pointer-events-none before:absolute before:h-4 before:w-4 before:scale-0 before:rounded-full before:bg-transparent before:opacity-0 before:shadow-[0px_0px_0px_13px_transparent] before:content-[''] after:absolute after:z-[1] after:block after:h-4 after:w-4 after:rounded-full after:content-[''] checked:border-primary checked:before:opacity-[0.16] checked:after:absolute checked:after:left-1/2 checked:after:top-1/2 checked:after:h-[0.625rem] checked:after:w-[0.625rem] checked:after:rounded-full checked:after:border-primary checked:after:bg-white checked:after:content-[''] checked:after:[transform:translate(-50%,-50%)] hover:cursor-pointer hover:before:opacity-[0.04] hover:before:shadow-[0px_0px_0px_13px_rgba(0,0,0,0.6)] focus:shadow-none focus:outline-none focus:ring-0 focus:before:scale-100 focus:before:opacity-[0.12] focus:before:shadow-[0px_0px_0px_13px_rgba(0,0,0,0.6)] focus:before:transition-[box-shadow_0.2s,transform_0.2s] checked:focus:border-primary checked:focus:before:scale-100 checked:focus:before:shadow-[0px_0px_0px_13px_#3b71ca] checked:focus:before:transition-[box-shadow_0.2s,transform_0.2s] dark:border-neutral-600 dark:checked:border-primary dark:checked:after:border-primary dark:checked:after:bg-primary dark:focus:before:shadow-[0px_0px_0px_13px_rgba(255,255,255,0.4)] dark:checked:focus:border-primary dark:checked:focus:before:shadow-[0px_0px_0px_13px_#3b71ca]"
                            type="radio"
                            name="onboard"
                            id="inlineRadio3"
                            defaultValue="mock_payor"
                            defaultChecked
                            onClick={() => {setRadioOnboard('mock_payor'); setShowVerify(false)}}
                          />
                          <label
                            className="mt-px inline-block pl-[0.15rem] hover:cursor-pointer"
                            htmlFor="inlineRadio3"
                          >
                            Mock Payor
                          </label>
                        </div>
                        {/*Second radio*/}
                        <div className="mb-[0.125rem] mr-4 inline-block min-h-[1.5rem] pl-[1.5rem]">
                          <input
                            className="form-radio relative float-left -ml-[1.5rem] mr-1 mt-0.5 h-5 w-5 appearance-none rounded-full border-2 border-solid border-neutral-300 before:pointer-events-none before:absolute before:h-4 before:w-4 before:scale-0 before:rounded-full before:bg-transparent before:opacity-0 before:shadow-[0px_0px_0px_13px_transparent] before:content-[''] after:absolute after:z-[1] after:block after:h-4 after:w-4 after:rounded-full after:content-[''] checked:border-primary checked:before:opacity-[0.16] checked:after:absolute checked:after:left-1/2 checked:after:top-1/2 checked:after:h-[0.625rem] checked:after:w-[0.625rem] checked:after:rounded-full checked:after:border-primary checked:after:bg-white checked:after:content-[''] checked:after:[transform:translate(-50%,-50%)] hover:cursor-pointer hover:before:opacity-[0.04] hover:before:shadow-[0px_0px_0px_13px_rgba(0,0,0,0.6)] focus:shadow-none focus:outline-none focus:ring-0 focus:before:scale-100 focus:before:opacity-[0.12] focus:before:shadow-[0px_0px_0px_13px_rgba(0,0,0,0.6)] focus:before:transition-[box-shadow_0.2s,transform_0.2s] checked:focus:border-primary checked:focus:before:scale-100 checked:focus:before:shadow-[0px_0px_0px_13px_#3b71ca] checked:focus:before:transition-[box-shadow_0.2s,transform_0.2s] dark:border-neutral-600 dark:checked:border-primary dark:checked:after:border-primary dark:checked:after:bg-primary dark:focus:before:shadow-[0px_0px_0px_13px_rgba(255,255,255,0.4)] dark:checked:focus:border-primary dark:checked:focus:before:shadow-[0px_0px_0px_13px_#3b71ca]"
                            type="radio"
                            name="onboard"
                            id="inlineRadio4"
                            defaultValue="actual_payor"
                            onClick={() => {setRadioOnboard('actual_payor'); setShowVerify(true)}}
                          />
                          <label
                            className="mt-px inline-block pl-[0.15rem] hover:cursor-pointer"
                            htmlFor="inlineRadio4"
                          >
                            Actual Payor
                          </label>
                        </div>
                      </div> : "" }

                      {stepper()}
                      
                      {radioOnboard !== "actual_payor" ?
                      <> 
                      <div className="relative">
                        <input
                          type="email"
                          className={"w-full h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline" + (emailError ? " border-red-600" : "")} 
                          id="exampleFormControlInput1"
                          placeholder="Email"
                          onChange={(event) => {setEmail(event.target.value); setEmailError(false)}}
                          value={email}
                        />
                      </div>
                      <div className="relative">
                        <input
                          type="tel"
                          className={"w-full h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline"  + (phoneError ? " border-red-600" : "")}
                          id="exampleFormControlInput11"
                          placeholder="Phone Number"
                          onChange={(event) => {setPhoneNumber(event.target.value); setPhoneError(false)}}
                          value={phoneNumber}
                        />
                      </div>
                      <div className="relative">
                        <input
                          type="text"
                          className={"w-full h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline"  + (orgError ? " border-red-600" : "")}
                          id="exampleFormControlInput12"
                          placeholder="Organization"
                          onChange={(event) => {setOrg(event.target.value); setOrgError(false)}}
                          value={org}
                        />
                      </div> </>: null}
                      {radioRole == "provider" && (radioOnboard == "actual_payor" || isEnvStaging == false) ? <div className="relative">
                      <select id="payordropdown" 
                        className="w-full h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline" 
                        onChange={(event) => getPayor(event.target.value) }>
                        <option selected value="1">Select Payor</option>
                        {payorList !== undefined ? payorList.map((value, index) => {
                          return <option value={value.participant_name}>{value.participant_name}</option>
                        }) : null }
                      </select>
                      </div> : null }
                      {radioOnboard == "actual_payor" ? 
                      <>
                      <div className="relative">
                      <input
                        type="text"
                        className={"w-full h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline" + (applicantError ? " border-red-600" : "")}
                        id="exampleFormControlInput12"
                        placeholder="Applicant Code"
                        onChange={(event) => {setApplicantCode(event.target.value); setApplicantError(false)}}
                      />
                      </div>
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
                          onClick={() => {getParticipantDetails();}}
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
                          onClick={() => {onSubmit();}}
                        >
                          Register
                        </button>
                        {/*Forgot password link*/}
                        {/* <a href="#!">Forgot password?</a> */}
                      </div> : null }

                      {/*Register button*/}
                      <div className="flex items-center justify-between pb-6">
                        <p className="mb-0 mr-2">Already have an account?</p>
                        <button
                          type="button"
                          className="inline-block rounded border-2 border-blue-500 px-6 pb-[6px] pt-2 text-xs font-medium uppercase leading-normal text-blue-500 transition duration-150 ease-in-out hover:border-blue-600 hover:bg-neutral-500 hover:bg-opacity-10 hover:text-blue-600 focus:border-blue-600 focus:text-blue-600 focus:outline-none focus:ring-0 active:border-blue-700 active:text-blue-700 dark:hover:bg-neutral-100 dark:hover:bg-opacity-10"
                          data-te-ripple-init=""
                          data-te-ripple-color="light"
                          onClick={() => navigate("/onboarding/login")}
                        >
                          Login
                        </button>
                      </div>
                    </form>
                      :  
                    <form>
                      {stepper()}
                      <p className="mb-3">Please set your password</p>
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
                  <div className="relative mb-4">
                    <input
                      type="password"
                      className={"w-full h-10 px-3 mb-4 text-base text-gray-700 placeholder-gray-600 border rounded-lg focus:shadow-outline" + (pass2Error ? " border-red-600" : "")}
                      id="exampleFormControlInput11"
                      placeholder="Password"
                      onChange={(event) => {setPass2(event.target.value); setPass2Error(false)}}
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
                      onClick={() => resetPassword()}
                    >
                      Set Password
                    </button>
                    {/*Forgot password link*/}
                    {/* <a href="#!">Forgot password?</a> */}
                  </div>
                    </form>
                    }
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
