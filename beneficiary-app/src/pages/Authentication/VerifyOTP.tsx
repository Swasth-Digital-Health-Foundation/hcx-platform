import { Link, useLocation, useNavigate } from 'react-router-dom';
import Logo from '../../images/swasth_logo.png';
import { useState } from 'react';
import { toast } from 'react-toastify';
import { postRequest } from '../../services/registryService';
import LoadingButton from '../../components/LoadingButton';
import { sendOTP, verifyOTP } from '../../services/hcxMockService';
import strings from '../../utils/strings';
import maskMobileNumber from '../../utils/maskMobileNumber';



const VerifyOTP = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [OTP, setOTP] = useState(null);
  const [isValid, setIsValid] = useState(true);
  const mobileNumber = location.state;
  const [loading, setLoading] = useState(false);
  const [passwordVisible, setPasswordVisible] = useState(false);

  const filter = {
    entityType: ['Beneficiary'],
    filters: {
      mobile: { eq: mobileNumber },
    },
  };

  const verifyOTPrequestBody = {
    mobile: location.state,
    otp_code: OTP,
  };

  const userExist = async () => {
    try {
      setLoading(true);
      const otpResponse = await verifyOTP(verifyOTPrequestBody);
      if (otpResponse.status === 200) {
        const searchUser = await postRequest('/search', filter);
        if (searchUser?.data?.length !== 0) {
          toast.success('OTP verified successfully!', {
            position: toast.POSITION.TOP_CENTER,
          });
          navigate('/home', { state: filter });
        } else {
          navigate('/signup');
        }
      }
    } catch (error: any) {
      setLoading(false);
      if (error.response?.status === 400) {
        // console.log("status",error.response)
        toast.error('Enter valid OTP!', {
          position: toast.POSITION.TOP_CENTER,
        });
      } else {
        toast.error('Request timed out,try again!', {
          position: toast.POSITION.TOP_CENTER,
        });
      }
    }
  };

  const handleOTPchange = (e: any) => {
    const inputValue = e.target.value;
    const isValidInput = /^\d{6}$/.test(inputValue);
    setIsValid(isValidInput);
    setOTP(inputValue);
  };

  const mobileNumberPayload = {
    mobile: verifyOTPrequestBody.mobile,
  };

  const resendOTP = async () => {
    try {
      let response = await sendOTP(mobileNumberPayload);
      if (response.status === 200) {
        toast.success('OTP sent successfully!');
      }
    } catch (err: any) {
      toast.error('Please try again!');
    }
  };

  const handleTogglePassword = () => {
    setPasswordVisible(!passwordVisible);
  };

  return (
    <div className="w-full border-stroke bg-white dark:border-strokedark xl:w-1/2 xl:border-l-2">
      <div className="w-full p-4 sm:p-12.5 xl:p-17.5">
        <div
          className="ml-1 mb-1 flex flex-row align-middle"
          onClick={() => navigate(-1)}
        >
          <svg
            className="w-6"
            fill="currentColor"
            viewBox="0 0 20 20"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              fillRule="evenodd"
              d="M7.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l2.293 2.293a1 1 0 010 1.414z"
              clipRule="evenodd"
            ></path>
          </svg>
        </div>
        <Link
          className="mb-5.5 inline-block md:block lg:block lg:hidden"
          to="#"
        >
          <img className="w-48 dark:hidden" src={Logo} alt="Logo" />
        </Link>
        <h1 className="mb-5 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
          {strings.WELCOME}
        </h1>
        <h2 className="mb-9 text-2xl font-bold text-black dark:text-white sm:text-title-xl2">
          {strings.SIGNIN}
        </h2>

        <form>
          <div className="mb-6">
            <div>
              <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                {/* {strings.OTP_SENT} */}
                Please enter the 6-digit OTP sent to your mobile no. {maskMobileNumber(location.state)} below :
              </label>
            </div>
            <div className="mt-5">
              <div className="relative">
                <input
                  onChange={handleOTPchange}
                  type={passwordVisible ? 'text' : 'password'}
                  placeholder={strings.SIX_DIGIT}
                  className={`border ${isValid ? 'border-stroke' : 'border-red'
                    } w-full rounded-lg bg-transparent py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
                />
                <div className='absolute top-4 right-5' onClick={handleTogglePassword}>
                  {passwordVisible ? <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg> : <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                  </svg>
                  }
                </div>
              </div>
            </div>
          </div>

          <div className="mb-5">
            {!loading ? (
              <button
                onClick={(event: any) => {
                  event.preventDefault();
                  userExist();
                }}
                disabled={!isValid || OTP === null}
                type="submit"
                className="align-center flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
              >
                {strings.VERIFY_OTP_BUTTON}
              </button>
            ) : (
              <LoadingButton className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed" />
            )}
          </div>
          <p className="mt-2 text-end underline">
            <a
              onClick={() => {
                resendOTP();
              }}
            >
              {strings.RESEND_OTP_BUTTON}
            </a>
          </p>
        </form>
      </div>
    </div>
  );
};

export default VerifyOTP;
