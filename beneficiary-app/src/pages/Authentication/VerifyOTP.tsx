import { Link, useLocation, useNavigate } from 'react-router-dom';
import Logo from '../../images/swasth_logo.png';
import { useState } from 'react';
import { toast } from 'react-toastify';
import { postRequest } from '../../services/registryService';
import LoadingButton from '../../components/LoadingButton';
import { sendOTP, verifyOTP } from '../../services/hcxMockService';
import strings from '../../utils/strings';

const VerifyOTP = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [OTP, setOTP] = useState(null);
  const [isValid, setIsValid] = useState(true);
  const mobileNumber = location.state;
  const [loading, setLoading] = useState(false);

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
                {strings.OTP_SENT}
              </label>
              <div className="relative">
                <input
                  type="number"
                  value={location.state}
                  placeholder={strings.TEN_DIGIT}
                  disabled
                  className={
                    'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                  }
                />
              </div>
            </div>
            <div className="mt-5">
              <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                {strings.ENTER_OTP}
              </label>
              <div className="relative">
                <input
                  onChange={handleOTPchange}
                  type="number"
                  placeholder={strings.SIX_DIGIT}
                  className={`border ${
                    isValid ? 'border-stroke' : 'border-red'
                  } w-full rounded-lg bg-transparent py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
                />
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
          <p className="mt-2 text-center underline">
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
