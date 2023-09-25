import React from 'react';
import successImage from '../../images/Group 49576.png';
import { useNavigate } from 'react-router-dom';

const Success = () => {
  const navigate = useNavigate();
  return (
    <>
      <div className="flex-col justify-center p-5">
        <img className="m-auto" src={successImage} alt="success icon" />
      </div>
      <p className="text-center">Bank details submitted successfully.</p>
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

export default Success;
