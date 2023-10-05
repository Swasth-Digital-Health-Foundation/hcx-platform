import { useLocation, useNavigate } from 'react-router-dom';
import successImage from '../images/Verified-512.png';

const CoverageEligibilitySuccessPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const mobile = location.state?.mobileNumber;

  return (
    <>
      <div className="flex-col justify-center p-5">
        <img
          className="m-auto"
          src={successImage}
          alt="success icon"
          width={150}
        />
      </div>
      <p className="text-center">
        Congratulations on your approved coverage eligibility! You are now
        eligible to access the claims benefits through this provider. Please
        proceed with filing a claim or explore the pre-authorization limit for
        the treatment/services.
      </p>
      <button
        onClick={(event: any) => {
          event.preventDefault();
          navigate('/home', { state: mobile });
        }}
        type="submit"
        className="align-center mt-8 flex w-full justify-center rounded bg-primary py-3 font-medium text-gray"
      >
        Home
      </button>
    </>
  );
};

export default CoverageEligibilitySuccessPage;
