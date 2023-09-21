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
  const onNewScanResult = (decodedText: any, decodedResult: any) => {
    console.log(decodedResult);
    setQrCodeData(decodedText);
  };
  if (qrCodeData !== undefined) {
    let obj = JSON.parse(qrCodeData);
    navigate('/coverage-eligibility-request', {
      state: { obj: obj, filters: location.state },
    });
  }

  const mobile = _.get(location, 'state.filters.mobile.eq', undefined);

  const requestPayload = {
    mobile: mobile,
  };

  const filter = {
    entityType: ['Beneficiary'],
    filters: {
      mobile: { eq: requestPayload?.mobile },
    },
  };

  useEffect(() => {
    const getActivePlans = async () => {
      try {
        let response = await generateOutgoingRequest(
          'claim/list',
          requestPayload
        );
        setActiveRequests(response.data);
      } catch (err) {
        console.log(err);
      }
    };
    const search = async () => {
      try {
        const searchUser = await postRequest('/search', filter);
        console.log(searchUser);
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
        {activeRequests?.claim.map((ele: any) => {
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
          {activeRequests?.coverageEligibility.map((ele: any) => {
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
          {activeRequests?.preauth.map((ele: any) => {
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
