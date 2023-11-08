import { Link, useNavigate } from "react-router-dom";
import Logo from "../../images/swasth_logo.png";
import { useState } from "react";
import { toast } from "react-toastify";
import LoadingButton from "../../components/LoadingButton";
import * as _ from "lodash";
import strings from "../../utils/strings";
import { login } from "../../services/hcxService";

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const [passwordVisible, setPasswordVisible] = useState(false);

  localStorage.setItem("email", email);

  const loginCredentials = {
    username: email,
    password: password,
  };

  const userLogin = async () => {
    try {
      setLoading(true);
      const loginResponse = await login(loginCredentials);
      if (loginResponse.status === 200) {
        toast.success("Logged in successfully!");
        navigate("/home");
      }
    } catch (err: any) {
      setLoading(false);
      toast.error("Please check user credentials!");
    }
  };

  const handleTogglePassword = () => {
    setPasswordVisible(!passwordVisible);
  };

  return (
    <div className="w-full border-stroke bg-white dark:border-strokedark">
      <div className="w-full p-4 sm:p-12.5 xl:p-17.5">
        <Link className="mb-5.5 inline-block" to="#">
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
                className={`w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
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
                type={passwordVisible ? 'text' : 'password'}
                placeholder="Enter password"
                className={`w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
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

          <div className="mb-5">
            {!loading ? (
              <button
                type="submit"
                className="align-center flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
                disabled={email === "" || password === ""}
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
