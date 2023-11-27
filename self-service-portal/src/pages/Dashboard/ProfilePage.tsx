import React, { useEffect, useState } from 'react';
import CardDataStatsProfile from '../../components/CardDataStatsProfile';
import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { toast } from 'react-toastify';
import { generateAPIKey, generateClientSecret, getParticipant, getParticipantByCode } from '../../api/RegistryService';
import { serachUser, reverifyLink } from '../../api/UserService';
import { addAppData } from '../../reducers/app_data';
import { addParticipantDetails } from '../../reducers/participant_details_reducer';
import { addParticipantToken } from '../../reducers/token_reducer';
import { RootState } from '../../store';
import { post } from '../../api/APIService';
import { useNavigate } from 'react-router-dom';
import SwitcherProfile from '../../components/SwitcherProfile';
import Loader from '../../common/Loader';
import ModalConfirmBack from '../../components/ModalConfrimBack';

const ProfilePage: React.FC = () => {

  const dispatch = useDispatch();
  const navigate = useNavigate();
  const participantDetails: Object = useSelector((state: RootState) => state.participantDetailsReducer.participantDetails);
  const authToken = useSelector((state: RootState) => state.tokenReducer.participantToken);
  console.log("part details in dash", participantDetails, authToken);
  const [email, setEmail] = useState(_.get(participantDetails, "primary_email") || "example@org.com");
  const [phone, setPhone] = useState(_.get(participantDetails, "primary_mobile") || "1234567890");
  const [address, setAddress] = useState(_.get(participantDetails, "address") || {});
  const [encryptionCert, setEncryptionCert] = useState(_.get(participantDetails, "encryption_cert") || '');
  const [endpointUrl, setEndpointUrl] = useState(_.get(participantDetails, "endpoint_url") || '');
  const [certType, setCertType] = useState("Certificate URL");
  const [certError, setCertError] = useState(false);
  const [endpointError, setEndPointError] = useState(false);
  const [actEmail, setActEmail] = useState(_.get(participantDetails, "onboard_validation_properties.email") || 'activation');
  const [actPhone, setActPhone] = useState(_.get(participantDetails, "onboard_validation_properties.phone") || 'verification');
  const [actMessage, setActMessage] = useState("Email and Phone verification is required to activate the HCX account");
  const [showModalYesNo, setShowModalYesNo] = useState(false);
  const [showLoader, setShowLoader] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);


  useEffect(() => {
    if (authToken == "abcd") {
      console.log("came in abcd",)
      let sessionToken = sessionStorage.getItem("hcx_user_token");
      let userName = sessionStorage.getItem("hcx_user_name");
      if (sessionToken == null) {
        navigate("/onboarding/login");
      } else {
        try {
          dispatch(addParticipantToken(sessionToken));
          dispatch(addAppData({ "username": userName }));
          getParticipant(userName).then((res: any) => {
            console.log("we are in inside get par", res, res["data"]["participants"].length);
            if (res["data"]["participants"].length !== 0) {
              console.log("came in if")
              dispatch(addParticipantDetails(res["data"]["participants"][0]));
              setEmail(res["data"]["participants"][0]["primary_email"]);
              console.log("email", email, res["data"]["participants"][0]["primary_email"]);
              setPhone(res["data"]["participants"][0]["primary_mobile"]);
            } else {
              console.log("came in else");
              serachUser(userName).then((res: any) => {
                console.log("search user res", res);
                let osOwner = res["data"]["users"][0]["osOwner"];
                let participant = res["data"]["users"][0]["tenant_roles"];
                participant.map((value: any, index: any) => {
                  getParticipantByCode(value.participant_code).then(res => {
                    console.log("participant info", res);
                    dispatch(addParticipantDetails(res["data"]["participants"][0]));
                    setEmail(res["data"]["participants"][0]["primary_email"]);
                    setPhone(res["data"]["participants"][0]["primary_mobile"]);

                  })
                  dispatch(addAppData({ "sidebar": "Profile" }))
                })
              });
            }
          })
        } catch {
          navigate("/onboarding/login");
        }
      }
    }
  }, []);


  useEffect(() => {
    setAddress(_.get(participantDetails, "address") || {});
    setEncryptionCert(_.get(participantDetails, "encryption_cert") || '');
    setEndpointUrl(_.get(participantDetails, "endpoint_url") || '');
    if (_.get(participantDetails, "roles[0]") == "provider") {
      getParticipantByCode(_.get(participantDetails, "sponsors[0].verifierCode")).then(res => {
        console.log("verifier details", res);
        let emailV = "";
        try {
          emailV = res["data"]["participants"][0]["onboard_validation_properties"]["email"] != undefined ? res["data"]["participants"][0]["onboard_validation_properties"]["email"] : "Activation";
        } catch {
          emailV = "Activation";
        }
        let phoneV = "";
        try {
          phoneV = res["data"]["participants"][0]["onboard_validation_properties"]["phone"] != undefined ? res["data"]["participants"][0]["onboard_validation_properties"]["phone"] : "Activation";
        } catch {
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
    setShowLoader(true);
    if (endpointUrl == "" || encryptionCert == "") {
      if (endpointUrl == "") setEndPointError(true);
      if (encryptionCert == "") setCertError(true);
      if (actEmail == "Verification" && actPhone == "Verification") {
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
      })
    }
    setShowLoader(false);
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

  const secretGenerateCancel = () => {
    setShowConfirmModal(false);
  }

  const secretGenerateApproved = () => {
    setShowConfirmModal(false);
    generateClientSecret(_.get(participantDetails, "participant_code"), authToken).then((res:any) => {
      toast.success(`Client Secret has been successfully generated. An email has been sent to all users`, {
        position: toast.POSITION.TOP_CENTER
      });
    }).catch(err => {
      toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
        position: toast.POSITION.TOP_RIGHT
      });
    });
  }

  const generateAPISecret = ()=> {
    generateAPIKey(_.get(appData, "username") || "", _.get(participantDetails, "participant_code") || "").then((res:any) => {
      toast.success(`User API Key has been successfully generated. An email has to the registered email address`, {
        position: toast.POSITION.TOP_CENTER
      });
    }).catch(err => {
      toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
        position: toast.POSITION.TOP_RIGHT
      });
    });;
  }

  return (
    <>
      <CardDataStatsProfile></CardDataStatsProfile>

      {showLoader ? <Loader></Loader> : null }
      <div className="mt-4 grid md:mt-6 md:gap-6 2xl:mt-7.5 2xl:gap-7.5">
      {showConfirmModal == true?
      <ModalConfirmBack show={showConfirmModal} title="Generate Client Secret" body="You are about to generate a new Client Secret which will invalidate existing secret for all users of the participant. An email will be sent to all users." 
      onCancelClick={secretGenerateCancel} onSubmitClick={secretGenerateApproved}></ModalConfirmBack> :
      null }
        <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
          <div className="border-b border-stroke py-4 px-6.5 dark:border-strokedark">
            <h3 className="font-medium text-black dark:text-white">
              HCX Connectivity Details
            </h3>
          </div>
          <div className="flex flex-col gap-5.5 p-6.5">
            <div>
              <label className="mb-3 block text-black dark:text-white">
                Endpoint URL
              </label>
              <input
                type="text"
                placeholder="Endpoint URL"
                value={endpointUrl}
                className="w-full rounded-lg border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                onChange={(event) => { setEndpointUrl(event.target.value); setEndPointError(false) }} />
            </div>


            <div className='flex flex-wrap gap-5'>
              <label className="mb-3 block text-black dark:text-white">
                Certificate Type :
              </label>
              {/* <RadioBox text="Certificate URL" onRadioCheck={(text) => {setCertType(text)}}></RadioBox>
              <RadioBox text="Certificate" onRadioCheck={(text) => {setCertType(text); window.console.log("cert type ", text)}}></RadioBox> */}
              <label className="block text-black dark:text-white">
                Certificate URL
              </label>
              <SwitcherProfile value={false} onSwitch={(value) => value ? setCertType("Certificate") : setCertType("Certificate URL")} />
              <label className="block text-black dark:text-white">
                Certificate
              </label>
            </div>
            {certType == "Certificate URL" ?
              <div>
                <label className="mb-3 block text-black dark:text-white">
                  Public Certificate URL
                </label>
                <input
                  type="text"
                  placeholder="Active Input"
                  className="w-full rounded-lg border-[1.5px] border-primary bg-transparent py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:bg-form-input"
                  value={encryptionCert}
                  onChange={(event) => { setEncryptionCert(event.target.value); setCertError(false) }} />
              </div>
              :
              <div>
                <label className="mb-3 block text-black dark:text-white">
                  Public Certificate
                </label>
                <textarea
                  rows={6}
                  placeholder="Active textarea"
                  className="w-full rounded-lg border-[1.5px] border-primary bg-transparent py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:bg-form-input"
                  onChange={(event) => { setEncryptionCert(event.target.value); setCertError(false) }}></textarea>
              </div>
            }
          </div>
        </div>
      </div>
      <div className="mt-4 grid md:mt-6 md:gap-6 2xl:mt-7.5 2xl:gap-7.5">
        <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
          <div className="border-b border-stroke py-4 px-6.5 dark:border-strokedark">
            <h3 className="font-medium text-black dark:text-white">
              Provider Onboarding Configuration
            </h3>
          </div>
          <div className="mb-4.5 flex flex-col gap-5.5 p-6.5 xl:flex-row">
            <div className="w-full xl:w-1/2">
              <label className="mb-2.5 block text-black dark:text-white">
                Email
              </label>
              <div className="relative z-20 bg-transparent dark:bg-form-input">
                <select className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-3 px-5 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                  onChange={(event) => { setActEmail(event.target.value) }}>
                  <option value="activation">Activation & communication</option>
                <option value="verification">Communication</option>
                </select>
                <span className="absolute top-1/2 right-4 z-30 -translate-y-1/2">
                  <svg
                    className="fill-current"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <g opacity="0.8">
                      <path
                        fillRule="evenodd"
                        clipRule="evenodd"
                        d="M5.29289 8.29289C5.68342 7.90237 6.31658 7.90237 6.70711 8.29289L12 13.5858L17.2929 8.29289C17.6834 7.90237 18.3166 7.90237 18.7071 8.29289C19.0976 8.68342 19.0976 9.31658 18.7071 9.70711L12.7071 15.7071C12.3166 16.0976 11.6834 16.0976 11.2929 15.7071L5.29289 9.70711C4.90237 9.31658 4.90237 8.68342 5.29289 8.29289Z"
                        fill=""
                      ></path>
                    </g>
                  </svg>
                </span>
              </div>

            </div>
            <div className="w-full xl:w-1/2">
              <label className="mb-2.5 block text-black dark:text-white">
                Phone
              </label>
              <div className="relative z-20 bg-transparent dark:bg-form-input">
                <select className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-3 px-5 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                  onChange={(event) => { setActPhone(event.target.value) }}>
                  <option value="activation">Activation & communication</option>
                <option value="verification">Communication</option>
                </select>
                <span className="absolute top-1/2 right-4 z-30 -translate-y-1/2">
                  <svg
                    className="fill-current"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <g opacity="0.8">
                      <path
                        fillRule="evenodd"
                        clipRule="evenodd"
                        d="M5.29289 8.29289C5.68342 7.90237 6.31658 7.90237 6.70711 8.29289L12 13.5858L17.2929 8.29289C17.6834 7.90237 18.3166 7.90237 18.7071 8.29289C19.0976 8.68342 19.0976 9.31658 18.7071 9.70711L12.7071 15.7071C12.3166 16.0976 11.6834 16.0976 11.2929 15.7071L5.29289 9.70711C4.90237 9.31658 4.90237 8.68342 5.29289 8.29289Z"
                        fill=""
                      ></path>
                    </g>
                  </svg>
                </span>
              </div>

            </div>
          </div>
        </div>
      </div>
      <div className="mt-4 grid md:mt-6 md:gap-6 2xl:mt-7.5 2xl:gap-7.5">
        <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
          <div className="border-b border-stroke py-4 px-6.5 dark:border-strokedark">
            <h3 className="font-medium text-black dark:text-white">
              Client Secret and Keys Management
            </h3>
          </div>
          <div className="mb-4.5 flex flex-col gap-5.5 p-6.5 xl:flex-row justify-between">
          <div className="w-full md:w-1/3">
              <input
                type="submit"
                value="Generate Client Secret"
                className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
                onClick={(event) => { event.preventDefault(); setShowConfirmModal(true);}}
              />
            </div>
            <div className="w-full md:w-1/3">
              <input
                type="submit"
                value="Generate API Key"
                className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
                onClick={(event) => { event.preventDefault(); generateAPISecret();}}
              />
            </div>
          </div>
        </div>
      </div>
      <div className="mt-4 grid md:mt-6 md:gap-6 2xl:mt-7.5 2xl:gap-7.5">
        <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
          <div className="border-b border-stroke py-4 px-6.5 dark:border-strokedark">
            <h3 className="font-medium text-black dark:text-white">
              Address
            </h3>
          </div>
          <div className="flex flex-col gap-5.5 p-6.5 xl:flex-row">
            <div className="w-full xl:w-1/3">
              <label className="mb-2.5 block text-black dark:text-white">
                Plot
              </label>
              <input
                type="text"
                placeholder=""
                className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                value={_.get(address, "plot") || ''}
                onChange={(event) => setAddress({ ...address, "plot": event.target.value })} />
            </div>

            <div className="w-full xl:w-1/3">
              <label className="mb-2.5 block text-black dark:text-white">
                Street
              </label>
              <input
                type="text"
                placeholder=""
                className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                value={_.get(address, "street") || ''}
                onChange={(event) => setAddress({ ...address, "street": event.target.value })} />
            </div>
            <div className="w-full xl:w-1/3">
              <label className="mb-2.5 block text-black dark:text-white">
                Landmark
              </label>
              <input
                type="text"
                placeholder=""
                className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                value={_.get(address, "landmark") || ''}
                onChange={(event) => setAddress({ ...address, "landmark": event.target.value })} />
            </div>
          </div>
          <div className="mb-4.5 flex flex-col gap-5.5 px-6.5 pb-6.5 xl:flex-row">
            <div className="w-full xl:w-1/3">
              <label className="mb-2.5 block text-black dark:text-white">
                District
              </label>
              <input
                type="text"
                placeholder=""
                className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                value={_.get(address, "district") || ''}
                onChange={(event) => setAddress({ ...address, "district": event.target.value })} />
            </div>
            <div className="w-full xl:w-1/3">
              <label className="mb-2.5 block text-black dark:text-white">
                State
              </label>
              <input
                type="text"
                placeholder=""
                className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                value={_.get(address, "state") || ''}
                onChange={(event) => setAddress({ ...address, "state": event.target.value })} />
            </div>
            <div className="w-full xl:w-1/3">
              <label className="mb-2.5 block text-black dark:text-white">
                Pincode
              </label>
              <input
                type="text"
                placeholder=""
                className="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                value={_.get(address, "pincode") || ''}
                onChange={(event) => setAddress({ ...address, "pincode": event.target.value })} />
            </div>
          </div>
        </div>
      </div>
      <div className="mt-4 grid md:mt-6 md:gap-6 2xl:mt-7.5 2xl:gap-7.5">
        <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
          <div className="flex flex-col gap-5.5 px-6.5 py-3 items-center">
            <div className="w-full md:w-1/3">
              <input
                type="submit"
                value="Update Information"
                className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
                onClick={(event) => { event.preventDefault(); onSubmit(); }}
              />
            </div>
            </div>
          </div>
          </div>
    </>
  );
};

export default ProfilePage;
