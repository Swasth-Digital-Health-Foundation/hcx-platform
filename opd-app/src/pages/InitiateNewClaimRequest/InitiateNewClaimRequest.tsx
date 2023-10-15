import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { handleFileChange } from "../../utils/attachmentSizeValidation";
import { generateOutgoingRequest } from "../../services/hcxMockService";
import LoadingButton from "../../components/LoadingButton";
import { toast } from "react-toastify";
import strings from "../../utils/strings";
import { generateToken, searchParticipant } from "../../services/hcxService";
import axios from "axios";
import { postRequest } from "../../services/registryService";
import SelectInput from "../../components/SelectInput";
import TextInputWithLabel from "../../components/inputField";
import Loader from "../../common/Loader";
import TransparentLoader from "../../components/TransparentLoader";

const InitiateNewClaimRequest = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [selectedFile, setSelectedFile]: any = useState<FileList | undefined>(
    undefined
  );
  const [fileErrorMessage, setFileErrorMessage]: any = useState();
  const [isSuccess, setIsSuccess]: any = useState(false);

  const [amount, setAmount] = useState<string>("");
  const [serviceType, setServiceType] = useState<string>();
  const [documentType, setDocumentType] = useState<string>("prescription");

  const [loading, setLoading] = useState(false);
  const [payorName, setPayorName] = useState<string>("");

  const [fileUrlList, setUrlList] = useState<any>([]);

  const [userInfo, setUserInformation] = useState<any>([]);
  const [activeRequests, setActiveRequests] = useState<any>([]);
  const [finalData, setFinalData] = useState<any>([]);
  const [displayedData, setDisplayedData] = useState<any>(
    finalData.slice(0, 5)
  );

  const [selectedInsurance, setSelectedInsurance] = useState<string>("");
  const [submitLoading, setSubmitLoading] = useState(false);

  const insuranceOptions = [
    { label: "Select", value: "" },
    {
      label: displayedData[0]?.insurance_id,
      value: displayedData[0]?.insurance_id,
    },
  ];

  const serviceTypeOptions = [
    { label: "Select", value: "" },
    {
      label: displayedData[0]?.claimType,
      value: displayedData[0]?.claimType,
    },
  ];

  const documentTypeOptions = [
    {
      label: "Prescription",
      value: "Prescription",
    },
    {
      label: "Payment Receipt",
      value: "Payment Receipt",
    },
    {
      label: "Medical Bill/invoice",
      value: "Medical Bill/invoice",
    },
  ];

  const treatmentOptions = [{ label: "Consultation", value: "Consultation" }];

  let FileLists: any;
  if (selectedFile !== undefined) {
    FileLists = Array.from(selectedFile);
  }

  const data = location.state;
  const handleDelete = (name: any) => {
    if (selectedFile !== undefined) {
      const updatedFilesList = selectedFile.filter(
        (file: any) => file.name !== name
      );
      setSelectedFile(updatedFilesList);
    }
  };

  let initiateClaimRequestBody: any = {
    insuranceId: data?.insuranceId || displayedData[0]?.insurance_id,
    insurancePlan: data?.insurancePlan || null,
    mobile:
      localStorage.getItem("mobile") || localStorage.getItem("patientMobile"),
    patientName: userInfo[0]?.name || localStorage.getItem("patientName"),
    participantCode:
      data?.participantCode || localStorage.getItem("senderCode"),
    payor: data?.payor || payorName,
    providerName: data?.providerName || localStorage.getItem("providerName"),
    serviceType: data?.serviceType || displayedData[0]?.claimType,
    billAmount: amount,
    workflowId: data?.workflowId,
    supportingDocuments: [
      {
        documentType: documentType,
        urls: fileUrlList.map((ele: any) => {
          return ele.url;
        }),
      },
    ],
    type: "provider_app",
  };

  const filter = {
    entityType: ["Beneficiary"],
    filters: {
      mobile: { eq: localStorage.getItem("mobile") },
    },
  };

  useEffect(() => {
    const search = async () => {
      try {
        const searchUser = await postRequest("/search", filter);
        setUserInformation(searchUser.data);
      } catch (error) {
        console.log(error);
      }
    };
    search();
  }, []);

  const payorCodePayload = {
    filters: {
      participant_code: {
        eq: displayedData[0]?.recipient_code || location.state?.payorCode,
      },
    },
  };

  useEffect(() => {
    getCoverageEligibilityRequestList();
  }, []);

  useEffect(() => {
    const search = async () => {
      try {
        const tokenResponse = await generateToken();
        let token = tokenResponse.data?.access_token;
        const payorResponse = await searchParticipant(payorCodePayload, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        let payorname = payorResponse.data?.participants[0]?.participant_name;
        setPayorName(payorname);
      } catch (err) {
        console.log("error", err);
      }
    };
    search();
  }, [displayedData]);

  const handleUpload = async () => {
    try {
      setSubmitLoading(true);
      const formData = new FormData();
      formData.append("mobile", location.state?.patientMobile);

      FileLists.forEach((file: any) => {
        formData.append(`file`, file);
      });
      toast.info("Uploading documents please wait...!");
      const response = await axios({
        url: `${process.env.hcx_mock_service}/upload/documents`,
        method: "POST",
        data: formData,
      });
      let obtainedResponse = response.data;
      const uploadedUrls = obtainedResponse.map((ele: any) => ele.url);
      // Update the payload with the new URLs
      initiateClaimRequestBody.supportingDocuments[0].urls = uploadedUrls;
      setUrlList((prevFileUrlList: any) => [
        ...prevFileUrlList,
        ...obtainedResponse,
      ]);
      toast.info("Documents uploaded successfully!");
    } catch (error) {
      setSubmitLoading(false);
      console.error("Error in uploading file", error);
    }
  };

  const submitClaim = async () => {
    try {
      setSubmitLoading(true);
      handleUpload();
      setTimeout(async () => {
        let getUrl = await generateOutgoingRequest(
          "create/claim/submit",
          initiateClaimRequestBody
        );
        setSubmitLoading(false);
        navigate("/request-success", {
          state: {
            text: "claim",
            mobileNumber: data.mobile || initiateClaimRequestBody.mobile,
          },
        });
      }, 2000);
    } catch (err) {
      setSubmitLoading(false);
      toast.error("Faild to submit claim, try again!");
    }
  };

  const requestPayload = {
    sender_code: localStorage.getItem("senderCode"),
  };

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

  return (
    <>
      {loading ? (
        <TransparentLoader />
      ) : (
        <div className="w-full">
          <h2 className="mb-4 text-2xl font-bold text-black dark:text-white sm:text-title-xl2">
            {strings.NEW_CLAIM_REQUEST}
          </h2>
          <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
            <SelectInput
              label="Selected insurance :"
              value={selectedInsurance || displayedData[0]?.insurance_id}
              onChange={(e: any) => setSelectedInsurance(e.target.value)}
              options={insuranceOptions}
              disabled={false}
            />
            <SelectInput
              label="Service type :"
              value={displayedData[0]?.claimType || serviceType}
              // disabled={true}
              onChange={(e: any) => setServiceType(e.target.value)}
              options={serviceTypeOptions}
            />
            <SelectInput
              label="Service/Treatment given :"
              value={"consultation"}
              onChange={(e: any) => setAmount(e.target.value)}
              // disabled={true}
              options={treatmentOptions}
            />
            <TextInputWithLabel
              label="Bill amount :"
              value={amount}
              onChange={(e: any) => setAmount(e.target.value)}
              placeholder="Enter amount"
              disabled={false}
              type="number"
            />
          </div>
          <div className="mt-4 rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
            <h2 className="text-1xl mb-4 font-bold text-black dark:text-white sm:text-title-xl2">
              {strings.SUPPORTING_DOCS}
            </h2>
            <div className="relative z-20 mb-4 bg-white dark:bg-form-input">
              <SelectInput
                label="Document type :"
                // value={treatmentType}
                onChange={(e: any) => setDocumentType(e.target.value)}
                disabled={false}
                options={documentTypeOptions}
              />
            </div>
            <div className="flex items-center justify-evenly gap-x-6">
              <div>
                <label
                  htmlFor="profile"
                  className="bottom-0 right-0 flex h-15 w-15 cursor-pointer items-center justify-center rounded-full bg-primary text-white hover:bg-opacity-90 sm:bottom-2 sm:right-2"
                >
                  <svg
                    className="fill-current"
                    width="20"
                    height="20"
                    viewBox="0 0 14 14"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      fillRule="evenodd"
                      clipRule="evenodd"
                      d="M4.76464 1.42638C4.87283 1.2641 5.05496 1.16663 5.25 1.16663H8.75C8.94504 1.16663 9.12717 1.2641 9.23536 1.42638L10.2289 2.91663H12.25C12.7141 2.91663 13.1592 3.101 13.4874 3.42919C13.8156 3.75738 14 4.2025 14 4.66663V11.0833C14 11.5474 13.8156 11.9925 13.4874 12.3207C13.1592 12.6489 12.7141 12.8333 12.25 12.8333H1.75C1.28587 12.8333 0.840752 12.6489 0.512563 12.3207C0.184375 11.9925 0 11.5474 0 11.0833V4.66663C0 4.2025 0.184374 3.75738 0.512563 3.42919C0.840752 3.101 1.28587 2.91663 1.75 2.91663H3.77114L4.76464 1.42638ZM5.56219 2.33329L4.5687 3.82353C4.46051 3.98582 4.27837 4.08329 4.08333 4.08329H1.75C1.59529 4.08329 1.44692 4.14475 1.33752 4.25415C1.22812 4.36354 1.16667 4.51192 1.16667 4.66663V11.0833C1.16667 11.238 1.22812 11.3864 1.33752 11.4958C1.44692 11.6052 1.59529 11.6666 1.75 11.6666H12.25C12.4047 11.6666 12.5531 11.6052 12.6625 11.4958C12.7719 11.3864 12.8333 11.238 12.8333 11.0833V4.66663C12.8333 4.51192 12.7719 4.36354 12.6625 4.25415C12.5531 4.14475 12.4047 4.08329 12.25 4.08329H9.91667C9.72163 4.08329 9.53949 3.98582 9.4313 3.82353L8.43781 2.33329H5.56219Z"
                      fill=""
                    />
                    <path
                      fillRule="evenodd"
                      clipRule="evenodd"
                      d="M7.00004 5.83329C6.03354 5.83329 5.25004 6.61679 5.25004 7.58329C5.25004 8.54979 6.03354 9.33329 7.00004 9.33329C7.96654 9.33329 8.75004 8.54979 8.75004 7.58329C8.75004 6.61679 7.96654 5.83329 7.00004 5.83329ZM4.08337 7.58329C4.08337 5.97246 5.38921 4.66663 7.00004 4.66663C8.61087 4.66663 9.91671 5.97246 9.91671 7.58329C9.91671 9.19412 8.61087 10.5 7.00004 10.5C5.38921 10.5 4.08337 9.19412 4.08337 7.58329Z"
                      fill=""
                    />
                  </svg>
                  <input
                    type="file"
                    accept="image/*"
                    capture="environment"
                    name="profile"
                    id="profile"
                    className="sr-only"
                    onChange={(event: any) => {
                      handleFileChange(
                        event,
                        setFileErrorMessage,
                        setIsSuccess,
                        setSelectedFile
                      );
                    }}
                  />
                </label>
              </div>
              <div>OR</div>
              <div>
                <label htmlFor="actual-btn" className="upload underline">
                  {strings.UPLOAD_DOCS}
                </label>
                <input
                  hidden
                  id="actual-btn"
                  type="file"
                  multiple={true}
                  onChange={(event: any) => {
                    handleFileChange(
                      event,
                      setFileErrorMessage,
                      setIsSuccess,
                      setSelectedFile
                    );
                  }}
                  className="w-full rounded-md border border-stroke p-3 outline-none transition file:rounded file:border-[0.5px] file:border-stroke file:bg-[#EEEEEE] file:py-1 file:px-2.5 file:text-sm file:font-medium focus:border-primary file:focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:file:border-strokedark dark:file:bg-white/30 dark:file:text-white"
                />
              </div>
            </div>
            {isSuccess ? (
              <div>
                {FileLists.map((file: any) => {
                  return (
                    <div className="flex items-center justify-between">
                      <div className="mb-2.5 mt-4 block text-left text-sm text-black dark:text-white">
                        {file?.name}
                      </div>
                      <a
                        className="text-red underline"
                        onClick={() => handleDelete(file?.name)}
                      >
                        {strings.DELETE}
                      </a>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="mb-2.5 mt-4 block text-left text-xs text-red dark:text-red">
                {fileErrorMessage}
              </div>
            )}
          </div>
          <div className="mb-5 mt-4">
            {!submitLoading ? (
              <button
                disabled={amount === "" || selectedFile === undefined}
                onClick={(event: any) => {
                  event.preventDefault();
                  submitClaim();
                }}
                type="submit"
                className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
              >
                Submit claim
              </button>
            ) : (
              <LoadingButton className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed" />
            )}
          </div>
        </div>
      )}
    </>
  );
};

export default InitiateNewClaimRequest;
