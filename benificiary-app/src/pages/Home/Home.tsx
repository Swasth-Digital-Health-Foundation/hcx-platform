import qrCodeImage from '../../images/googleQRcodes.png';
import ClaimCycleTable from '../../components/ClaimCycleTable';
import { useNavigate } from 'react-router-dom';

const Home = () => {
  const navigate = useNavigate();
  return (
    <div>
      <div className="flex justify-between">
        <div className="">
          <h1 className="text-1xl font-bold text-black dark:text-white">
            Welcome, Ajit
          </h1>
        </div>
        <div>
          <a className="underline cursor-pointer">+ Add health plan</a>
        </div>
      </div>
      <div className="mt-2">
        <div className="qr-code p-1">
          <img src={qrCodeImage} alt="QR scanner" />
        </div>
        <p className="mt-1">Scan provider QR code to verify coverage</p>
        <p className="mt-3 font-bold text-black">OR</p>
        <div className="mt-3">
          <a
            className="underline cursor-pointer"
            onClick={() => {
              navigate('/new-claim');
            }}
          >
            Click here to submit a new claim
          </a>
        </div>
      </div>
      <div className="border-gray-300 my-4 border-t "></div>
      <div className="mt-3">
        <div className="mb-3">
          <h1 className="text-2xl font-bold text-black dark:text-white">
            Active claim cycle
          </h1>
        </div>
        <ClaimCycleTable />
      </div>
    </div>
  );
};

export default Home;
