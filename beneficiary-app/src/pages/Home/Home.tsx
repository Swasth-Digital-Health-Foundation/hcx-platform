import { useLocation, useNavigate } from 'react-router-dom';
import Html5QrcodePlugin from '../../components/Html5QrcodeScannerPlugin/Html5QrcodeScannerPlugin';
import { useState } from 'react';
import ActiveClaimCycleCard from '../../components/ActiveClaimCycleCard';
import strings from '../../utils/strings';

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [qrCodeData, setQrCodeData] = useState<any>();
  const onNewScanResult = (decodedText: any, decodedResult: any) => {
    setQrCodeData(decodedText);
  };
  if (qrCodeData !== undefined) {
    let obj = JSON.parse(qrCodeData);
    navigate('/coverage-eligibility-request', {
      state: { obj: obj, filters: location.state },
    });
  }
  const mobile = location?.state?.filters?.mobile?.eq;
  return (
    <div>
      <div className="flex justify-between">
        <div className="">
          <h1 className="text-1xl font-bold text-black dark:text-white">
            {strings.WELCOME_TEXT} Ajit
          </h1>
        </div>
      </div>
      <div className="rounded-sm border border-stroke bg-white p-2 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="mt-2">
          <div className="qr-code p-1">
            <div id="reader" className="px-1">
              <Html5QrcodePlugin
                fps={60}
                qrbox={250}
                disableFlip={false}
                qrCodeSuccessCallback={onNewScanResult}
              />
            </div>
          </div>
          <p className="mt-1 text-center">{strings.SCAN_QRCODE}</p>
          <p className="mt-3 text-center font-bold text-black dark:text-gray">
            OR
          </p>
          <div className="mt-3 text-center">
            <a
              className="cursor-pointer underline"
              onClick={() => {
                navigate('/new-claim', { state: mobile });
              }}
            >
              {strings.SUBMIT_NEW_CLAIM}
            </a>
          </div>
        </div>
      </div>
      <div className="mt-3">
        <h1 className="px-1 text-2xl font-bold text-black dark:text-white">
          {strings.YOUR_ACTIVE_CYCLE} (5)
        </h1>
        <div className="border-gray-300 my-4 border-t"></div>
        <ActiveClaimCycleCard />
      </div>
    </div>
  );
};

export default Home;
