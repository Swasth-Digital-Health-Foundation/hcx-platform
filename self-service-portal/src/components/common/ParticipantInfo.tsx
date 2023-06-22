
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../store";
import { useAuthActions } from "../../recoil/actions/auth.actions";
import _ from "lodash";
import { toast } from "react-toastify";
import { getParticipant, getParticipantByCode } from "../../api/RegistryService";
import { addParticipantDetails } from "../../reducers/participant_details_reducer";
import { post } from "../../api/APIService";
import { reverifyLink } from "../../api/UserService";

const ParticipantInfo = () => {

  const dispatch = useDispatch()
  const participantDetails: Object = useSelector((state: RootState) => state.participantDetailsReducer.participantDetails);
  const authToken = useSelector((state: RootState) => state.tokenReducer.participantToken);
  console.log("part details in dash", participantDetails, authToken);

  const { login } = useAuthActions();

  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [address, setAddress] = useState(_.get(participantDetails, "address") || {});
  const [encryptionCert, setEncryptionCert] = useState(_.get(participantDetails, "encryption_cert") || '');
  const [endpointUrl, setEndpointUrl] = useState(_.get(participantDetails, "endpoint_url") || '');
  const [certType, setCertType] = useState("text");
  const [certError, setCertError] = useState(false);
  const [endpointError, setEndPointError] = useState(false);
  const [actEmail, setActEmail] = useState(_.get(participantDetails, "onboard_validation_properties.email") || 'activation');
  const [actPhone, setActPhone] = useState(_.get(participantDetails, "onboard_validation_properties.phone") || 'verification');
  const [actMessage, setActMessage] = useState("Email and Phone verification is required to activate the HCX account")

  useEffect(() => {
    setAddress(_.get(participantDetails, "address") || {});
    setEncryptionCert(_.get(participantDetails, "encryption_cert") || '');
    setEndpointUrl(_.get(participantDetails, "endpoint_url") || '');
    if(_.get(participantDetails,"roles[0]") == "provider"){
    getParticipantByCode(_.get(participantDetails, "sponsors[0].verifierCode")).then(res => { 
        let emailV = res["data"]["participants"][0]["onboard_validation_properties"]["email"];
        let phoneV = res["data"]["participants"][0]["onboard_validation_properties"]["phone"];
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



  const getIdentityVerification = (type: string) => {
    if (_.get(participantDetails, "sponsors[0].status") == "accepted") {
      return (type == "color" ? "text-green-500" : "Successful")
    } else if (_.get(participantDetails, "sponsors[0].status") == "rejected") {
      return (type == "color" ? "text-green-500" : "Rejected")
    } else {
      return (type == "color" ? "text-yellow-500" : "Pending")
    }
  }


  const getStatusVerification = (type: string) => {
    if (_.get(participantDetails, "status") == "Active") {
      return (type == "color" ? "text-green-500" : "Active")
    } else if (_.get(participantDetails, "status") == "Inactive") {
      return (type == "color" ? "text-red-500" : "Inactive")
    } else if (_.get(participantDetails, "status") == "Blocked") {
      return (type == "color" ? "text-red-500" : "Blocked")
    }
    else {
      return (type == "color" ? "text-yellow-500" : "Created")
    }
  }

  const onSubmit = () => {
    //setSending(true)
    if (endpointUrl == "" || encryptionCert == "" || actEmail == actPhone) {
      if (endpointUrl == "") setEndPointError(true);
      if (encryptionCert == "") setCertError(true);
      if(actEmail == actPhone){
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


  return (
    <div className="p-4 sm:ml-64">
      <div className="p-4 border-2 border-gray-200 border rounded-lg dark:border-gray-700 mt-14">
        <form className="w-full p-12">
          <div className="flex flex-wrap -mx-3 justify-between">
            <label
              className="block  uppercase tracking-wide text-gray-700 text-s font-bold mb-2"
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
                className="block  tracking-wide text-grey-700 text-sm font-bold m-1"
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
              className="w-1/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              Participant Name :
            </label>
            <label
              className="w-5/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              {_.get(participantDetails, "participant_name") ? _.get(participantDetails, "participant_name") : ""}
            </label>
            {/* <p className="text-red-500 text-sm italic">Please fill out this field.</p> */}
          </div>
          <div className="flex flex-wrap w-full px-3 mb-2">
            <label
              className="w-1/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-last-name"
            >
              Participant Code :
            </label>
            <label
              className="w-5/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              {_.get(participantDetails, "participant_code")}
            </label>
          </div>
          <div className="flex flex-wrap w-full px-3 mb-2">
            <label
              className="w-1/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              Email Address :
            </label>

            <label
              className="w-5/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              {_.get(participantDetails, "communication.emailVerified") ? `${_.get(participantDetails, "primary_email")} (Verified)` : `${_.get(participantDetails, "primary_email")} (Verification pending) `}
              &nbsp;&nbsp;&nbsp;&nbsp;
              {_.get(participantDetails, "communication.emailVerified") ?
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 inline-block mr-1 border rounded-full border-green-500" fill="none" viewBox="0 0 24 24" stroke="green">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg> :
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 inline-block mr-1" viewBox="0 0 24 24" fill="none" stroke="red" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M12 8v4M12 16h.01" />
                </svg>}
            <a href="#" className="text-blue-700 text-sm mx-2 font-medium"
              onClick={(event) => { event.preventDefault(); verifyResend("email") }}
            >Resend Verification link</a>
            </label>    
          </div>

          <div className="flex flex-wrap w-full px-3 mb-10">
            <label
              className="w-1/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              Phone Number :
            </label>

            <label
              className="w-5/6 block  tracking-wide text-gray-700 text-sm font-bold mb-2"
              htmlFor="grid-first-name"
            >
              {_.get(participantDetails, "communication.phoneVerified") ? `${_.get(participantDetails, "primary_mobile")} (Verified)` : `${_.get(participantDetails, "primary_mobile")} (Verification pending)`}
              &nbsp;&nbsp;&nbsp;&nbsp;
              {_.get(participantDetails, "communication.phoneVerified") ?
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 inline-block mr-1 border rounded-full border-green-500" fill="none" viewBox="0 0 24 24" stroke="green">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg> :
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 inline-block mr-1" viewBox="0 0 24 24" fill="none" stroke="red" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M12 8v4M12 16h.01" />
                </svg>}
            <a href="#" className="text-blue-700 text-sm mx-2 font-medium"
              onClick={(event) => { event.preventDefault(); verifyResend("phone number")}}
            >Resend Verification link</a>
            </label>    
          </div>
          <div className="flex flex-wrap -mx-3 mb-2">
            <label
              className="block uppercase tracking-wide text-gray-700 text-s font-bold mb-1"
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
                className="block  tracking-wide text-gray-700 text-sm font-bold mb-2"
                htmlFor="grid-password"
              >
                Encryption Certificate
              </label>
              <div className="flex mb-3">
                <div className="flex items-center mr-4">
                  <input id="inline-radio" type="radio" value="" name="inline-radio-group" className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500"
                    onClick={() => setCertType("text")} defaultChecked></input>
                  <label htmlFor="inline-radio" className="ml-2 text-sm font-medium text-gray-900 dark:text-gray-300">Certificate URL</label>
                </div>
                <div className="flex items-center mr-4">
                  <input id="inline-2-radio" type="radio" value="" name="inline-radio-group" className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500"
                    onClick={() => setCertType("textarea")}></input>
                  <label htmlFor="inline-2-radio" className="ml-2 text-sm font-medium text-gray-900 dark:text-gray-300">Certificate</label>
                </div>

              </div>
              {certType == "textarea" ?
                <textarea
                  className={"appearance-none block w-full bg-gray-100 text-gray-700 border border-gray-200 rounded py-3 px-4 mb-3 leading-tight focus:outline-none focus:bg-white focus:border-gray-500" + (certError ? " border-red-600" : "")}
                  id="grid-password"
                  placeholder="Please provide your public certificate here"
                  //value={encryptionCert}
                  onChange={(event) => { setEncryptionCert(event.target.value); setCertError(false) }}
                /> :
                <input
                  className={"appearance-none block w-full bg-gray-100 text-gray-700 border border-gray-200 rounded py-3 px-4 mb-3 leading-tight focus:outline-none focus:bg-white focus:border-gray-500" + (certError ? " border-red-600" : "")}
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
                className="block  tracking-wide text-gray-700 text-sm font-bold mb-2"
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
              className="block uppercase tracking-wide text-gray-700 text-s font-bold mb-2"
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
                className="block  tracking-wide text-gray-700 text-sm font-bold mb-2"
                htmlFor="grid-last-name"
              >
                Email
              </label>
              <select id="payordropdown"
                value={actEmail}
                onChange={(event) => { setActEmail(event.target.value) }}
                className="appearance-none block w-full bg-gray-200 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500" >
                <option value="activation">Activation</option>
                <option value="verification">Verification</option>
              </select>
            </div>
            <div className="w-full md:w-1/2 px-3 mb-6 md:mb-0">
              <label
                className="block  tracking-wide text-gray-700 text-sm font-bold mb-2"
                htmlFor="grid-last-name"
              >
                Phone
              </label>
              <select id="payordropdown"
                value={actPhone}
                onChange={(event) => { setActPhone(event.target.value) }}
                className="appearance-none block w-full bg-gray-200 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500" >
                <option value="activation">Activation</option>
                <option value="verification">Verification</option>
              </select>
            </div>
          </div>
          </> : null}
          <div className="flex flex-wrap -mx-3 mt-10">
            <label
              className="block uppercase tracking-wide text-gray-700 text-s font-bold mb-2"
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
                className="block  tracking-wide text-gray-700 text-sm font-bold mb-2"
                htmlFor="grid-city"
              >
                Plot
              </label>
              <input
                className="appearance-none block w-full bg-gray-100 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500"
                id="grid-city"
                type="text"
                placeholder=""
                value={_.get(address, "plot") || ''}
                onChange={(event) => setAddress({ ...address, "plot": event.target.value })}
              />
            </div>
            <div className="w-full md:w-1/3 px-3 mb-6 md:mb-0">
              <label
                className="block  tracking-wide text-gray-700 text-sm font-bold mb-2"
                htmlFor="grid-city"
              >
                Street
              </label>
              <input
                className="appearance-none block w-full bg-gray-100 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500"
                id="grid-city"
                type="text"
                placeholder=""
                value={_.get(address, "street") || ''}
                onChange={(event) => setAddress({ ...address, "street": event.target.value })}
              />

            </div>
            <div className="w-full md:w-1/3 px-3 mb-6 md:mb-0">
              <label
                className="block  tracking-wide text-gray-700 text-sm font-bold mb-2"
                htmlFor="grid-zip"
              >
                Landmark
              </label>
              <input
                className="appearance-none block w-full bg-gray-100 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500"
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
                className="block  tracking-wide text-gray-700 text-sm font-bold mb-2"
                htmlFor="grid-city"
              >
                District
              </label>
              <input
                className="appearance-none block w-full bg-gray-100 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500"
                id="grid-city"
                type="text"
                placeholder=""
                value={_.get(address, "district") || ''}
                onChange={(event) => setAddress({ ...address, "district": event.target.value })}
              />
            </div>
            <div className="w-full md:w-1/3 px-3 mb-6 md:mb-0">
              <label
                className="block  tracking-wide text-gray-700 text-sm font-bold mb-2"
                htmlFor="grid-city"
              >
                State
              </label>
              <input
                className="appearance-none block w-full bg-gray-100 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500"
                id="grid-city"
                type="text"
                placeholder=""
                value={_.get(address, "state") || ''}
                onChange={(event) => setAddress({ ...address, "state": event.target.value })}
              />

            </div>
            <div className="w-full md:w-1/3 px-3 mb-6 md:mb-0">
              <label
                className="block  tracking-wide text-gray-700 text-sm font-bold mb-2"
                htmlFor="grid-zip"
              >
                Pincode
              </label>
              <input
                className="appearance-none block w-full bg-gray-100 text-gray-700 border border-gray-200 rounded py-3 px-4 leading-tight focus:outline-none focus:bg-white focus:border-gray-500"
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
              Update
            </button>
            <button
              className="mb-3 inline-block w-1/5 rounded px-6 pb-2 pt-2.5 text-sm font-medium  leading-normal text-white shadow-[0_4px_9px_-4px_rgba(0,0,0,0.2)] transition duration-150 ease-in-out hover:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] focus:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)] focus:outline-none focus:ring-0 active:shadow-[0_8px_9px_-4px_rgba(0,0,0,0.1),0_4px_18px_0_rgba(0,0,0,0.2)]"
              type="button"
              data-te-ripple-init=""
              data-te-ripple-color="light"
              style={{
                background:
                  "linear-gradient(to right, #1C4DC3, #3632BE, #1D1991, #060347)"
              }}
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