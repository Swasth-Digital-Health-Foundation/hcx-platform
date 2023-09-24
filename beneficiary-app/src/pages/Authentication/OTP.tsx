import { Link, useNavigate } from 'react-router-dom';
import Logo from '../../images/swasth_logo.png';
import { useState } from 'react';
import { toast } from 'react-toastify';
import LoadingButton from '../../components/LoadingButton';
import { sendOTP } from '../../services/hcxMockService';
import * as _ from 'lodash';
import strings from '../../utils/strings';

const OTP = () => {
  const navigate = useNavigate();
  const [mobileNumber, setMobileNumber] = useState<number>();
  const [isValid, setIsValid] = useState(true);
  const [loading, setLoading] = useState(false);

  const payload = {
    mobile: mobileNumber,
  };

  const formSubmit = async () => {
    try {
      setLoading(true);
      let response = await sendOTP(payload);
      // toast.success('OTP sent successfully!');
      navigate('/verify-otp', { state: mobileNumber });
      setLoading(false);
    } catch (err: any) {
      setLoading(false);
      toast.error(_.get(err, 'response.data.error.message'));
    }
  };

  const handleMobileNumberChange = (e: any) => {
    const inputValue = e.target.value;
    // Check if the input contains exactly 10 numeric characters
    const isValidInput = /^\d{10}$/.test(inputValue);
    setIsValid(isValidInput);
    setMobileNumber(inputValue);
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
              {strings.ENTER_MOBILE_NUMBER}
            </label>
            <div className="relative">
              <input
                onChange={handleMobileNumberChange}
                type="number"
                placeholder={strings.TEN_DIGIT}
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
                onClick={formSubmit}
                disabled={!isValid || mobileNumber === undefined}
              >
                {strings.SEND_OTP}
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

export default OTP;
