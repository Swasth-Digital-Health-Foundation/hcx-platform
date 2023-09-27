import { useLocation, useNavigate } from 'react-router-dom';
import Html5QrcodePlugin from '../../components/Html5QrcodeScannerPlugin/Html5QrcodeScannerPlugin';
import { useEffect, useState } from 'react';
import ActiveClaimCycleCard from '../../components/ActiveClaimCycleCard';
import strings from '../../utils/strings';
import { generateOutgoingRequest } from '../../services/hcxMockService';
import { postRequest } from '../../services/registryService';
import * as _ from 'lodash';
import TransparentLoader from '../../components/TransparentLoader';

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [qrCodeData, setQrCodeData] = useState<any>();
  const [activeRequests, setActiveRequests] = useState<any>([]);
  const [currentIndex, setCurrentIndex] = useState(5);
  const [userInformation, setUserInformation] = useState<any>([]);
  const [loading, setLoading] = useState(false);

  const getMobileFromLocalStorage = localStorage.getItem('mobile');

  const onNewScanResult = (decodedText: any, decodedResult: any) => {
    setQrCodeData(decodedText);
  };

  const [finalData, setFinalData] = useState<any>([]);
  const [displayedData, setDisplayedData] = useState<any>(
    finalData.slice(0, 5)
  );

  if (qrCodeData !== undefined) {
    let obj = JSON.parse(qrCodeData);
    navigate('/coverage-eligibility-request', {
      state: { obj: obj, mobile: location.state },
    });
  }

  const requestPayload = {
    mobile: getMobileFromLocalStorage,
  };

  const filter = {
    entityType: ['Beneficiary'],
    filters: {
      mobile: { eq: getMobileFromLocalStorage },
    },
  };

  const search = async () => {
    try {
      const searchUser = await postRequest('/search', filter);
      setUserInformation(searchUser.data);
    } catch (error) {
      console.log(error);
    }
  };

  const getCoverageEligibilityRequestList = async () => {
    try {
      setLoading(true);
      let response = await generateOutgoingRequest(
        'bsp/request/list',
        requestPayload
      );
      const data = response.data.entries;
      setActiveRequests(data);

      const coverageArray = [];
      const claimArray = [];

      for (const entry of data) {
        // Iterate through each entry in the input data.
        const key = Object.keys(entry)[0];
        const objects = entry[key];

        if (objects.length === 1 && objects[0].type === 'claim') {
          // If there's only one object and its type is "claim," add it to claimArray.
          claimArray.push(objects[0]);
        } else {
          // If there's more than one object or any object with type "coverageeligibility," add them to coverageArray.
          coverageArray.push(
            ...objects.filter((obj: any) => obj.type === 'coverageeligibility')
          );
        }
      }
      // Create a new array containing both claimArray and coverageArray.
      const newArray = [...claimArray, ...coverageArray];
      const sortedData = newArray.slice().sort((a: any, b: any) => {
        return b.date - a.date;
      });

      setFinalData(sortedData);
      setDisplayedData(sortedData.slice(0, 5));
      setLoading(false);
    } catch (err) {
      setLoading(false);
    }
  };

  useEffect(() => {
    getCoverageEligibilityRequestList();
    search();
  }, []);

  const loadMoreData = () => {
    const nextData = finalData.slice(currentIndex, currentIndex + 5);
    setDisplayedData([...displayedData, ...nextData]);
    setCurrentIndex(currentIndex + 5);
  };

  return (
    <div>
      <div className="flex justify-between">
        <div className="">
          <h1 className="text-1xl font-bold text-black dark:text-white">
            {strings.WELCOME_TEXT} {userInformation[0]?.name || '...'}
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
                navigate('/new-claim', { state: location.state });
              }}
            >
              {strings.SUBMIT_NEW_CLAIM}
            </a>
          </div>
        </div>
      </div>
      <div className="mt-3">
        {loading ? (
          <div className="flex items-center gap-4">
            <h1 className="px-1 text-2xl font-bold text-black dark:text-white">
              Getting active requests
            </h1>
            <TransparentLoader />
          </div>
        ) : displayedData.length === 0 ? (
          <h1 className="px-1 text-2xl font-bold text-black dark:text-white">
            No active requests
          </h1>
        ) : (
          <div className="flex justify-between">
            <h1 className="px-1 text-2xl font-bold text-black dark:text-white">
              {strings.YOUR_ACTIVE_CYCLE} ({activeRequests.length})
            </h1>
            <button
              disabled={loading}
              onClick={(event: any) => {
                event.preventDefault();
                getCoverageEligibilityRequestList();
              }}
              className="align-center flex w-20 justify-center rounded py-1 font-medium text-black underline disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
            >
              Refresh
            </button>
          </div>
        )}
        <div className="border-gray-300 my-4 border-t"></div>
        {!loading ? (
          <div>
            {displayedData?.map((ele: any, index: any) => {
              return (
                <div className="mt-2" key={index}>
                  <ActiveClaimCycleCard
                    participantCode={ele.sender_code}
                    payorCode={ele.recipient_code}
                    date={ele.date}
                    insurance_id={ele.insurance_id}
                    claimType={ele.claimType}
                    apiCallId={ele.apiCallId}
                    status={ele.status}
                    type={ele.type}
                    mobile={location.state}
                    billAmount={ele.billAmount}
                    workflowId={ele.workflow_id}
                  />
                </div>
              );
            })}
            <div className="mt-2 flex justify-end underline">
              {currentIndex < activeRequests.length && (
                <button onClick={loadMoreData}>View More</button>
              )}
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default Home;
