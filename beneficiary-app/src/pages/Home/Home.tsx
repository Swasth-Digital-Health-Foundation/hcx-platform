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
              navigate('/request-success', {
                state: { text: 'coverage eligibility' },
              });
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

  // const data = {
  //   entries: [
  //     {
  //       '6bd79fe8-d240-4908-969c-30bdfd272290': [
  //         {
  //           date: '1695878808516',
  //           insurance_id: '1234567999',
  //           claimType: 'OPD',
  //           billAmount: '12345',
  //           workflow_id: '6bd79fe8-d240-4908-969c-30bdfd272290',
  //           correlationId: 'baceb4ca-9fcd-41d9-86e7-d206304edf1f',
  //           recipient_code: 'wemeanhospital+mock_payor.yopmail@swasth-hcx-dev',
  //           type: 'claim',
  //           apiCallId: 'b2df9fd1-e9ef-4f99-9797-d324a1c07099',
  //           sender_code: 'testprovider1.apollo@swasth-hcx-dev',
  //           supportingDocuments: [
  //             'https://dev-hcx.s3.ap-south-1.amazonaws.com/beneficiary-app/%2FC4GT_-_Proposal_Status_5_June_%20%285%29%20%282%29.pdf',
  //           ],
  //           status: 'Approved',
  //         },
  //       ],
  //     },
  //     {
  //       '69396c96-9fd2-44dd-a245-3d0ce2c910f7': [
  //         {
  //           date: '1695891596062',
  //           insurance_id: '1234567999',
  //           claimType: 'OPD',
  //           workflow_id: '69396c96-9fd2-44dd-a245-3d0ce2c910f7',
  //           correlationId: '7dec24dc-c0ed-4670-a9bd-dde1e954142d',
  //           recipient_code: 'wemeanhospital+mock_payor.yopmail@swasth-hcx-dev',
  //           type: 'coverageeligibility',
  //           apiCallId: '66adb7f0-a246-4dd0-ba93-684821d7fb19',
  //           sender_code: 'testprovider1.apollo@swasth-hcx-dev',
  //           status: 'Approved',
  //         },
  //       ],
  //     },
  //     {
  //       'ead18fe8-00ce-457c-8b1a-85f614564cdd': [
  //         {
  //           date: '1695893354174',
  //           insurance_id: '1234567999',
  //           claimType: 'OPD',
  //           workflow_id: 'ead18fe8-00ce-457c-8b1a-85f614564cdd',
  //           correlationId: 'e9acc578-d7af-4e0f-8d56-e354317406b1',
  //           recipient_code: 'wemeanhospital+mock_payor.yopmail@swasth-hcx-dev',
  //           type: 'coverageeligibility',
  //           apiCallId: '7cd410b8-bd3b-4985-b63e-9d9ef99042dd',
  //           sender_code: 'testprovider1.apollo@swasth-hcx-dev',
  //           status: 'Approved',
  //         },
  //       ],
  //     },
  //     {
  //       '8c44175f-c5b7-4cd6-8350-8cfc6bdf9ad0': [
  //         {
  //           date: '1695798314112',
  //           insurance_id: '1234567999',
  //           claimType: 'OPD',
  //           billAmount: '99997',
  //           workflow_id: '8c44175f-c5b7-4cd6-8350-8cfc6bdf9ad0',
  //           correlationId: 'eac33f6f-1378-4dad-a030-895b05b43be1',
  //           recipient_code: 'wemeanhospital+mock_payor.yopmail@swasth-hcx-dev',
  //           type: 'claim',
  //           apiCallId: '914318f0-73a8-4e7c-aed5-d142ba279d44',
  //           sender_code: 'testprovider1.apollo@swasth-hcx-dev',
  //           supportingDocuments: [],
  //           status: 'Approved',
  //         },
  //         {
  //           date: '1695798210571',
  //           insurance_id: '1234567999',
  //           claimType: 'OPD',
  //           workflow_id: '8c44175f-c5b7-4cd6-8350-8cfc6bdf9ad0',
  //           correlationId: 'c5adf814-1169-4c0f-b39e-14d0504a8103',
  //           recipient_code: 'wemeanhospital+mock_payor.yopmail@swasth-hcx-dev',
  //           type: 'coverageeligibility',
  //           apiCallId: '7ec9203b-9a88-49b8-b6ce-5332c644bb23',
  //           sender_code: 'testprovider1.apollo@swasth-hcx-dev',
  //           status: 'Approved',
  //         },
  //       ],
  //     },
  //     {
  //       'c2114404-f6f6-4fd2-90de-10ebfc79062e': [
  //         {
  //           date: '1695817323601',
  //           insurance_id: '1234567999',
  //           claimType: 'OPD',
  //           billAmount: '2497',
  //           workflow_id: 'c2114404-f6f6-4fd2-90de-10ebfc79062e',
  //           correlationId: '38de0ffa-e919-42eb-8afd-5806d3fc0a0f',
  //           recipient_code: 'wemeanhospital+mock_payor.yopmail@swasth-hcx-dev',
  //           type: 'claim',
  //           apiCallId: '7ebe5c36-e69a-4868-a2fe-0aa46e106311',
  //           sender_code: 'testprovider1.apollo@swasth-hcx-dev',
  //           supportingDocuments: [],
  //           status: 'Pending',
  //         },
  //         {
  //           date: '1695816502701',
  //           insurance_id: '1234567999',
  //           claimType: 'OPD',
  //           workflow_id: 'c2114404-f6f6-4fd2-90de-10ebfc79062e',
  //           correlationId: 'fa837d41-b781-4a1d-8e22-cf313d7d693a',
  //           recipient_code: 'wemeanhospital+mock_payor.yopmail@swasth-hcx-dev',
  //           type: 'preauth',
  //           apiCallId: '8389a7d3-bc8d-4f38-910d-00c3991b23d3',
  //           sender_code: 'testprovider1.apollo@swasth-hcx-dev',
  //           status: 'Approved',
  //         },
  //         {
  //           date: '1695816248108',
  //           insurance_id: '1234567999',
  //           claimType: 'OPD',
  //           workflow_id: 'c2114404-f6f6-4fd2-90de-10ebfc79062e',
  //           correlationId: 'd369f9d2-88f0-469c-84c4-b39d35a2bc82',
  //           recipient_code: 'wemeanhospital+mock_payor.yopmail@swasth-hcx-dev',
  //           type: 'coverageeligibility',
  //           apiCallId: 'b6a2d7df-83c4-432b-a064-32449b6860c2',
  //           sender_code: 'testprovider1.apollo@swasth-hcx-dev',
  //           status: 'Approved',
  //         },
  //       ],
  //     },
  //     {
  //       '770e6510-8327-4901-a7da-e8c3b881efc4': [
  //         {
  //           date: '1695817435293',
  //           insurance_id: '1234567999',
  //           claimType: 'OPD',
  //           billAmount: '12344',
  //           workflow_id: '770e6510-8327-4901-a7da-e8c3b881efc4',
  //           correlationId: 'd31a795b-0484-448e-8d45-7abd34adcb91',
  //           recipient_code: 'wemeanhospital+mock_payor.yopmail@swasth-hcx-dev',
  //           type: 'claim',
  //           apiCallId: 'baf8a95b-55df-4299-927a-35ba69545d51',
  //           sender_code: 'testprovider1.apollo@swasth-hcx-dev',
  //           supportingDocuments: [
  //             'https://dev-hcx.s3.ap-south-1.amazonaws.com/beneficiary-app/%2FC4GT_-_Proposal_Status_5_June_%20%285%29%20%281%29.pdf',
  //           ],
  //           status: 'Approved',
  //         },
  //       ],
  //     },
  //   ],
  // };

  // const coverageArray = [];
  // const claimArray = [];

  // for (const entry of activeRequests) {
  //   // Iterate through each entry in the input data.
  //   const key = Object.keys(entry)[0];
  //   const objects = entry[key];

  //   if (objects.length === 1 && objects[0].type === 'claim') {
  //     // If there's only one object and its type is "claim," add it to claimArray.
  //     claimArray.push(objects[0]);
  //   } else {
  //     // If there's more than one object or any object with type "coverageeligibility," add them to coverageArray.
  //     coverageArray.push(
  //       ...objects.filter((obj: any) => obj.type === 'coverageeligibility')
  //     );
  //   }
  // }

  // console.log(coverageArray);
  // console.log(claimArray);

  const latestStatusByEntry: Record<string, string | undefined> = {};

  activeRequests.forEach((entry:any) => {
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

  // console.log(latestStatusByEntry);
  console.log(coverageAndClaimData)

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
