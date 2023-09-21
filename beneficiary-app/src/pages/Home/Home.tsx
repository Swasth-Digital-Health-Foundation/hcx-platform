import { useLocation, useNavigate } from 'react-router-dom';
import Html5QrcodePlugin from '../../components/Html5QrcodeScannerPlugin/Html5QrcodeScannerPlugin';
import { useEffect, useState } from 'react';
import ActiveClaimCycleCard from '../../components/ActiveClaimCycleCard';
import strings from '../../utils/strings';
import { generateOutgoingRequest } from '../../services/hcxMockService';
import { postRequest } from '../../services/registryService';
import * as _ from 'lodash';

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [qrCodeData, setQrCodeData] = useState<any>();
  const [activeRequests, setActiveRequests] = useState<any>();
  const [userInformation, setUserInformation] = useState<any>([]);
  const [loading, setLoading] = useState(false);

  const [mobileNumber, setMobileNumber] = useState<string>(
    _.get(location, 'state.filters.mobile.eq')
  );
  const onNewScanResult = (decodedText: any, decodedResult: any) => {
    setQrCodeData(decodedText);
  };

  if (qrCodeData !== undefined) {
    let obj = JSON.parse(qrCodeData);
    navigate('/coverage-eligibility-request', {
      state: { obj: obj, mobile: location.state },
    });
  }

  const requestPayload = {
    mobile: mobileNumber || location.state,
  };

  const filter = {
    entityType: ['Beneficiary'],
    filters: {
      mobile: { eq: requestPayload.mobile },
    },
  };

  // console.log(filter);
  // console.log(location.state);
  // console.log(activeRequests);

  useEffect(() => {
    const getActivePlans = async () => {
      try {
        setLoading(true);
        let response = await generateOutgoingRequest(
          'bsp/request/list',
          requestPayload
        );
        setLoading(false);
        setActiveRequests(response.data);
      } catch (err) {
        setLoading(false);
        console.log(err);
      }
    };
    const search = async () => {
      try {
        const searchUser = await postRequest('/search', filter);
        setUserInformation(searchUser.data);
      } catch (error) {
        console.log(error);
      }
    };
    search();
    getActivePlans();
  }, []);

  return (
    <div>
      <div className="flex justify-between">
        <div className="">
          <h1 className="text-1xl font-bold text-black dark:text-white">
            {strings.WELCOME_TEXT} {userInformation[0]?.name || ''}
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
                navigate('/new-claim', { state: mobileNumber });
              }}
            >
              {strings.SUBMIT_NEW_CLAIM}
            </a>
          </div>
        </div>
      </div>
      <div className="mt-3">
        <h1 className="px-1 text-2xl font-bold text-black dark:text-white">
          {strings.YOUR_ACTIVE_CYCLE}
        </h1>
        <div className="border-gray-300 my-4 border-t"></div>
        {!loading ? (
          <div>
            {activeRequests?.claim.map((ele: any) => {
              return (
                <div className="mt-2">
                  <ActiveClaimCycleCard
                    participantCode={ele.participantCode}
                    date={ele.date}
                    insurance_id={ele.insurance_id}
                    claimType={ele.claimType}
                    claimID={ele.claimID}
                    status={ele.status}
                    link="/view-claim-request"
                  />
                </div>
              );
            })}
            <div className="mt-2">
              {activeRequests?.coverageEligibility.map((ele: any) => {
                return (
                  <div className="mt-2">
                    <ActiveClaimCycleCard
                      participantCode={ele.participantCode}
                      date={ele.date}
                      insurance_id={ele.insurance_id}
                      claimType={ele.claimType}
                      claimID={ele.claimID}
                      status={ele.status}
                      link="/coverage-eligibility"
                    />
                  </div>
                );
              })}
            </div>
            <div className="mt-2">
              {activeRequests?.preauth.map((ele: any) => {
                return (
                  <div className="mt-2">
                    <ActiveClaimCycleCard
                      participantCode={ele.participantCode}
                      claimID={ele.claimID}
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
        ) : (
          <div className="flex items-center justify-center">
            <svg
              aria-hidden="true"
              className="text-gray-200 dark:text-gray-600 fill-blue-600 mr-2 h-6 w-6 animate-spin"
              viewBox="0 0 100 101"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z"
                fill="currentColor"
              />
              <path
                d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z"
                fill="currentFill"
              />
            </svg>
          </div>
        )}
      </div>
    </div>
  );
};

export default Home;
