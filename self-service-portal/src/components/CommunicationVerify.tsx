import { Link } from 'react-router-dom';
import Logo from '../../src/images/hcx/swasth_logo.png';
import HcxImage from '../../src/images/hcx/banner.svg';
import { useSelector, useDispatch } from 'react-redux';
import _ from 'lodash';
import queryString from 'query-string';
import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { RootState } from '../store';
import { post } from '../api/APIService';


const CommunicationVerify = () => {

    const dispatch = useDispatch()
    const participantToken = useSelector((state: RootState) => state.tokenReducer.participantToken);



    const [showLoader, setShowLoader] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [isFailed, setIsFailed] = useState(false);
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [participantName, setParticipantName] = useState("");
    const [emailVerified, setEmailVerified] = useState(false)
    const [phoneVerified, setPhoneVerified] = useState(false)

    useEffect(() => {
        const jwtToken = _.get(queryString.parse(window.location.search), "jwt_token");
        setParticipantName(getPayload(jwtToken) != null ? getPayload(jwtToken).participant_name : "");

        (async () => {
            let participantCode = getPayload(jwtToken).participant_code
            const reqBody = { filters: { participant_code: { 'eq': participantCode } } };
            await post("/applicant/search?fields=communication,sponsors", reqBody)
                .then((async function (data) {
                    let participant = _.get(data, 'data.participants')[0] || {}
                    if (participant) {
                        setEmailVerified(_.get(participant, 'communication.emailVerified') || false)
                        setPhoneVerified(_.get(participant, 'communication.phoneVerified') || false)
                    }
                })).catch((function (err) {
                    console.error(err)
                    toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                        position: toast.POSITION.TOP_CENTER
                    });
                }))
        })()

        if (_.get(queryString.parse(window.location.search), "email") != null) {

            setEmail(_.get(queryString.parse(window.location.search), "email")?.toString() || "");
        } else if (_.get(queryString.parse(window.location.search), "phone") != null) {
            setPhone(_.get(queryString.parse(window.location.search), "phone")?.toString() || "");
        }
    }, []);

    const onSubmit = () => {
        setShowLoader(true);
        const body = { "jwt_token": _.get(queryString.parse(window.location.search), "jwt_token"), "status": "successful", "comments": "Identifity Verification Done" };
        console.log(body)
        post("/applicant/verify", body).then((response => {
            setSubmitted(true);
        })).catch(err => {
            showError(_.get(err, 'response.data.error.message') || "Internal Server Error");
            setSubmitted(false);
            toast.error("Could not verify the email. Please try again or contact HCX team", {
                position: toast.POSITION.TOP_CENTER
            });
        }).finally(() => {
            setShowLoader(false);
        })
    }

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

    const showError = (errorMsg: string) => {
        toast.error(errorMsg, {
            position: toast.POSITION.TOP_CENTER,
            autoClose: 2000
        });
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
                            <div className="text-center w-full xl:hidden">
                                <Link className="mb-5.5 inline-block" to="#">
                                    <img className="hidden dark:block w-48" src={Logo} alt="Logo" />
                                    <img className="dark:hidden w-48" src={Logo} alt="Logo" />
                                </Link>
                            </div>
                            {/* <span className="mb-1.5 block font-medium">Start for free</span> */}
                            <h2 className="mb-9 text-2xl font-bold text-black dark:text-white sm:text-title-xl2 text-center">
                                HCX Onboarding Verification
                            </h2>

                            <form>
                                {submitted == false ?
                                    <>
                                        <div className="mb-4 flex flex-wrap">
                                            <label className="mb-2.5 w-1/3 block font-medium text-black dark:text-white">
                                                Participant Name:
                                            </label>
                                            <label className="mb-2.5 w-2/3 block font-medium dark:text-white">
                                                {participantName}
                                            </label>
                                        </div>


                                        {email ?
                                            <div className="mb-4 flex flex-wrap">
                                                <label className="mb-2.5 w-1/3 block font-medium text-black dark:text-white">
                                                    Email Address:
                                                </label>
                                                <label className="mb-2.5 w-2/3 block font-medium dark:text-white">
                                                    {email}
                                                </label>
                                            </div>
                                            : null}
                                        {phone ?
                                            <div className="mb-4">
                                                <label className="mb-2.5 block font-medium text-black dark:text-white">
                                                    Phone Number:
                                                </label>
                                                <label className="mb-2.5 w-2/3 block font-medium dark:text-white">
                                                    {phone}
                                                </label>
                                            </div>
                                            : null}
                                        <div className="mb-6">
                                            {email ?
                                                <label className="mb-2.5 block font-medium text-black dark:text-white">
                                                    Please verify your Email Address
                                                </label> :
                                                <label className="mb-2.5 block font-medium text-black dark:text-white">
                                                    Please verify your Phone Number
                                                </label>}
                                        </div>
                                        <div className="mb-5">
                                            <input
                                                type="submit"
                                                value="Verify"
                                                className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
                                                onClick={(event) => { event.preventDefault(); onSubmit(); }}
                                            />
                                        </div>
                                    </> :
                                    <>
                                        <div className="py-17.5 px-20 text-center">
                                            <Link className="mb-5.5 inline-block" to="#">
                                                <svg
                                                    className="fill-primary"
                                                    width="60px"
                                                    height="60px"
                                                    viewBox="0 0 48 48"
                                                    fill="none"
                                                    xmlns="http://www.w3.org/2000/svg"
                                                >
                                                    <rect width={48} height={48} fill="white" fillOpacity={0.01} />
                                                    <path
                                                        d="M24 4L29.2533 7.83204L35.7557 7.81966L37.7533 14.0077L43.0211 17.8197L41 24L43.0211 30.1803L37.7533 33.9923L35.7557 40.1803L29.2533 40.168L24 44L18.7467 40.168L12.2443 40.1803L10.2467 33.9923L4.97887 30.1803L7 24L4.97887 17.8197L10.2467 14.0077L12.2443 7.81966L18.7467 7.83204L24 4Z"
                                                        fill=""
                                                        stroke="black"
                                                        strokeWidth={2}
                                                        strokeLinecap="round"
                                                        strokeLinejoin="round"
                                                    />
                                                    <path
                                                        d="M17 24L22 29L32 19"
                                                        stroke="white"
                                                        strokeWidth={4}
                                                        strokeLinecap="round"
                                                        strokeLinejoin="round"
                                                    />
                                                </svg>
                                            </Link>
                                            <p className="2xl:px-15 py-5 font-bold text-l text-black dark:text-white">
                                                {email ? "Congratulations! Your email account has been successfully verified" :
                                                    "Congratulations! Your phone number has been successfully verified"}
                                            </p>
                                        </div>
                                        <div className="mb-5">
                                            <input
                                                type="submit"
                                                value="Proceed to Login"
                                                className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
                                                onClick={(event) => { event.preventDefault(); }}
                                            />
                                        </div>
                                    </>}
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}

export default CommunicationVerify;