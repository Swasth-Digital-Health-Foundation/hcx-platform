
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../store";
import { useAuthActions } from "../../recoil/actions/auth.actions";
import _ from "lodash";
import { toast } from "react-toastify";
import { generateClientSecret, getParticipant, getParticipantByCode } from "../../api/RegistryService";
import { addParticipantDetails } from "../../reducers/participant_details_reducer";
import { post } from "../../api/APIService";
import { reverifyLink, serachUser } from "../../api/UserService";
import { navigate } from "raviger";
import { addAppData } from "../../reducers/app_data";
import { addParticipantToken } from "../../reducers/token_reducer";
import ModalYesNo from "./ModalYesNo";

const ParticipantInfo = () => {

  const dispatch = useDispatch()
  const participantDetails: Object = useSelector((state: RootState) => state.participantDetailsReducer.participantDetails);
  const authToken = useSelector((state: RootState) => state.tokenReducer.participantToken);
  console.log("part details in dash", participantDetails, authToken);
  const [email, setEmail] = useState(_.get(participantDetails, "primary_email") || "");
  const [phone, setPhone] = useState(_.get(participantDetails, "primary_mobile") || "1234567890");
  const [address, setAddress] = useState(_.get(participantDetails, "address") || {});
  const [encryptionCert, setEncryptionCert] = useState(_.get(participantDetails, "encryption_cert") || '');
  const [endpointUrl, setEndpointUrl] = useState(_.get(participantDetails, "endpoint_url") || '');
  const [certType, setCertType] = useState("text");
  const [certError, setCertError] = useState(false);
  const [endpointError, setEndPointError] = useState(false);
  const [actEmail, setActEmail] = useState(_.get(participantDetails, "onboard_validation_properties.email") || 'activation');
  const [actPhone, setActPhone] = useState(_.get(participantDetails, "onboard_validation_properties.phone") || 'verification');
  const [actMessage, setActMessage] = useState("Email and Phone verification is required to activate the HCX account");
  const [showModalYesNo,setShowModalYesNo] = useState(false);




  useEffect(()=> {
    setEmail(_.get(participantDetails, "primary_email") || '');
    setPhone(_.get(participantDetails, "primary_mobile") || '1234567890');
  },[participantDetails])


  useEffect(()=> {
  if(authToken == "abcd"){
    console.log("came in abcd",)
    let sessionToken = sessionStorage.getItem("hcx_user_token");
    let userName = sessionStorage.getItem("hcx_user_name");
    if(sessionToken == null){
      navigate("/onboarding/login");
    }else{
      try{
        dispatch(addParticipantToken(sessionToken));
        dispatch(addAppData({"username":userName}));
        getParticipant(userName).then((res :any) => {
          console.log("we are in inside get par", res, res["data"]["participants"].length);
          if( res["data"]["participants"].length !== 0){
            console.log("came in if")
           dispatch(addParticipantDetails(res["data"]["participants"][0]));
           setEmail(res["data"]["participants"][0]["primary_email"]);
           console.log("email", email, res["data"]["participants"][0]["primary_email"]);
           setPhone(res["data"]["participants"][0]["primary_mobile"]);  
          }else{
            console.log("came in else");
            serachUser(userName).then((res: any) => {
              console.log("search user res", res);
              let osOwner = res["data"]["users"][0]["osOwner"];
              let participant = res["data"]["users"][0]["tenant_roles"];
              participant.map((value: any,index: any) => {
                getParticipantByCode(value.participant_code).then(res => {
                  console.log("participant info", res);
                  dispatch(addParticipantDetails(res["data"]["participants"][0]));
                  setEmail(res["data"]["participants"][0]["primary_email"]);
                  setPhone(res["data"]["participants"][0]["primary_mobile"]);
          
                })
                  dispatch(addAppData({"sidebar":"Profile"}))
              })
            });
          }
        })
      }catch{
        navigate("/onboarding/login");
      }
    }
  }
},[]);

  //const { login } = useAuthActions();

  
  useEffect(() => {
    setAddress(_.get(participantDetails, "address") || {});
    setEncryptionCert(_.get(participantDetails, "encryption_cert") || '');
    setEndpointUrl(_.get(participantDetails, "endpoint_url") || '');
    if(_.get(participantDetails,"roles[0]") == "provider"){
    getParticipantByCode(_.get(participantDetails, "sponsors[0].verifierCode")).then(res => { 
        console.log("verifier details",res);
        let emailV = "";
        try {
        emailV = res["data"]["participants"][0]["onboard_validation_properties"]["email"] != undefined ? res["data"]["participants"][0]["onboard_validation_properties"]["email"] : "Activation";
        }catch{
          emailV = "Activation";
        }
        let phoneV = "";
        try{
        phoneV = res["data"]["participants"][0]["onboard_validation_properties"]["phone"] != undefined ? res["data"]["participants"][0]["onboard_validation_properties"]["phone"] : "Activation";
        }catch{
          phoneV = "Activation";
        }
        if (emailV == "activation" && phoneV == "verification") {
          setActMessage("Email verification is required to activate the HCX account");
        } else if (emailV == "verification" && phoneV == "activation") {
          setActMessage("Phone verification is required to activate the HCX account");
        } else if (emailV == "verification" && phoneV == "verification") {
          setActMessage("Email and Phone verification is not required to activate the HCX account. But we suggest you do it");
        }
    })
  }
  }, [participantDetails]);

  const onSubmit = () => {
    //setSending(true)
    if (endpointUrl == "" || encryptionCert == "") {
      if (endpointUrl == "") setEndPointError(true);
      if (encryptionCert == "") setCertError(true);
      if(actEmail == "Verification" && actPhone == "Verification" ){
        toast.error("Atleast one of the Provider onboarding configuration needs to be Activation")
      }
    } else {
      const formData = {
        participant: {
          "participant_code": _.get(participantDetails, "participant_code"), "participant_name": _.get(participantDetails, "participant_name"), "endpoint_url": endpointUrl, "encryption_cert": encryptionCert
          , "address": address,
          "onboard_validation_properties": {
            "email": actEmail,
            "phone": actPhone
          }
        }
      };
      post("/participant/onboard/update", formData, {}, authToken).then((response => {
        const result = _.get(response, 'data.result') || {}
        console.log('result ', result)
        toast.success("Particpant Information updated successfully");
      })).catch(err => {
        toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
          position: toast.POSITION.TOP_CENTER
        });
      }).finally(() => {
        //setSending(false)
      })
    }
  }

  const refreshPage = () => {
    getParticipant(_.get(participantDetails, "primary_email")).then((res: any) => {
      dispatch(addParticipantDetails(res["data"]["participants"][0]));
      toast.success("Participant details refreshed successfully");
    }).catch(err => {
      toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
        position: toast.POSITION.TOP_CENTER
      });
    })
  }

  const verifyResend = (type:any) => {
    let payload = {};
    if(type == "email")
    {
      payload = {"primary_email":_.get(participantDetails, "primary_email"), 
                "participant_code": _.get(participantDetails, "participant_code"), 
                "participant_name": _.get(participantDetails, "participant_name")}
    }else{
      payload = {"primary_mobile":_.get(participantDetails, "primary_mobile"),
                "participant_code": _.get(participantDetails, "participant_code"), 
                "participant_name": _.get(participantDetails, "participant_name")}
    }
    reverifyLink(payload).then((res: any) => {
      toast.success(`Re-verification link successfully sent to ${type}`, {
        position: toast.POSITION.TOP_CENTER
      });
    }).catch(err => {
      toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
        position: toast.POSITION.TOP_CENTER
      });
    });
  }


  function generateClientSecrete(event: React.MouseEvent<HTMLButtonElement, MouseEvent>) {
    console.log("yes its clicked ", event);
    generateClientSecret(_.get(participantDetails, "participant_code")).then((res) =>{
      toast.success(`Client Secret has been sent to your primary email`, {
        position: toast.POSITION.TOP_CENTER
      })
    }).catch(err => {
        toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
          position: toast.POSITION.TOP_CENTER
        });;
    })
    setShowModalYesNo(false);
  }

  const cancelGenerateSecret = (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    setShowModalYesNo(false);
  }

  return (
    <div className="p-4 sm:ml-64">
      {showModalYesNo ?
      <ModalYesNo text="Are you sure you want to regenerate a new Client Secret?" onChildClickYes={(event) => {generateClientSecrete(event)}} onChildClickNo={(event) => {cancelGenerateSecret(event)}} ></ModalYesNo>
      : null }
      <div className="p-4 border-2 border-gray-200 border rounded-lg dark:border-gray-700 mt-14">
        <form className="w-full p-12">
          <div className="flex flex-wrap -mx-3 justify-between">
            <label
              className="lable-page-header"
            >
              Participant Information
            </label>
            <div className="flex flex-wrap mb-2">
              {_.get(participantDetails, "status") == "Active" ?
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 inline-block mr-1 border rounded-full border-green-500" fill="none" viewBox="0 0 24 24" stroke="green">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg> :
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 inline-block mr-1" viewBox="0 0 24 24" fill="none" stroke="red" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M12 8v4M12 16h.01" />
                </svg>}
              <label
                className="label-primary m-1"
                htmlFor="grid-first-name"
              >
                {_.get(participantDetails, "status")  == "Active" ? "Active" : "Inactive"}
              </label>
            </div>

          </div>
          <div className="flex flex-wrap -mx-3 mb-6 border-b-2 shadow-l shadow-bottom place-content-end">
            <p className="text-gray-600 text-sm italic">
              {actMessage}
            </p>
          </div>
          <div className="flex flex-wrap w-full px-3 mb-2">
            <label
              className="w-1/6 label-primary mb-2"
              htmlFor="grid-first-name"
            >
              Participant Name :
            </label>
            <label
              className="w-5/6 label-primary mb-2"
              htmlFor="grid-first-name"
            >
              {_.get(participantDetails, "participant_name") ? _.get(participantDetails, "participant_name") : ""}
            </label>
            {/* <p className="text-red-500 text-sm italic">Please fill out this field.</p> */}
          </div>
          <div className="flex flex-wrap w-full px-3 mb-2">
            <label
              className="w-1/6 label-primary mb-2"
              htmlFor="grid-last-name"
            >
              Participant Code :
            </label>
            <label
              className="w-5/6 label-primary mb-2"
              htmlFor="grid-first-name"
            >
              {_.get(participantDetails, "participant_code")}
            </label>
          </div>
          <div className="flex flex-wrap w-full px-3 mb-2">
            <label
              className="w-1/6 label-primary mb-2"
              htmlFor="grid-first-name"
            >
              Email Address :
            </label>

            <label
              className="w-5/6 label-primary mb-2"
              htmlFor="grid-first-name"
            >
              {_.get(participantDetails, "communication.emailVerified") ? `${email} (Verified)` : `${email} (Verification pending) `}
              &nbsp;&nbsp;&nbsp;&nbsp;
              {_.get(participantDetails, "communication.emailVerified") ?
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 inline-block mr-1 border rounded-full border-green-500" fill="none" viewBox="0 0 24 24" stroke="green">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg> :
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 inline-block mr-1" viewBox="0 0 24 24" fill="none" stroke="red" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M12 8v4M12 16h.01" />
                </svg>}
            {_.get(participantDetails, "communication.emailVerified") != true?    
            <a href="#" className="text-blue-700 text-sm mx-2 font-medium"
              onClick={(event) => { event.preventDefault(); verifyResend("email") }}
            >Resend Verification link</a> : null }
            </label>    
          </div>

          <div className="flex flex-wrap w-full px-3 mb-10">
            <label
              className="w-1/6 label-primary mb-2"
              htmlFor="grid-first-name"
            >
              Phone Number :
            </label>

            <label
              className="w-5/6 label-primary mb-2"
              htmlFor="grid-first-name"
            >
              {_.get(participantDetails, "communication.phoneVerified") ? `${phone} (Verified)` : `${phone} (Verification pending)`}
              &nbsp;&nbsp;&nbsp;&nbsp;
              {_.get(participantDetails, "communication.phoneVerified") ?
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 inline-block mr-1 border rounded-full border-green-500" fill="none" viewBox="0 0 24 24" stroke="green">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg> :
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 inline-block mr-1" viewBox="0 0 24 24" fill="none" stroke="red" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M12 8v4M12 16h.01" />
                </svg>}
                {_.get(participantDetails, "communication.phoneVerified") != true?    
            <a href="#" className="text-blue-700 text-sm mx-2 font-medium"
              onClick={(event) => { event.preventDefault(); verifyResend("phone number")}}
            >Resend Verification link</a>: null}
            </label>    
          </div>
          <div className="flex flex-wrap -mx-3 mb-2">
            <label
              className="label-primary mb-1"
            >
              HCX Connectivity Details
            </label>
          </div>
          <div className="flex flex-wrap -mx-3 mb-6 border-b-2 shadow-l shadow-bottom">
            <p className="text-gray-600 text-sm italic">
                Kindly provide Encryption certificates path or certificate and Endpoint url of your service
            </p>
          </div>
          <div className="flex flex-wrap -mx-3 mb-6">
            <div className="w-full px-3">
              <label
                className="label-primary mb-2"
                htmlFor="grid-password"
              >
                Encryption Certificate
              </label>
              <div className="flex mb-3">
                <div className="flex items-center mr-4">
                  <input id="inline-radio" type="radio" value="" name="inline-radio-group" className="radio-primary"
                    onClick={() => setCertType("text")} defaultChecked></input>
                  <label htmlFor="inline-radio" className="ml-2 label-primary">Certificate URL</label>
                </div>
                <div className="flex items-center mr-4">
                  <input id="inline-2-radio" type="radio" value="" name="inline-radio-group" className="radio-primary"
                    onClick={() => setCertType("textarea")}></input>
                  <label htmlFor="inline-2-radio" className="ml-2 label-primary">Certificate</label>
                </div>

              </div>
              {certType == "textarea" ?
                <textarea
                  className={"input-primary" + (certError ? " border-red-600" : "")}
                  id="grid-password"
                  placeholder="Please provide your public certificate here"
                  //value={encryptionCert}
                  onChange={(event) => { setEncryptionCert(event.target.value); setCertError(false) }}
                /> :
                <input
                  className={"input-primary" + (certError ? " border-red-600" : "")}
                  id="grid-password"
                  type="text"
                  placeholder=""
                  value={encryptionCert}
                  onChange={(event) => { setEncryptionCert(event.target.value); setCertError(false) }}
                />}
            </div>
          </div>
          <div className="flex flex-wrap -mx-3 mb-10">
            <div className="w-full px-3">
              <label
                className="label-primary mb-2"
                htmlFor="grid-password"
              >
                Endpoint URL
              </label>
              <input
                className={"appearance-none block w-full bg-gray-100 text-gray-700 border border-gray-200 rounded py-3 px-4 mb-3 leading-tight focus:outline-none focus:bg-white focus:border-gray-500" + (endpointError ? " border-red-600" : "")}
                id="grid-password"
                type="text"
                placeholder=""
                value={endpointUrl}
                onChange={(event) => { setEndpointUrl(event.target.value); setEndPointError(false) }}
              />
            </div>
          </div>
          {_.get(participantDetails,"roles[0]") == "payor"? 
          <>
            <div className="flex flex-wrap -mx-3 mb-1">
            <label
              className="label-primary mb-2"
            >
              Provider Onbaording Configuration
            </label>
          </div>
          <div className="flex flex-wrap -mx-3 mb-6 border-b-2 shadow-l shadow-bottom">
            <p className="text-gray-600 text-sm italic">
                Kindly provide the level of provider onboarding configuration to activate the provider's participant when they onboard through you
            </p>
          </div>
          <div className="flex flex-wrap -mx-3 mb-2">
            <div className="w-full md:w-1/2 px-3 mb-6 md:mb-0">
              <label
                className="label-primary mb-2"
                htmlFor="grid-last-name"
              >
                Email
              </label>
              <select id="payordropdown"
                value={actEmail}
                onChange={(event) => { setActEmail(event.target.value) }}
                className="appearance-none block w-full bg-gray-200 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500" >
                <option value="activation">Activation & communication</option>
                <option value="verification">Communication</option>
              </select>
            </div>
            <div className="w-full md:w-1/2 px-3 mb-6 md:mb-0">
              <label
                className="label-primary mb-2"
                htmlFor="grid-last-name"
              >
                Phone
              </label>
              <select id="payordropdown"
                value={actPhone}
                onChange={(event) => { setActPhone(event.target.value) }}
                className="appearance-none block w-full bg-gray-200 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500" >
                <option value="activation">Activation & communication</option>
                <option value="verification">Communication</option>
              </select>
            </div>
          </div>
          </> : null}
          <div className="flex flex-wrap -mx-3 mb-1">
            <label
              className="label-primary mb-2"
            >
              Client Secret Generation
            </label>
          </div>
          <div className="flex flex-wrap -mx-3 mb-6 border-b-2 shadow-l shadow-bottom">
            <p className="text-gray-600 text-sm italic">
                Client Secret regeneration will send email to all admins associated with the participant with new Client Secret.
            </p>
          </div>
          <div className="flex flex-wrap -mx-3 mb-6 justify-between">
            <button
              className="button-primary w-1/5"
              type="button"
              data-te-ripple-init=""
              data-te-ripple-color="light"
              onClick={() => setShowModalYesNo(true)}
            >
              Generate Client Secret
            </button>
          </div>
          <div className="flex flex-wrap -mx-3 mt-10">
            <label
              className="label-primary mb-2"
            >
              Address
            </label>
          </div>
          <div className="flex flex-wrap -mx-3 mb-10 border-b-2 shadow-l shadow-bottom">
            <p className="text-gray-700 text-sm italic">
                Primary address of the participant organization.
            </p>
          </div>
          <div className="flex flex-wrap -mx-3 mb-2">
            <div className="w-full md:w-1/3 px-3 mb-6 md:mb-0">
              <label
                className="label-primary mb-2"
                htmlFor="grid-city"
              >
                Plot
              </label>
              <input
                className="input-primary"
                id="grid-city"
                type="text"
                placeholder=""
                value={_.get(address, "plot") || ''}
                onChange={(event) => setAddress({ ...address, "plot": event.target.value })}
              />
            </div>
            <div className="w-full md:w-1/3 px-3 mb-6 md:mb-0">
              <label
                className="label-primary mb-2"
                htmlFor="grid-city"
              >
                Street
              </label>
              <input
                className="input-primary"
                id="grid-city"
                type="text"
                placeholder=""
                value={_.get(address, "street") || ''}
                onChange={(event) => setAddress({ ...address, "street": event.target.value })}
              />

            </div>
            <div className="w-full md:w-1/3 px-3 mb-6 md:mb-0">
              <label
                className="label-primary mb-2"
                htmlFor="grid-zip"
              >
                Landmark
              </label>
              <input
                className="input-primary"
                id="grid-zip"
                type="text"
                placeholder=""
                value={_.get(address, "landmark") || ''}
                onChange={(event) => setAddress({ ...address, "landmark": event.target.value })}
              />
            </div>
          </div>
          <div className="flex flex-wrap -mx-3 mb-8">
            <div className="w-full md:w-1/3 px-3 mb-6 md:mb-0">
              <label
                className="label-primary mb-2"
                htmlFor="grid-city"
              >
                District
              </label>
              <input
                className="input-primary"
                id="grid-city"
                type="text"
                placeholder=""
                value={_.get(address, "district") || ''}
                onChange={(event) => setAddress({ ...address, "district": event.target.value })}
              />
            </div>
            <div className="w-full md:w-1/3 px-3 mb-6 md:mb-0">
              <label
                className="label-primary mb-2"
                htmlFor="grid-city"
              >
                State
              </label>
              <input
                className="input-primary"
                id="grid-city"
                type="text"
                placeholder=""
                value={_.get(address, "state") || ''}
                onChange={(event) => setAddress({ ...address, "state": event.target.value })}
              />

            </div>
            <div className="w-full md:w-1/3 px-3 mb-6 md:mb-0">
              <label
                className="label-primary mb-2"
                htmlFor="grid-zip"
              >
                Pincode
              </label>
              <input
                className="input-primary"
                id="grid-zip"
                type="number"
                placeholder=""
                value={_.get(address, "pincode") || ''}
                onChange={(event) => setAddress({ ...address, "pincode": event.target.value })}
              />
            </div>
          </div>

          <div className="flex flex-wrap -mx-3 mb-6 justify-between">
            <button
              className="button-primary w-1/5"
              type="button"
              data-te-ripple-init=""
              data-te-ripple-color="light"
              onClick={() => onSubmit()}
            >
              Update
            </button>
            <button
              className="button-primary w-1/5"
              type="button"
              data-te-ripple-init=""
              data-te-ripple-color="light"
              onClick={() => refreshPage()}>
              Refresh Page
            </button>

          </div>
        </form>

      </div>
    </div>
  );


}

export default ParticipantInfo;