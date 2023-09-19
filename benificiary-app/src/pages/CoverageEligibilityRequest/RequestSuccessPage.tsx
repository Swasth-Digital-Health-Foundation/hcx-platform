import { useNavigate } from 'react-router-dom';
import successImage from '../../images/Group 49576.png';

const RequestSuccessPage = () => {
  const navigate = useNavigate();
  return (
    <>
      <div className="flex-col justify-center p-5">
        <img className="m-auto" src={successImage} alt="success icon" />
      </div>
      <p className="text-center">
        Congratulations! You've initiated the coverage eligibility request with
        your insurer. Your insurer will review and respond to your request. You
        can track the request status under active claims tab on homepage.
      </p>
      <button
        onClick={(event: any) => {
          event.preventDefault();
          navigate('/home');
        }}
        type="submit"
        className="align-center mt-8 flex w-full justify-center rounded bg-primary py-3 font-medium text-gray"
      >
        Home
      </button>
    </>
  );
};

export default RequestSuccessPage;
