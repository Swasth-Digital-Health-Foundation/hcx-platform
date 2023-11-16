import { useLocation, useNavigate } from "react-router-dom";
import Html5QrcodePlugin from "../../components/Html5QrcodeScannerPlugin/Html5QrcodeScannerPlugin";
import { useEffect, useState } from "react";
import ActiveClaimCycleCard from "../../components/ActiveClaimCycleCard";
import strings from "../../utils/strings";
import { generateOutgoingRequest, getCoverageEligibilityRequestList } from "../../services/hcxMockService";
import * as _ from "lodash";
import TransparentLoader from "../../components/TransparentLoader";
import { generateToken, searchParticipant } from "../../services/hcxService";
import CustomButton from "../../components/CustomButton";
import LoadingButton from "../../components/LoadingButton";
import { toast } from "react-toastify";
import { ArrowPathIcon } from "@heroicons/react/24/outline";

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [qrCodeData, setQrCodeData] = useState<any>();
  const [activeRequests, setActiveRequests] = useState<any>([]);
  const [currentIndex, setCurrentIndex] = useState(5);
  const [participantInformation, setParticipantInformation] = useState<any>([]);
  const [loading, setLoading] = useState(false);
  const [mobileNumber, setMobileNumber] = useState<string>("");
  const [initialized, setInitialized] = useState(true);
  const [isValid, setIsValid] = useState(true);
  const [finalData, setFinalData] = useState<any>([]);

  const getEmailFromLocalStorage = localStorage.getItem("email");

  const onNewScanResult = (decodedText: any, decodedResult: any) => {
    setQrCodeData(decodedText);
    setInitialized(false);
  };

  const [displayedData, setDisplayedData] = useState<any>(
    finalData.slice(0, 5)
  );

  if (qrCodeData !== undefined) {
    let obj = JSON.parse(qrCodeData);
    navigate("/add-patient", {
      state: { obj: obj, mobile: location.state },
    });
  }

  const userSearchPayload = {
    entityType: ["Beneficiary"],
    filters: {
      primary_email: { eq: getEmailFromLocalStorage },
    },
  };


  localStorage.setItem(
    "patientMobile",
    mobileNumber
  );
  localStorage.setItem(
    "senderCode",
    participantInformation[0]?.participant_code
  );
  localStorage.setItem(
    "providerName",
    participantInformation[0]?.participant_name
  );

  const getListUsingMobile = { mobile: mobileNumber, app: "OPD" };


  let requestPayload: any = {
    sender_code: participantInformation[0]?.participant_code,
    app: "OPD",
  }

  const search = async () => {
    try {
      const tokenResponse = await generateToken();
      let token = tokenResponse.data?.access_token;
      const response = await searchParticipant(userSearchPayload, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      let userRes = response.data?.participants;
      setParticipantInformation(userRes);

      let requestPayload = {
        sender_code: response.data?.participants[0]?.participant_code,
        app: "OPD",
      };

      if (response.status === 200) {
        getCoverageEligibilityRequestList(setLoading, requestPayload, setActiveRequests, setFinalData, setDisplayedData);
      }
    } catch (error) {
      // console.log(error);
      getCoverageEligibilityRequestList(setLoading, requestPayload, setActiveRequests, setFinalData, setDisplayedData);
    }
  };


  const loadMoreData = () => {
    const nextData = finalData.slice(currentIndex, currentIndex + 5);
    setDisplayedData([...displayedData, ...nextData]);
    setCurrentIndex(currentIndex + 5);
  };

  const latestStatusByEntry: Record<string, string | undefined> = {};

  activeRequests.forEach((entry: Record<string, any>) => {
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

  const handleMobileNumberChange = (e: any) => {
    const inputValue = e.target.value;
    // Check if the input contains exactly 10 numeric characters
    const isValidInput = /^\d{10}$/.test(inputValue);
    setIsValid(isValidInput);
    setMobileNumber(inputValue);
  };

  useEffect(() => {
    search();
  }, [])

  return (
    <div>
      <div className="flex justify-between">
        <div className="">
          <h1 className="text-1xl mb-3 font-bold text-black dark:text-white">
            {strings.WELCOME_TEXT} {participantInformation[0]?.participant_name}
          </h1>
        </div>
      </div>
      <div className="rounded-lg border border-stroke bg-white p-2 shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="mt-2">
          <div className="qr-code p-1">
            <div id="reader" className="px-1 ">
              <Html5QrcodePlugin
                fps={60}
                qrbox={250}
                disableFlip={false}
                qrCodeSuccessCallback={onNewScanResult}
                setInitialized={initialized}
              />
            </div>
          </div>
          <p className="mt-3 text-center font-bold text-black dark:text-gray">
            OR
          </p>
          <div className="mt-3 text-center">
            <a
              className="cursor-pointer underline"
              onClick={() => {
                navigate("/add-patient");
              }}
            >
              {strings.ADD_NEW_PATIENT}
            </a>
          </div>
        </div>
      </div>
      <div>
        <div className="mt-5 flex items-center justify-between">
          <label className="block text-left text-2xl font-bold text-black dark:text-white">
            {strings.SEARCH_PATIENT}
          </label>
          <ArrowPathIcon
            onClick={() => {
              getCoverageEligibilityRequestList(setLoading, requestPayload, setActiveRequests, setFinalData, setDisplayedData);
            }}
            className={
              loading ? "animate-spin h-7 w-7" : "h-7 w-7"
            }
            aria-hidden="true"
          />
        </div>
        <label className="mb-2.5 mt-2 block text-left font-medium text-black dark:text-white">
          {strings.PATIENT_MOBILE}
        </label>
        <div className="relative">
          <input
            onChange={handleMobileNumberChange}
            type="text"
            placeholder="Enter mobile no."
            className={`border ${isValid ? "border-stroke" : "border-red"
              } w-full rounded-lg py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
          />
        </div>
        <div>
          {loading ? (
            <LoadingButton className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed" />
          ) : (
            <CustomButton
              text="Search"
              onClick={() => {
                if (mobileNumber === "") {
                  toast.info("please enter mobile number");
                } else {
                  getCoverageEligibilityRequestList(setLoading, getListUsingMobile, setActiveRequests, setFinalData, setDisplayedData);
                }
              }}
              disabled={!isValid}
            />
          )}
        </div>
      </div>
      <div className="mt-3">
        {loading ? (
          <div className="mt-3 flex items-center gap-4">
            <h1 className="px-1 text-2xl font-bold text-black dark:text-white">
              Getting active requests
            </h1>
            <TransparentLoader />
          </div>
        ) : displayedData.length === 0 ? (
          <div className="mt-3 flex justify-between">
            <h1 className="px-1 text-2xl font-bold text-black dark:text-white">
              No active patients
            </h1>
            <button
              disabled={loading}
              onClick={(event: any) => {
                event.preventDefault();
                getCoverageEligibilityRequestList(setLoading, requestPayload, setActiveRequests, setFinalData, setDisplayedData);
              }}
              className="align-center flex w-20 justify-center rounded py-1 font-medium text-black underline disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
            >
              Refresh
            </button>
          </div>
        ) : (
          <h1 className="mt-5 px-1 text-2xl font-bold text-black dark:text-white">
            {strings.ACTIVE_LIST} ({activeRequests.length})
          </h1>
        )}
        {!loading ? (
          <div>
            {_.map(displayedData, (ele: any, index: number) => {
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
                    patientMobileNumber={ele.mobile || mobileNumber}
                    patientName={ele.patientName}
                  />
                </div>
              );
            })}
            <div className="mt-2 flex justify-end underline">
              {currentIndex < activeRequests.length && (
                <button onClick={loadMoreData}>{strings.VIEW_MORE}</button>
              )}
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default Home;
