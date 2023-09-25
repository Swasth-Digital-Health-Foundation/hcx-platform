import { useLocation, useNavigate } from 'react-router-dom';
import successImage from '../images/success.png';

const RequestSuccess = () => {
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
        Congratulations! You have successfully initiated a new {' '}
        {location.state?.text} {''}
        request. You can track the request status from 'Active requests' on
        homepage.
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

export default RequestSuccess;
