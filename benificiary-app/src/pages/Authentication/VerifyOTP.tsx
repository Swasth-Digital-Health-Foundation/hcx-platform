import { Link, useLocation, useNavigate } from 'react-router-dom';
import Logo from '../../images/swasth_logo.png';
import { useState } from 'react';
import { toast } from 'react-toastify';
import { postRequest } from '../../services/networkService';
import LoadingButton from '../../components/LoadingButton';

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

  const userExist = async () => {
    try {
      setLoading(true);
      const response: any = await postRequest('search', filter);
      if (response.data.length === 0) {
        navigate('/signup');
        setLoading(false);
      } else {
        setTimeout(() => {
          navigate('/home', { state: filter });
          setLoading(false)
        }, 1000);
      }
    } catch (error: any) {
      setLoading(false);
      toast.error(error.response.data.params.errmsg, {
        position: toast.POSITION.TOP_CENTER,
      });
    }
  };

  const handleOTPchange = (e: any) => {
    const inputValue = e.target.value;
    // Check if the input contains exactly 10 numeric characters
    const isValidInput = /^\d{6}$/.test(inputValue);
    setIsValid(isValidInput);
    setOTP(inputValue);
  };

  return (
    <div className="w-full border-stroke bg-white dark:border-strokedark xl:w-1/2 xl:border-l-2">
      <div className="w-full p-4 sm:p-12.5 xl:p-17.5">
        <Link
          className="mb-5.5 inline-block md:block lg:block lg:hidden"
          to="#"
        >
          <img className="w-48 dark:hidden" src={Logo} alt="Logo" />
        </Link>
        <div
          className="-mt-4 mb-2 cursor-pointer text-3xl"
          onClick={() => {
            navigate(-1);
          }}
        >
          &#11013;
        </div>
        <h1 className="mb-5 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
          Welcome
        </h1>
        <h2 className="mb-9 text-2xl font-bold text-black dark:text-white sm:text-title-xl2">
          Please sign in to your account.
        </h2>

        <form>
          <div className="mb-6">
            <div>
              <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                OTP sent to below mobile number
              </label>
              <div className="relative">
                <input
                  type="number"
                  value={location.state}
                  placeholder="10-digit mobile no."
                  disabled
                  className={
                    'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                  }
                />
              </div>
            </div>
            <div className="mt-5">
              <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                Enter OTP
              </label>
              <div className="relative">
                <input
                  onChange={handleOTPchange}
                  type="number"
                  placeholder="OTP"
                  className={`border ${
                    isValid ? 'border-stroke' : 'border-red'
                  } w-full rounded-lg bg-transparent py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
                />
                <p className="mt-2 text-right underline">
                  <a
                    onClick={() => {
                      toast.success('OTP sent successfully!');
                    }}
                  >
                    Resend OTP
                  </a>
                </p>
              </div>
            </div>
          </div>

          <div className="mb-5">
            {!loading ? (
              <button
                onClick={(event: any) => {
                  event.preventDefault();
                  if (OTP === '123456') {
                    // navigate('/home');
                    userExist();
                  } else {
                    toast.error('Enter valid OTP!');
                  }
                }}
                disabled={!isValid || OTP === null}
                type="submit"
                className="align-center flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
              >
                Sign In
              </button>
            ) : (
              <LoadingButton />
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default VerifyOTP;
