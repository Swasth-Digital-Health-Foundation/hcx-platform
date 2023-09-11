import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Logo from '../../images/swasth_logo.png';
import PayorDetailsCard from '../../components/PayorDetailsCard/PayorDetailsCard';

const SignUp = () => {
  const navigate = useNavigate();
  const [cards, setCards] = useState<any>([]);

  const addCard = () => {
    setCards([...cards, { id: cards.length + 1 }]);
  };

  return (
    <div>
      <div className="w-full border-stroke bg-white dark:border-strokedark xl:w-1/2 xl:border-l-2">
        <Link className="inline-block p-4 md:block lg:block lg:hidden" to="#">
          <img className="w-48 dark:hidden" src={Logo} alt="Logo" />
        </Link>
        <div className="w-full p-4 sm:p-12.5 xl:p-17.5">
          <h2 className="mb-4 -mt-4 text-3xl font-bold text-black dark:text-white sm:text-title-xl2">
            New user profile
          </h2>
          <h2 className="sm:text-title-xl1 mb-4 text-2xl font-bold text-black dark:text-white">
            Personal details :
          </h2>
          <form>
            <div className="mb-6">
              <div>
                <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                  User name
                </label>
                <div className="relative">
                  <input
                    type="text"
                    placeholder="Enter your name"
                    className={
                      'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                    }
                  />
                </div>
              </div>
              <div className="mt-5">
                <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                  Mobile number
                </label>
                <div className="relative">
                  <input
                    type="number"
                    placeholder="Enter mobile number"
                    className={
                      'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                    }
                  />
                </div>
              </div>
              <div className="mt-5">
                <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                  Email ID
                </label>
                <div className="relative">
                  <input
                    type="email"
                    placeholder="Enter email address"
                    className={
                      'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                    }
                  />
                  {/* <p className="mt-2 text-right underline">
                    <a>Resend OTP</a>
                  </p> */}
                </div>
              </div>
            </div>

            <h2 className="sm:text-title-xl1 mb-4 text-2xl font-bold text-black dark:text-white">
              Add insurance plan :
            </h2>

            <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
              <div className="flex flex-col gap-5.5 p-4">
                <div>
                  <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                    Payor Details *
                  </label>
                  <div className="relative z-20 bg-white dark:bg-form-input">
                    <select
                      required
                      className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input"
                    >
                      <option value="">Swast payor</option>
                    </select>
                    <span className="absolute top-1/2 right-4 z-10 -translate-y-1/2">
                      <svg
                        width="24"
                        height="24"
                        viewBox="0 0 24 24"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <g opacity="0.8">
                          <path
                            fillRule="evenodd"
                            clipRule="evenodd"
                            d="M5.29289 8.29289C5.68342 7.90237 6.31658 7.90237 6.70711 8.29289L12 13.5858L17.2929 8.29289C17.6834 7.90237 18.3166 7.90237 18.7071 8.29289C19.0976 8.68342 19.0976 9.31658 18.7071 9.70711L12.7071 15.7071C12.3166 16.0976 11.6834 16.0976 11.2929 15.7071L5.29289 9.70711C4.90237 9.31658 4.90237 8.68342 5.29289 8.29289Z"
                            fill="#637381"
                          ></path>
                        </g>
                      </svg>
                    </span>
                  </div>
                </div>
                <div>
                  <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                    Insurance ID *
                  </label>
                  <div className="relative">
                    <input
                      required
                      type="text"
                      placeholder="Insurance ID"
                      className={
                        'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                      }
                    />
                  </div>
                </div>
              </div>
            </div>

            <div>
              {cards.map((index: any) => (
                <div className="mt-3">
                  <PayorDetailsCard key={index} />
                </div>
              ))}
            </div>

            <div className="mt-4 text-right">
              <a className="underline" onClick={addCard}>
                + Add another
              </a>
            </div>

            <div className="mb-5">
              <button
                onClick={(event: any) => {
                  event.preventDefault();
                  navigate('/home');
                }}
                type="submit"
                className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray"
              >
                Save profile details
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SignUp;
