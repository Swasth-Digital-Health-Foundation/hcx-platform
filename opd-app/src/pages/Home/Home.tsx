import { useLocation, useNavigate } from "react-router-dom";
import Html5QrcodePlugin from "../../components/Html5QrcodeScannerPlugin/Html5QrcodeScannerPlugin";
import { useEffect, useState } from "react";
import ActiveClaimCycleCard from "../../components/ActiveClaimCycleCard";
import strings from "../../utils/strings";
import { generateOutgoingRequest } from "../../services/hcxMockService";
import * as _ from "lodash";
import TransparentLoader from "../../components/TransparentLoader";
import { searchParticipant } from "../../services/hcxService";
import CustomButton from "../../components/CustomButton";
import LoadingButton from "../../components/LoadingButton";

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [qrCodeData, setQrCodeData] = useState<any>();
  const [activeRequests, setActiveRequests] = useState<any>([]);
  const [currentIndex, setCurrentIndex] = useState(5);
  const [participantInformation, setParticipantInformation] = useState<any>([]);
  const [loading, setLoading] = useState(false);
  const [mobileNumber, setMobileNumber] = useState<string>("");

  const getEmailFromLocalStorage = localStorage.getItem("email");

  const onNewScanResult = (decodedText: any, decodedResult: any) => {
    // console.log(decodedResult);
    setQrCodeData(decodedText);
  };

  const [finalData, setFinalData] = useState<any>([]);
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

  const token = localStorage.getItem("token");

  const config = {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };

  const search = async () => {
    try {
      const response = await searchParticipant(userSearchPayload, config);
      let userRes = response.data.participants;
      setParticipantInformation(userRes);
    } catch (error) {
      console.log(error);
    }
  };

  const requestPayload =
    mobileNumber === ""
      ? { sender_code: participantInformation[0]?.participant_code }
      : { mobile: mobileNumber };

  localStorage.setItem(
    "senderCode",
    participantInformation[0]?.participant_code
  );
  localStorage.setItem(
    "providerName",
    participantInformation[0]?.participant_name
  );

  const getCoverageEligibilityRequestList = async () => {
    try {
      setLoading(true);
      let response = await generateOutgoingRequest(
        "bsp/request/list",
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

        if (objects.length === 1 && objects[0].type === "claim") {
          // If there's only one object and its type is "claim," add it to claimArray.
          claimArray.push(objects[0]);
        } else {
          // If there's more than one object or any object with type "coverageeligibility," add them to coverageArray.
          coverageArray.push(
            ...objects.filter((obj: any) => obj.type === "coverageeligibility")
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
    search();
    getCoverageEligibilityRequestList();
  }, [requestPayload.sender_code]);

  const loadMoreData = () => {
    const nextData = finalData.slice(currentIndex, currentIndex + 5);
    setDisplayedData([...displayedData, ...nextData]);
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
  // console.log("mobile active ", displayedData);
  // console.log("mobile",location.state)
  return (
    <div>
      <div className="flex justify-between">
        <div className="">
          <h1 className="text-1xl font-bold text-black dark:text-white">
            {strings.WELCOME_TEXT} {participantInformation[0]?.participant_name}
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
                navigate("/add-patient");
              }}
            >
              + Add new patient
            </a>
          </div>
        </div>
      </div>
      <div>
        <label className="mb-2.5 mt-2 block text-left text-2xl font-bold text-black dark:text-white">
          Search patient :
        </label>
        <label className="mb-2.5 mt-2 block text-left font-medium text-black dark:text-white">
          Patient mobile no. :
        </label>
        <div className="relative">
          <input
            onChange={(e: any) => {
              setMobileNumber(e.target.value);
            }}
            type="text"
            placeholder="Enter mobile no."
            className={`border ${
              true ? "border-stroke" : "border-red"
            } bg w-full rounded-lg py-4 pl-6 pr-10 outline-none focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary`}
          />
        </div>
        <div>
          {loading ? (
            <LoadingButton className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed" />
          ) : (
            <CustomButton
              text="Search"
              onClick={() => getCoverageEligibilityRequestList()}
              disabled={false}
            />
          )}
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
          <div className="flex justify-between">
            <h1 className="px-1 text-2xl font-bold text-black dark:text-white">
              No active patients
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
        ) : (
          <h1 className="px-1 text-2xl font-bold text-black dark:text-white">
            Recent active patients : ({activeRequests.length})
          </h1>
        )}
        <div className="border-gray-300 my-4 border-t"></div>
        {!loading ? (
          <div>
            {displayedData?.map((ele: any, index: any) => {
              console.log(ele)
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
                    // mobile={ele.mobile}
                    patientMobileNumber={ele.mobile || mobileNumber}
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
