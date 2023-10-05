import { useLocation, useNavigate } from 'react-router-dom';
import Html5QrcodePlugin from '../../components/Html5QrcodeScannerPlugin/Html5QrcodeScannerPlugin';
import { useEffect, useState } from 'react';
import ActiveClaimCycleCard from '../../components/ActiveClaimCycleCard';
import strings from '../../utils/strings';
import { generateOutgoingRequest } from '../../services/hcxMockService';
import { postRequest } from '../../services/registryService';
import * as _ from 'lodash';
import TransparentLoader from '../../components/TransparentLoader';
import { toast } from 'react-toastify';

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [qrCodeData, setQrCodeData] = useState<any>();
  const [currentIndex, setCurrentIndex] = useState(5);
  const [userInformation, setUserInformation] = useState<any>([]);
  const [loading, setLoading] = useState(false);

  const getMobileFromLocalStorage = localStorage.getItem('mobile');

  const onNewScanResult = (decodedText: any, decodedResult: any) => {
    setQrCodeData(decodedText);
  };

  const [activeRequests, setActiveRequests] = useState<any>([]);
  const [finalData, setFinalData] = useState<any>([]);
  const [coverageAndClaimData, setDisplayedData] = useState<any>(
    finalData.slice(0, 5)
  );

  useEffect(() => {
    if (qrCodeData !== undefined) {
      let obj = JSON.parse(qrCodeData);
      let payload = {
        providerName: obj?.provider_name,
        participantCode: obj?.participant_code,
        serviceType: 'OPD',
        mobile: localStorage.getItem('mobile'),
        payor: userInformation[0]?.payor_details[0]?.payor,
        insuranceId: userInformation[0]?.payor_details[0]?.insurance_id,
        patientName: userInformation[0]?.name,
      };

      const sendCoverageEligibilityRequest = async () => {
        try {
          // setLoading(true);
          let response = await generateOutgoingRequest(
            'create/coverageeligibility/check',
            payload
          );
          if (response?.status === 202) {
            setLoading(true);
            setTimeout(() => {
              setLoading(true);
              getCoverageEligibilityRequestList();
              navigate('/coverage-eligibility-success-page');
            }, 3000);
          }
        } catch (error) {
          // setLoading(false);
          toast.error(_.get(error, 'response.data.error.message'));
        }
      };
      sendCoverageEligibilityRequest();
      setLoading(false);
    }
  }, [qrCodeData]);

  // const getPatientName = async () => {
  //   try {
  //     let response = await postRequest('/search', filter);
  //     setPatientName(response.data[0]?.name);
  //     setUserInformation(response.data);
  //   } catch (error) {
  //     toast.error(_.get(error, 'response.data.error.message'));
  //   }
  // };

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
      // Create a new array containing both claim and coverage requests.
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
    search();
    getCoverageEligibilityRequestList();
  }, []);

  const loadMoreData = () => {
    const nextData = finalData.slice(currentIndex, currentIndex + 5);
    setDisplayedData([...coverageAndClaimData, ...nextData]);
    setCurrentIndex(currentIndex + 5);
  };

  const latestStatusByEntry: Record<string, string | undefined> = {};

  activeRequests.forEach((entry: any) => {
    for (const [key, items] of Object.entries(entry)) {
      // Find the item with the latest date
      const latestItem = items.reduce((latest: any, item: any) => {
        const itemDate = parseInt(item.date, 10);
        if (!latest || itemDate > parseInt(latest.date, 10)) {
          return item;
        }
        return latest;
      }, null);

      // Extract the status of the latest item
      if (latestItem) {
        latestStatusByEntry[key] = latestItem.status;
      }
    }
  });

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
        ) : coverageAndClaimData.length === 0 ? (
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
            {coverageAndClaimData?.map((ele: any, index: any) => {
              // console.log(ele)
              return (
                <div className="mt-2" key={index}>
                  <ActiveClaimCycleCard
                    participantCode={ele.sender_code}
                    payorCode={ele.recipient_code}
                    date={ele.date}
                    insurance_id={ele.insurance_id}
                    claimType={ele.claimType}
                    apiCallId={ele.apiCallId}
                    status={latestStatusByEntry[ele.workflow_id]}
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
