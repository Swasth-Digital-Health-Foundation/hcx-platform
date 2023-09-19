import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Logo from '../../images/swasth_logo.png';
import PayorDetailsCard from '../../components/PayorDetailsCard/PayorDetailsCard';
import { postRequest } from '../../services/networkService';
import { toast } from 'react-toastify';
import LoadingButton from '../../components/LoadingButton';

const SignUp = () => {
  const navigate = useNavigate();
  const [cards, setCards] = useState<any>([]);
  const [mobileNumber, setMobileNumber] = useState<string>();
  const [userName, setUserName] = useState();
  const [email, setEmail] = useState();
  const [isValid, setIsValid] = useState(true);
  const [loading, setLoading] = useState(false);

  // Function to update card data
  const updateCardData = (cardKey: any, newData: any) => {
    const updatedCards = cards.map((card: any) =>
      card.cardKey === cardKey ? { ...card, ...newData } : card
    );
    setCards(updatedCards);
  };

  const addCard = () => {
    const cardKey = cards.length + 1;

    const newCard = {
      cardKey,
    };

    setCards([...cards, newCard]);
  };

  const removeCard = (cardToRemove: any) => {
    const updatedCards = cards.filter(
      (card: any) => card.cardKey !== cardToRemove.cardKey
    );
    setCards(updatedCards);
  };

  const [payor, setPayor] = useState<string>('Swast Payor');
  const handlePayorChange = (e: any) => {
    setPayor(e.target.value);
  };

  const [insuranceId, setInsuranceId] = useState<string>('');
  const handleInsuranceIdChange = (e: any) => {
    setInsuranceId(e.target.value);
  };

  let addMoreDetails = cards.map((ele: any) => {
    return { insurance_id: ele.insurance_id, payor: ele.payor };
  });

  let payload = {
    email: email,
    mobile: mobileNumber !== undefined ? mobileNumber.toString() : '',
    name: userName,
    payor_details: [
      {
        insurance_id: insuranceId,
        payor: payor,
      },
      ...addMoreDetails,
    ],
  };

  const registerUser = async () => {
    try {
      setLoading(true);
      let registerResponse: any = await postRequest('invite', payload);
      setLoading(false);
      toast.success('User registered successfully!', {
        position: toast.POSITION.TOP_CENTER,
      });
      navigate('/home');
    } catch (error: any) {
      setLoading(false);
      toast.error(error.response.data.params.errmsg, {
        position: toast.POSITION.TOP_CENTER,
      });
    }
  };

  const handleMobileNumberChange = (e: any) => {
    const inputValue = e.target.value;
    // Check if the input contains exactly 10 numeric characters
    const isValidInput = /^\d{10}$/.test(inputValue);
    console.log(isValidInput);
    setIsValid(isValidInput);
    setMobileNumber(inputValue);
  };

  const handleUserNameChange = (e: any) => {
    setUserName(e.target.value);
  };

  const handleEmailChange = (e: any) => {
    setEmail(e.target.value);
  };

  const insuranceCheck = insuranceId === '';
  const payorCheck = payor === ('' || 'none' || null);
  const numberCheck = mobileNumber?.length !== 10;
  const handleDisable = () => {
    if (numberCheck || insuranceCheck || payorCheck) {
      return true;
    }
    return false;
  };

  return (
    // <div>
    <div className="w-full border-stroke bg-white p-2 dark:border-strokedark xl:w-1/2 xl:border">
      <Link className="inline-block px-4 md:block lg:block lg:hidden" to="#">
        <img className="w-48 dark:hidden" src={Logo} alt="Logo" />
      </Link>
      {/* <div className="flex flex-row align-middle" onClick={() => navigate(-1)}>
        <svg
          className="mb-2 -mt-2 w-6"
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
      </div> */}
      <h2 className="sm:text-title-xl1 mb-4 text-2xl font-bold text-black dark:text-white">
        Add profile details :
      </h2>
      <div className="w-full rounded-sm border border-stroke bg-white p-4 shadow-default dark:border-strokedark dark:bg-boxdark sm:p-12.5 xl:p-17.5">
        <form>
          <div className="mb-6">
            <div>
              <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                User's name
              </label>
              <div className="relative">
                <input
                  onChange={handleUserNameChange}
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
                Mobile number *
              </label>
              <div className="relative">
                <input
                  onChange={handleMobileNumberChange}
                  type="number"
                  placeholder="Enter mobile number"
                  className={`border ${
                    isValid ? 'border-stroke' : 'border-red'
                  } w-full rounded-lg bg-transparent py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
                />
              </div>
            </div>
            <div className="mt-5">
              <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
                Email ID
              </label>
              <div className="relative">
                <input
                  onChange={handleEmailChange}
                  type="email"
                  placeholder="Enter email address"
                  className={
                    'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
                  }
                />
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
                    onChange={handlePayorChange}
                    className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input"
                  >
                    <option value="Swast payor">Swast payor</option>
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
                    onChange={handleInsuranceIdChange}
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
            {cards.map((card: any) => (
              <div className="relative mt-3" key={card.id}>
                <button
                  onClick={(event: any) => {
                    event.preventDefault();
                    removeCard(card);
                  }}
                  className="absolute right-5 mt-3 flex rounded bg-gray px-2 text-black dark:text-white"
                >
                  -
                </button>
                <PayorDetailsCard
                  onInputChange={(newData: any) =>
                    updateCardData(card.cardKey, newData)
                  }
                  cardKey={card.cardKey}
                />
              </div>
            ))}
          </div>

          <div className="mt-4 text-right">
            <a className="underline" onClick={addCard}>
              + Add another
            </a>
          </div>
        </form>
      </div>
      <div className="mb-5">
        {!loading ? (
          <button
            disabled={handleDisable()}
            onClick={(event: any) => {
              event.preventDefault();
              // navigate('/home');
              // submitData();
              registerUser();
            }}
            type="submit"
            className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
          >
            Save profile details
          </button>
        ) : (
          <LoadingButton />
        )}
      </div>
    </div>
    // </div>
  );
};

export default SignUp;
