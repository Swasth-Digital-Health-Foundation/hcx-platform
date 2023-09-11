import { Link, useNavigate } from 'react-router-dom';
import Logo from '../../images/swasth_logo.png';

const OTP = () => {
  const navigate = useNavigate();
  const formSubmit = (event: any) => {
    event.preventDefault();
    navigate('/verify-otp');
  };
  return (
    <div className="w-full bg-white border-stroke dark:border-strokedark xl:w-1/2 xl:border-l-2">
      <div className="w-full p-4 sm:p-12.5 xl:p-17.5">
        <Link
          className="mb-5.5 inline-block md:block lg:block lg:hidden"
          to="#"
        >
          <img className="w-48 dark:hidden" src={Logo} alt="Logo" />
        </Link>
        <h1 className="mb-5 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
          Welcome
        </h1>
        {/* <span className="mb-1.5 block font-medium">Start for free</span> */}
        <h2 className="mb-9 text-2xl font-bold text-black dark:text-white sm:text-title-xl2">
          Please sign in to your account.
        </h2>

        <form onSubmit={formSubmit}>
          <div className="mb-6">
            <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
              Please enter your mobile no. to sign in :
            </label>
            <div className="relative">
              <input
                type="number"
                placeholder="10-digit mobile no."
                className={
                  'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                }
              />
            </div>
          </div>

          <div className="mb-5">
            <button
              type="submit"
              className="align-center flex w-full justify-center rounded bg-primary py-4 font-medium text-gray"
            >
              Send OTP
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default OTP;
