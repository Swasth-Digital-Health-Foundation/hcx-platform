import { Link, useNavigate } from "react-router-dom";
import Logo from "../../images/swasth_logo.png";
import { useState } from "react";
import { toast } from "react-toastify";
import LoadingButton from "../../components/LoadingButton";
import * as _ from "lodash";
import strings from "../../utils/strings";
import { generateToken, login } from "../../services/hcxService";

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [loading, setLoading] = useState(false);

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
                type="password"
                placeholder="Enter password"
                className={`w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
              />
            </div>
          </div>

          <div className="mb-5">
            {!loading ? (
              <button
                type="submit"
                className="align-center flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
                // onClick={formSubmit}
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
