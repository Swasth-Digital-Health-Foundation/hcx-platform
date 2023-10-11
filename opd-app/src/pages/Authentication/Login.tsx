import { Link, useNavigate } from 'react-router-dom';
import Logo from '../../images/swasth_logo.png';
import { useState } from 'react';
import { toast } from 'react-toastify';
import LoadingButton from '../../components/LoadingButton';
import { sendOTP } from '../../services/hcxMockService';
import * as _ from 'lodash';
import strings from '../../utils/strings';
import { generateToken } from '../../services/hcxService';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [isValid, setIsValid] = useState(true);
  const [loading, setLoading] = useState(false);

  // const payload = {
  //   mobile: mobileNumber,
  // };

  // localStorage.setItem('mobile', mobileNumber);

  // const formSubmit = async () => {
  //   try {
  //     setLoading(true);
  //     let response = await sendOTP(payload);
  //     if (response.status === 200) {
  //       toast.success('OTP sent successfully!');
  //       navigate('/verify-otp', { state: mobileNumber });
  //       setLoading(false);
  //     }
  //   } catch (err: any) {
  //     setLoading(false);
  //     toast.error(_.get(err, 'response.data.error.message'));
  //   }
  // };

  // const handleMobileNumberChange = (e: any) => {
  //   const inputValue = e.target.value;
  //   // Check if the input contains exactly 10 numeric characters
  //   const isValidInput = /^\d{10}$/.test(inputValue);
  //   setIsValid(isValidInput);
  //   setMobileNumber(inputValue);
  // };
  let payload = {
    username: email,
    password: password,
  };
  localStorage.setItem('email', email);

  const userLogin = async () => {
    try {
      setLoading(true);
      const loginResponse = await generateToken(payload);
      let token = loginResponse.data?.access_token;
      localStorage.setItem('token', token);
      if (loginResponse.status === 200) {
        toast.success('Logged in successfully!');
        navigate('/home');
      }
    } catch (err: any) {
      setLoading(false);
      toast.error('Please check user credentials!');
    }
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
        <h1 className="mb-5 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
          {strings.WELCOME}
        </h1>
        <h2 className="mb-9 text-2xl font-bold text-black dark:text-white sm:text-title-xl2">
          {strings.SIGNIN}
        </h2>

        <form>
          <div className="mb-6">
            <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
              Please enter your email id to sign in :
            </label>
            <div className="relative">
              <input
                onChange={(e: any) => setEmail(e.target.value)}
                type="email"
                placeholder="Enter email address"
                className={`border ${
                  isValid ? 'border-stroke' : 'border-red'
                } w-full rounded-lg bg-transparent py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
              />
            </div>
            <label className="mb-2.5 mt-2 block text-left font-medium text-black dark:text-white">
              Enter your password :
            </label>
            <div className="relative">
              <input
                onChange={(e: any) => {
                  setPassword(e.target.value);
                }}
                type="text"
                placeholder="Enter password"
                className={`border ${
                  isValid ? 'border-stroke' : 'border-red'
                } w-full rounded-lg bg-transparent py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
              />
            </div>
          </div>

          <div className="mb-5">
            {!loading ? (
              <button
                type="submit"
                className="align-center flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
                // onClick={formSubmit}
                disabled={email === '' || password === ''}
                onClick={(e: any) => {
                  e.preventDefault();
                  userLogin();
                }}
              >
                Sign In
              </button>
            ) : (
              <LoadingButton className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed" />
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default Login;
