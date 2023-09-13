import { useNavigate } from 'react-router-dom';
import Html5QrcodePlugin from '../../components/Html5QrcodeScannerPlugin/Html5QrcodeScannerPlugin';
import { useState } from 'react';
import ActiveClaimCycleCard from '../../components/ActiveClaimCycleCard';

const Home = () => {
  const navigate = useNavigate();
  const [qrCodeData, setQrCodeData] = useState<any>();
  const onNewScanResult = (decodedText: any, decodedResult: any) => {
    setQrCodeData(decodedText);
  };
  if (qrCodeData !== undefined) {
    let obj = JSON.parse(qrCodeData);
    navigate('/new-claim', { state: obj });
  }
  

  return (
    <div>
      <div className="flex justify-between">
        <div className="">
          <h1 className="text-1xl font-bold text-black dark:text-white">
            Welcome, Ajit
          </h1>
        </div>
        <div>
          <a className="cursor-pointer underline">+ Add health plan</a>
        </div>
      </div>
      <div className="mt-2">
        <div className="qr-code p-1">
          {/* <img src={qrCodeImage} alt="QR scanner" /> */}
          <div id="reader">
            <Html5QrcodePlugin
              fps={60}
              qrbox={250}
              disableFlip={false}
              qrCodeSuccessCallback={onNewScanResult}
            />
          </div>
        </div>
        <p className="mt-1">Scan provider QR code to verify coverage</p>
        <p className="mt-3 font-bold text-black">OR</p>
        <div className="mt-3">
          <a
            className="cursor-pointer underline"
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
        <ActiveClaimCycleCard />
      </div>
    </div>
  );
};

export default Home;
