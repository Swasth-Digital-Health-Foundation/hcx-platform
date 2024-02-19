import React, { ChangeEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Logo from '../../images/hcx/swasth_logo.png';
import HcxImage from '../../images/hcx/banner.svg';
import Stepper from '../../components/Steeper';
import { useSelector, useDispatch } from 'react-redux';
import { post } from '../../api/APIService';
import { setUserPassword, generateTokenUser } from '../../api/KeycloakService';
import { getParticipantSearch, getParticipant } from '../../api/RegistryService';
import { addAppData } from '../../reducers/app_data';
import { addParticipantDetails } from '../../reducers/participant_details_reducer';
import { addParticipantToken } from '../../reducers/token_reducer';
import { RootState } from '../../store';
import queryString from 'query-string';
import * as _ from 'lodash';
import { toast } from "react-toastify";
import BasicInformation from '../../components/BasicInformation';
import SetPassword from '../../components/SetPassword';
import OnboardingSuccess from '../../components/OnboardingSuccess';
import RoleSelection from '../../components/RoleSelection';
import InviteUserRegister from '../../components/InviteUserRegister';
import { serachUser } from '../../api/UserService';
import Loader from '../../common/Loader';


type Payor = {
  participant_code: string,
  participant_name: string
}

const SignUp: React.FC = () => {

  const dispatch = useDispatch();
  const navigate = useNavigate();
  const mockPayorCode = process.env.REACT_APP_MOCK_PAYOR_CODE;
  const tag = process.env.REACT_APP_PARTICIPANT_TAG;
  const env = process.env.REACT_APP_ENV;


  //const { login } = useAuthActions();
  //const query = useQuery();

  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [org, setOrg] = useState('');
  const [radioRole, setRadioRole] = useState('provider');
  const [radioOnboard, setRadioOnboard] = useState('');
  const [fields, setFields] = useState([]);
  const [isJWTPresent, setIsJWTPresent] = useState(false);
  const [invalidApplicantCode, setInvalidApplicantCode] = useState(false);
  const [payor, setPayor] = useState<Payor>();
  const [sending, setSending] = useState(false);
  const [applicantCode, setApplicantCode] = useState("12990");
  const [fetchResponse, setFetchResponse] = useState(false);
  const [payorList, setPayorList] = useState<Payor[]>();
  const [showPassword, setShowPassword] = useState(false);
  const [showLoader, setShowLoader] = useState(false);
  const [checkBasic, setCheckBasic] = useState(false);
  const [checkVerify, setCheckVerify] = useState(false);
  const [checkResetPass, setCheckResetPass] = useState(false);
  const [showVerify, setShowVerify] = useState(false);
  const [isEnvStaging, setIsEnvStaging] = useState(true);
  const [showUserCreate, setShowUserCreate] = useState(false);
  const [stage, setStage] = useState("roleSelection");

  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  const participantDetails: Object = useSelector((state: RootState) => state.participantDetailsReducer.participantDetails);
  console.log("appData", appData, participantDetails);

  useEffect(() => {
    setStage(_.get(appData,"stageRegister") || "roleSelection");
  }, [appData])

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

  const onRoleSelection = () => {
    if(_.get(appData,"roleSelectedRegister") == "payor" || _.get(appData,"payorSelectedCodeRegister") == process.env.REACT_APP_MOCK_PAYOR_CODE){
      dispatch(addAppData({ "stageRegister": "accountInfo" }));
    }else{
      getParticipantDetails();
    }
  }
  
  const onInformationSubmit = () => {
    onSubmit();
  }

  const getParticipantDetails = () => {
    setShowLoader(true);
    let payload;
      if (_.get(appData,"applicantCodeRegister") && _.get(appData,"payorSelectedCodeRegister")) {
        payload = { "applicant_code": _.get(appData,"applicantCodeRegister"), "verifier_code": _.get(appData,"payorSelectedCodeRegister") }
      } else {
        payload = { "verification_token": _.get(queryString.parse(window.location.search), "jwt_token") }
      }
      post("/applicant/getinfo", JSON.stringify(payload)).then((data => {
        let respBody = data.data;
        dispatch(addAppData({"organizationNameRegister" : _.get(respBody, 'applicant_name') || ""}))
        dispatch(addAppData({"emailRegister" : _.get(respBody, 'email') || ""}))
        dispatch(addAppData({"phoneRegister" : _.get(respBody, 'mobile') || ""}))
        setFetchResponse(true);
        setCheckBasic(true);
        setRadioOnboard('');
        dispatch(addAppData({ "stageRegister": "accountInfo" }));
      })).catch((err => {
        let errMsg = _.get(err, 'response.data.error.message')
        if (typeof errMsg === 'string' && errMsg.includes('UnknownHostException')) {
        } else {
          toast.error(errMsg || "Internal Server Error", {
            position: toast.POSITION.TOP_CENTER
          });
        }
        setInvalidApplicantCode(true);
        setFetchResponse(true);
      })).finally(() => {
        setShowLoader(false);
      })
  }

  // const getPayor = (participantName: string) => {
  //   const participant = payorList?.find(participant => participant.participant_name === participantName);
  //   console.log("participant", participant);
  //   if (participant) {
  //     setPayor(participant)
  //   }
  // }

  const onSubmit = () => {
    setShowLoader(true);
    const jwtToken = _.get(queryString.parse(window.location.search), "jwt_token");
    let formData: any[];
      if (isJWTPresent) {
        formData = [{ "type": "onboard-through-jwt", "jwt": jwtToken, additionalVerification: fields, "participant": { "participant_name": org, "primary_email": email, "primary_mobile": phoneNumber, "roles": ["provider"] } }];
      } else if (_.get(appData,"roleSelectedRegister") !== 'provider') {
        formData = [{ "participant": { "participant_name": _.get(appData,"organizationNameRegister"), "primary_email": _.get(appData,"emailRegister"), "primary_mobile": _.get(appData,"phoneRegister"), "roles": [_.get(appData,"roleSelectedRegister")] } }];
      } else if ( _.get(appData,"roleSelectedRegister") == "provider" && _.get(appData,"applicantCodeRegister") != "") {
        formData = [{ "type": "onboard-through-verifier", "verifier_code": _.get(appData,"payorSelectedCodeRegister"), "applicant_code": _.get(appData,"applicantCodeRegister") , additionalVerification: fields, "participant": { "participant_name": _.get(appData,"organizationNameRegister"), "primary_email": _.get(appData,"emailRegister"), "primary_mobile": _.get(appData,"phoneRegister"), "roles": _.get(appData,"providerOptions")}}];
      } else if (_.get(appData,"roleSelectedRegister") == "provider" && _.get(appData,"applicantCodeRegister") == "") {
        formData = [{ "type": "onboard-through-verifier", "verifier_code": mockPayorCode, "applicant_code": String(Math.floor(Math.random() * 123456789)), additionalVerification: fields, "participant": { "participant_name": _.get(appData,"organizationNameRegister"), "primary_email": _.get(appData,"emailRegister"), "primary_mobile": _.get(appData,"phoneRegister"), "roles":  _.get(appData,"providerOptions") } }];
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
            if (data["data"]["result"]["is_user_exists"]) {
              dispatch(addAppData({"stageRegister":"onboardingSuccess"}))
            } else {
              dispatch(addAppData({"stageRegister":"setPassword"}))
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
            setShowLoader(false);
          }, 500);
        })
  }

  const resetPassword = () => {
    console.log("we are here to set the password");
        let osOwner;
        setShowLoader(true);
        setCheckResetPass(true);
        serachUser(_.get(appData,"emailRegister")).then((res: any) => {
          osOwner = res["data"]["users"][0]["osOwner"];
          setUserPassword(osOwner[0], _.get(appData,"passOneRegister")).then((async function () {
            generateTokenUser(_.get(appData,"emailRegister"), _.get(appData,"passOneRegister")).then((res: any) => {
              dispatch(addParticipantToken(res as string));
              sessionStorage.setItem('hcx_user_token', res as string);
            })
            //navigate("/onboarding/dashboard");
            dispatch(addAppData({"stageRegister":"onboardingSuccess"}));
            dispatch(addAppData({"username":_.get(appData,"emailRegister")}));
            sessionStorage.setItem('hcx_user_name', _.get(appData,"emailRegister") || "" );

          })).catch(err => {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
              position: toast.POSITION.TOP_CENTER
            });
          }).finally(() => {
            setSending(false);
          })
        });

        getParticipant(_.get(appData,"emailRegister")).then((res: any) => {
          osOwner = res["data"]["participants"][0]["osOwner"];
          dispatch(addParticipantDetails(res["data"]["participants"][0]));
        })
        setShowLoader(false);
  }


  const showInviteUser = () => {
    setStage("inviteUser");
    dispatch(addAppData({"stageRegister":"inviteUser"}))
  }

  return (
    <>
      {showLoader ? <Loader></Loader> : null }
      <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex flex-wrap items-center">
          <div className="hidden w-full xl:block xl:w-1/2">
            <div className="py-17.5 px-26 text-center">
              <Link className="mb-5.5 inline-block" to="/">
                <img className="hidden dark:block w-48" src={Logo} alt="Logo" />
                <img className="dark:hidden w-48" src={Logo} alt="Logo" />
              </Link>
              <p className="2xl:px-20 font-bold text-xl text-black dark:text-white">
                HCX Self Service Portal
              </p>
              <span className="mt-15 inline-block">
                <img className="block" src={HcxImage} alt="Logo" />
              </span>
            </div>
          </div>

          <div className="w-full border-stroke dark:border-strokedark xl:w-1/2 xl:border-l-2">
            <div className="w-full p-4 sm:p-12.5 xl:p-17.5">
              <div className="text-center w-full xl:hidden">
              <Link className="mb-5.5 inline-block" to="#">
                <img className="hidden dark:block w-48" src={Logo} alt="Logo" />
                <img className="dark:hidden w-48" src={Logo} alt="Logo" />
              </Link>
              </div>
              <h2 className="mb-9 text-2xl font-bold text-black dark:text-white sm:text-title-xl2">
                Sign Up to HCX
              </h2>
              <form>
                <div className="mb-4">
                <Stepper stage={stage}></Stepper>
                </div>
                {stage == "roleSelection" ? 
                <RoleSelection payorList={payorList} onRoleSubmit={onRoleSelection}></RoleSelection>
                : null }
                {stage == "accountInfo" ? 
                 <BasicInformation onInfoSubmit={onInformationSubmit}></BasicInformation>
                : null}
                {stage == "setPassword" ? 
                 <SetPassword createPassword={resetPassword}></SetPassword>
                : null}
                {stage == "onboardingSuccess" ?
                  <OnboardingSuccess inviteUser={showInviteUser}></OnboardingSuccess>
                : null}     
                {stage == "inviteUser" ?
                <InviteUserRegister></InviteUserRegister>
                : null }
              </form>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default SignUp;
