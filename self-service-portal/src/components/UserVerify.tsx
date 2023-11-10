import { Link, useNavigate } from 'react-router-dom';
import Logo from '../../src/images/hcx/swasth_logo.png';
import HcxImage from '../../src/images/hcx/banner.svg';
import { useSelector, useDispatch } from 'react-redux';
import _ from 'lodash';
import queryString from 'query-string';
import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { RootState } from '../store';
import { setUserPassword, generateTokenUser } from '../api/KeycloakService';
import { serachUser, userInviteAccept, userInviteReject } from '../api/UserService';
import { addParticipantToken } from '../reducers/token_reducer';
import { showError } from '../utils/Error';
import Loader from '../common/Loader';

const UserVerify = () => {

    const dispatch = useDispatch();
    const navigate = useNavigate();
    const participantToken = useSelector((state: RootState) => state.tokenReducer.participantToken);
    const [invitedBy, setInvitedBy] = useState('');
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [participantCode, setParticipantCode] = useState('');
    const [role, setRole] = useState('');
    const [password, setPassword] = useState('');
    const [showLoader, setShowLoader] = useState(false);
    const [userError, setUserError] = useState(false);
    const [passError, setPassError] = useState(false);
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
        const jwtToken = _.get(queryString.parse(window.location.search), "jwt_token");
        setEmail(getPayload(jwtToken) != null ? getPayload(jwtToken).email : "");
        setInvitedBy(getPayload(jwtToken) != null ? getPayload(jwtToken).invited_by : "");
        setParticipantCode(getPayload(jwtToken) != null ? getPayload(jwtToken).participant_code : "");
        setRole(getPayload(jwtToken) != null ? getPayload(jwtToken).role : "");
        serachUser(getPayload(jwtToken).email).then((res: any) => {
            let osOwner = _.get(res, "data.users[0].osOwner");
            console.log("oswoner", osOwner);
            if (osOwner !== undefined) {
                console.log("user is created")
                setExistingOwner(true);
            } else {
                setExistingOwner(false);
            }
        })
        console.log("Set show pass", setpass, existingOwner);
    }, [])

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
            "user_name": fullname,
            "email": email,
            "mobile": phone ? phone : '9999999999',
            "tenant_roles": [
                {
                    "participant_code": participantCode,
                    "role": role
                }
            ],
            "created_by": invitedBy
        };
        userInviteAccept(_.get(queryString.parse(window.location.search), "jwt_token"), user).then((res: any) => {
            setSubmitted(true);
            toast.success("You have successfully accepted the invite. Please login to continue");
            if (existingOwner) {
                navigate("/onboarding/login");
            } else {
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
            "email": email,
        };
        userInviteReject(_.get(queryString.parse(window.location.search), "jwt_token"), user).then((res: any) => {
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
        <>
            <div className="rounded-sm h-screen border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
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
                            {/* <span className="mb-1.5 block font-medium">Start for free</span> */}
                            <h2 className="mb-9 text-2xl font-bold text-black dark:text-white sm:text-title-xl2">
                                Please acknowledge your user invite
                            </h2>

                            <form>
                                {setpass == false ?
                                    <>
                                        <div className="mb-4 flex flex-wrap">
                                            <label className="mb-2.5 w-1/3 block font-medium text-black dark:text-white">
                                                Recipient Email:
                                            </label>
                                            <label className="mb-2.5 w-2/3 block font-medium dark:text-white">
                                                {email}
                                            </label>
                                        </div>
                                        <div className="mb-4 flex flex-wrap">
                                            <label className="mb-2.5 w-1/3 block font-medium text-black dark:text-white">
                                                Inviter Email:
                                            </label>
                                            <label className="mb-2.5 w-2/3 block font-medium dark:text-white">
                                                {invitedBy}
                                            </label>
                                        </div>
                                        <div className="mb-4">
                                            <label className="mb-2.5 block font-medium text-black dark:text-white">
                                                Inviter Participant Code:
                                            </label>
                                            <label className="mb-2.5 w-2/3 block font-medium dark:text-white">
                                                {participantCode}
                                            </label>
                                        </div>
                                        {!existingOwner? <>
                                            <div className="mb-4">
                                                <label className="mb-2.5 block font-medium text-black dark:text-white">
                                                    Email
                                                </label>
                                                <div className="relative">
                                                    <input
                                                        type="email"
                                                        placeholder="Enter your email"
                                                        className="w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                                                        value={fullname}
                                                        onChange={(event) => { setName(event.target.value); }}
                                                    />

                                                    <span className="absolute right-4 top-4">
                                                        <svg
                                                            className="fill-current"
                                                            width="22"
                                                            height="22"
                                                            viewBox="0 0 22 22"
                                                            fill="none"
                                                            xmlns="http://www.w3.org/2000/svg"
                                                        >
                                                            <g opacity="0.5">
                                                                <path
                                                                    d="M19.2516 3.30005H2.75156C1.58281 3.30005 0.585938 4.26255 0.585938 5.46567V16.6032C0.585938 17.7719 1.54844 18.7688 2.75156 18.7688H19.2516C20.4203 18.7688 21.4172 17.8063 21.4172 16.6032V5.4313C21.4172 4.26255 20.4203 3.30005 19.2516 3.30005ZM19.2516 4.84692C19.2859 4.84692 19.3203 4.84692 19.3547 4.84692L11.0016 10.2094L2.64844 4.84692C2.68281 4.84692 2.71719 4.84692 2.75156 4.84692H19.2516ZM19.2516 17.1532H2.75156C2.40781 17.1532 2.13281 16.8782 2.13281 16.5344V6.35942L10.1766 11.5157C10.4172 11.6875 10.6922 11.7563 10.9672 11.7563C11.2422 11.7563 11.5172 11.6875 11.7578 11.5157L19.8016 6.35942V16.5688C19.8703 16.9125 19.5953 17.1532 19.2516 17.1532Z"
                                                                    fill=""
                                                                />
                                                            </g>
                                                        </svg>
                                                    </span>
                                                </div>
                                            </div>

                                            <div className="mb-4">
                                                <label className="mb-2.5 block font-medium text-black dark:text-white">
                                                    Phone
                                                </label>
                                                <div className="relative">
                                                    <input
                                                        type="tel"
                                                        placeholder="Enter your full name"
                                                        className="w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                                                        value={phone}
                                                        onChange={(event) => { setPhone(event.target.value); }}
                                                    />

                                                    <span className="absolute right-4 top-4">
                                                        <svg
                                                            className="fill-current"
                                                            width="22"
                                                            height="22"
                                                            viewBox="0 0 22 22"
                                                            fill="none"
                                                            xmlns="http://www.w3.org/2000/svg"
                                                        >
                                                            <g opacity="0.5">
                                                                <path
                                                                    d="M405.333,298.667c-38.4,0-76.8-14.933-106.667-44.8c-17.067-17.067-42.667-17.067-59.733,0   c-29.867,29.867-68.267,44.8-106.667,44.8c-64,0-123.733-34.133-170.667-85.333C8.533,189.867,0,162.133,0,128   C0,57.6,57.6,0,128,0c34.133,0,61.867,8.533,84.267,25.6c12.8,8.533,25.6,17.067,38.4,25.6c10.667,6.4,25.6,6.4,34.133,0   c12.8-8.533,25.6-17.067,38.4-25.6c22.4-17.067,50.133-25.6,81.067-25.6c70.4,0,128,57.6,128,128   c0,34.133-8.533,61.867-25.6,84.267C440.533,264.533,419.2,298.667,405.333,298.667z"
                                                                />
                                                                <path
                                                                    d="M13.2352 11.0687H8.76641C5.08828 11.0687 2.09766 14.0937 2.09766 17.7719V20.625C2.09766 21.0375 2.44141 21.4156 2.88828 21.4156C3.33516 21.4156 3.67891 21.0719 3.67891 20.625V17.7719C3.67891 14.9531 5.98203 12.6156 8.83516 12.6156H13.2695C16.0883 12.6156 18.4258 14.9187 18.4258 17.7719V20.625C18.4258 21.0375 18.7695 21.4156 19.2164 21.4156C19.6633 21.4156 20.007 21.0719 20.007 20.625V17.7719C19.9039 14.0937 16.9133 11.0687 13.2352 11.0687Z"
                                                                    fill=""
                                                                />
                                                            </g>
                                                        </svg>
                                                    </span>
                                                </div>
                                            </div>
                                        </> : null}

                                        <div className="my-10 flex gap-2 justify-between">
                                            <input
                                                type="submit"
                                                value="Accept"
                                                className="w-1/3 cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
                                                onClick={(event) => { event.preventDefault(); Verify(); }}
                                            />
                                            <input
                                                type="submit"
                                                value="Decline"
                                                className="w-1/3 cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
                                                onClick={(event) => { event.preventDefault(); Reject() }}
                                            />
                                        </div>
                                    </> :
                                <>
                                <div className="mb-4">
                                          <label className="mb-2.5 block font-medium text-black dark:text-white">
                                            Password
                                          </label>
                                          <div className="relative">
                                            <input
                                              type="password"
                                              placeholder="Enter your password"
                                              className="w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                                              onChange={(event) => {setPass2(event.target.value); setPass2Error(false)}}
                                            />
                        
                                            <span className="absolute right-4 top-4">
                                              <svg
                                                className="fill-current"
                                                width="22"
                                                height="22"
                                                viewBox="0 0 22 22"
                                                fill="none"
                                                xmlns="http://www.w3.org/2000/svg"
                                              >
                                                <g opacity="0.5">
                                                  <path
                                                    d="M16.1547 6.80626V5.91251C16.1547 3.16251 14.0922 0.825009 11.4797 0.618759C10.0359 0.481259 8.59219 0.996884 7.52656 1.95938C6.46094 2.92188 5.84219 4.29688 5.84219 5.70626V6.80626C3.84844 7.18438 2.33594 8.93751 2.33594 11.0688V17.2906C2.33594 19.5594 4.19219 21.3813 6.42656 21.3813H15.5016C17.7703 21.3813 19.6266 19.525 19.6266 17.2563V11C19.6609 8.93751 18.1484 7.21876 16.1547 6.80626ZM8.55781 3.09376C9.31406 2.40626 10.3109 2.06251 11.3422 2.16563C13.1641 2.33751 14.6078 3.98751 14.6078 5.91251V6.70313H7.38906V5.67188C7.38906 4.70938 7.80156 3.78126 8.55781 3.09376ZM18.1141 17.2906C18.1141 18.7 16.9453 19.8688 15.5359 19.8688H6.46094C5.05156 19.8688 3.91719 18.7344 3.91719 17.325V11.0688C3.91719 9.52189 5.15469 8.28438 6.70156 8.28438H15.2953C16.8422 8.28438 18.1141 9.52188 18.1141 11V17.2906Z"
                                                    fill=""
                                                  />
                                                  <path
                                                    d="M10.9977 11.8594C10.5852 11.8594 10.207 12.2031 10.207 12.65V16.2594C10.207 16.6719 10.5508 17.05 10.9977 17.05C11.4102 17.05 11.7883 16.7063 11.7883 16.2594V12.6156C11.7883 12.2031 11.4102 11.8594 10.9977 11.8594Z"
                                                    fill=""
                                                  />
                                                </g>
                                              </svg>
                                            </span>
                                          </div>
                                        </div>
                        
                                        <div className="mb-6">
                                          <label className="mb-2.5 block font-medium text-black dark:text-white">
                                            Re-type Password
                                          </label>
                                          <div className="relative">
                                            <input
                                              type="password"
                                              placeholder="Re-enter your password"
                                              className="w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                                              onChange={(event) => {setPass1(event.target.value); setPass1Error(false)}}
                                            />
                                            <span className="absolute right-4 top-4">
                                              <svg
                                                className="fill-current"
                                                width="22"
                                                height="22"
                                                viewBox="0 0 22 22"
                                                fill="none"
                                                xmlns="http://www.w3.org/2000/svg"
                                              >
                                                <g opacity="0.5">
                                                  <path
                                                    d="M16.1547 6.80626V5.91251C16.1547 3.16251 14.0922 0.825009 11.4797 0.618759C10.0359 0.481259 8.59219 0.996884 7.52656 1.95938C6.46094 2.92188 5.84219 4.29688 5.84219 5.70626V6.80626C3.84844 7.18438 2.33594 8.93751 2.33594 11.0688V17.2906C2.33594 19.5594 4.19219 21.3813 6.42656 21.3813H15.5016C17.7703 21.3813 19.6266 19.525 19.6266 17.2563V11C19.6609 8.93751 18.1484 7.21876 16.1547 6.80626ZM8.55781 3.09376C9.31406 2.40626 10.3109 2.06251 11.3422 2.16563C13.1641 2.33751 14.6078 3.98751 14.6078 5.91251V6.70313H7.38906V5.67188C7.38906 4.70938 7.80156 3.78126 8.55781 3.09376ZM18.1141 17.2906C18.1141 18.7 16.9453 19.8688 15.5359 19.8688H6.46094C5.05156 19.8688 3.91719 18.7344 3.91719 17.325V11.0688C3.91719 9.52189 5.15469 8.28438 6.70156 8.28438H15.2953C16.8422 8.28438 18.1141 9.52188 18.1141 11V17.2906Z"
                                                    fill=""
                                                  />
                                                  <path
                                                    d="M10.9977 11.8594C10.5852 11.8594 10.207 12.2031 10.207 12.65V16.2594C10.207 16.6719 10.5508 17.05 10.9977 17.05C11.4102 17.05 11.7883 16.7063 11.7883 16.2594V12.6156C11.7883 12.2031 11.4102 11.8594 10.9977 11.8594Z"
                                                    fill=""
                                                  />
                                                </g>
                                              </svg>
                                            </span>
                                          </div>
                                        </div>
                                        <div className="mb-5">
                                          <input
                                            type="submit"
                                            value="Register"
                                            className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
                                            onClick={(event) => { event.preventDefault(); resetPassword();}}
                                          />
                                        </div>
                                </>   }
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
}

export default UserVerify;