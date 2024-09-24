import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { addAppData } from '../reducers/app_data';
import { toast } from 'react-toastify';

interface SetPasswordProps {
  createPassword: () => void;
}

const SetPassword: React.FC<SetPasswordProps> = ({ createPassword }: SetPasswordProps) => {
  const dispatch = useDispatch();
  const [pass1Error, setPass1Error] = useState(false);
  const [pass2Error, setPass2Error] = useState(false);
  const [pass1, setPass1] = useState('');
  const [pass2, setPass2] = useState('');
  const [showPassword1, setShowPassword1] = useState(false);
  const [showPassword2, setShowPassword2] = useState(false);

  const togglePasswordVisibility1 = () => setShowPassword1(!showPassword1);
  const togglePasswordVisibility2 = () => setShowPassword2(!showPassword2);

  const onSubmit = () => {
    if (pass1 === '' && pass2 === '') {
      setPass1Error(true);
      setPass2Error(true);
    } else {
      if (pass1 === pass2) {
        createPassword();
      } else {
        toast.error("Passwords do not match. Please provide the same password");
      }
    }
  };

  return (
    <>
      <div className="mb-4">
        <label className="mb-2.5 block font-medium text-black dark:text-white">
          Password
        </label>
        <div className="relative">
          <input
            type={showPassword1 ? "text" : "password"}
            placeholder="Enter your password"
            className={"w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary" + (pass1Error ? " !border-danger" : "")}
            onChange={(event) => { setPass1Error(false); setPass1(event.target.value); dispatch(addAppData({ "passOneRegister": event.target.value })) }}
          />
          <span className="absolute right-4 top-4 cursor-pointer" onClick={togglePasswordVisibility1}>
            {showPassword1 ? (
              <svg
                className="fill-current"
                width="22"
                height="22"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M12 6.5C10.355 6.5 9 7.855 9 9.5S10.355 12.5 12 12.5 15 11.145 15 9.5 13.645 6.5 12 6.5ZM12 11C11.448 11 11 10.552 11 10S11.448 9 12 9s1 0.448 1 1-0.448 1-1 1ZM12 3C7.134 3 3.736 5.704 1.349 9.349A1 1 0 0 0 1 10.651C3.736 14.296 7.134 17 12 17c4.866 0 9.264-2.704 11.651-6.349A1 1 0 0 0 22 10.651C19.264 5.704 14.866 3 12 3ZM12 15c-3.618 0-6.827-2.005-8.072-4.572C7.173 8.005 10.382 6 12 6c1.533 0 3.056.397 4.285 1.108-0.596.426-1.159.966-1.642 1.572C14.706 8.765 14.354 9.315 14 9.857c0.356.542 0.706 1.093 1.069 1.652.541.578 1.099 1.122 1.629 1.649A9.859 9.859 0 0 1 12 15Z" fill="" />
              </svg>
            ) : (
              <svg
                className="fill-current"
                width="22"
                height="22"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M12 5c-7.178 0-13 6.267-13 7s5.822 7 13 7 13-6.267 13-7-5.822-7-13-7ZM12 15c-3.648 0-6.945-2.001-8.241-4.581C5.055 8.003 8.353 6 12 6c2.547 0 4.953.748 7.214 2.068-1.314 1.608-3.151 3.038-5.214 3.932A8.812 8.812 0 0 1 12 15ZM12 8c-1.09 0-2.204.318-3.158.918 1.147.597 2.569 1.082 3.158 1.082 1.333 0 2.674-.766 3.666-1.712-1.125-.882-2.652-1.288-4.051-1.288Z" fill="" />
              </svg>
            )}
          </span>
        </div>
        {pass1Error ? <p className='text-danger italic'>* Please enter a valid password</p> : null}
      </div>

      <div className="mb-6">
        <label className="mb-2.5 block font-medium text-black dark:text-white">
          Re-type Password
        </label>
        <div className="relative">
          <input
            type={showPassword2 ? "text" : "password"}
            placeholder="Re-enter your password"
            className={"w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary" + (pass2Error ? " !border-danger" : "")}
            onChange={(event) => { setPass2Error(false); setPass2(event.target.value); dispatch(addAppData({ "passTwoRegister": event.target.value })) }}
          />
          <span className="absolute right-4 top-4 cursor-pointer" onClick={togglePasswordVisibility2}>
            {showPassword2 ? (
              <svg
                className="fill-current"
                width="22"
                height="22"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M12 6.5C10.355 6.5 9 7.855 9 9.5S10.355 12.5 12 12.5 15 11.145 15 9.5 13.645 6.5 12 6.5ZM12 11C11.448 11 11 10.552 11 10S11.448 9 12 9s1 0.448 1 1-0.448 1-1 1ZM12 3C7.134 3 3.736 5.704 1.349 9.349A1 1 0 0 0 1 10.651C3.736 14.296 7.134 17 12 17c4.866 0 9.264-2.704 11.651-6.349A1 1 0 0 0 22 10.651C19.264 5.704 14.866 3 12 3ZM12 15c-3.618 0-6.827-2.005-8.072-4.572C7.173 8.005 10.382 6 12 6c1.533 0 3.056.397 4.285 1.108-0.596.426-1.159.966-1.642 1.572C14.706 8.765 14.354 9.315 14 9.857c0.356.542 0.706 1.093 1.069 1.652.541.578 1.099 1.122 1.629 1.649A9.859 9.859 0 0 1 12 15Z" fill="" />
              </svg>
            ) : (
              <svg
                className="fill-current"
                width="22"
                height="22"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M12 5c-7.178 0-13 6.267-13 7s5.822 7 13 7 13-6.267 13-7-5.822-7-13-7ZM12 15c-3.648 0-6.945-2.001-8.241-4.581C5.055 8.003 8.353 6 12 6c2.547 0 4.953.748 7.214 2.068-1.314 1.608-3.151 3.038-5.214 3.932A8.812 8.812 0 0 1 12 15ZM12 8c-1.09 0-2.204.318-3.158.918 1.147.597 2.569 1.082 3.158 1.082 1.333 0 2.674-.766 3.666-1.712-1.125-.882-2.652-1.288-4.051-1.288Z" fill="" />
              </svg>
            )}
          </span>
        </div>
        {pass2Error ? <p className='text-danger italic'>* Please enter a valid password</p> : null}
      </div>

      <div className="mb-5">
        <input
          type="submit"
          value="Register"
          className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
          onClick={(event) => { event.preventDefault(); onSubmit() }}
        />
      </div>
    </>
  );
};

export default SetPassword;
