import { useLocation, useNavigate } from 'react-router-dom';
import Html5QrcodePlugin from '../../components/Html5QrcodeScannerPlugin/Html5QrcodeScannerPlugin';
import { useEffect, useState } from 'react';
import ActiveClaimCycleCard from '../../components/ActiveClaimCycleCard';
import strings from '../../utils/strings';
import { generateOutgoingRequest } from '../../services/hcxMockService';

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [qrCodeData, setQrCodeData] = useState<any>();
  const onNewScanResult = (decodedText: any, decodedResult: any) => {
    console.log(decodedResult)
    setQrCodeData(decodedText);
  };
  if (qrCodeData !== undefined) {
    let obj = JSON.parse(qrCodeData);
    navigate('/coverage-eligibility-request', {
      state: { obj: obj, filters: location.state },
    });
  }
  const mobile = location?.state?.filters?.mobile?.eq;

  const requestPayload = {
    mobile: '6363062395',
  };

  useEffect(() => {
    const getActivePlans = async () => {
      try {
        let response = await generateOutgoingRequest(
          'claim/list',
          requestPayload
        );
        console.log(response);
      } catch (err) {
        console.log(err);
      }
    };
    getActivePlans();
  }, []);

  const response = {
    preauth: [],
    'Total count': 4,
    claim: [
      {
        date: '1695188829847',
        insurance_id: null,
        claimType: 'OPD',
        claimID: 'df6da60e-5bd9-475d-8d96-ffc23e52566a',
        status: 'Pending',
      },
      {
        date: '1695188992056',
        insurance_id: null,
        claimType: 'OPD',
        claimID: '4e03b18b-e39a-40e2-85af-aa5e5548a784',
        status: 'Pending',
      },
      {
        date: '1695208599972',
        insurance_id: null,
        claimType: 'OPD',
        claimID: '9cd885ef-fe87-4330-8b18-cc32f84622f2',
        status: 'Pending',
      },
    ],
    coverageEligibility: [
      {
        date: '1695188541776',
        insurance_id: '12345678',
        claimType: 'OPD',
        claimID: '9fef61c7-d1f8-4062-a364-fa64f983d3c2',
        status: 'Pending',
      },
    ],
  };
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
        {response.claim.map((ele: any) => {
          return (
            <div className="mt-2">
              <ActiveClaimCycleCard
                date={ele.date}
                insurance_id={ele.insurance_id}
                claimType={ele.claimType}
                claimId={ele.claimId}
                status={ele.status}
                link="/initiate-claim-request"
              />
            </div>
          );
        })}
        <div className="mt-2">
          {response.coverageEligibility.map((ele: any) => {
            return (
              <div className="mt-2">
                <ActiveClaimCycleCard
                  date={ele.date}
                  insurance_id={ele.insurance_id}
                  claimType={ele.claimType}
                  claimId={ele.claimId}
                  status={ele.status}
                  link="/coverage-eligibility"
                />
              </div>
            );
          })}
        </div>
        <div className="mt-2">
          {response.preauth.map((ele: any) => {
            return (
              <div className="mt-2">
                <ActiveClaimCycleCard
                  date={ele.date}
                  insurance_id={ele.insurance_id}
                  claimType={ele.claimType}
                  claimId={ele.claimId}
                  status={ele.status}
                  link="/initiate-preauth-request"
                />
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default Home;
