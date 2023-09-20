import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { handleFileChange } from '../../utils/attachmentSizeValidation';
import { generateOutgoingRequest } from '../../services/hcxMockService';
import LoadingButton from '../../components/LoadingButton';
import { toast } from 'react-toastify';

const InitiateNewClaimRequest = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [selectedFile, setSelectedFile]: any = useState<FileList | undefined>(
    undefined
  );
  const [fileErrorMessage, setFileErrorMessage]: any = useState();
  const [isSuccess, setIsSuccess]: any = useState(false);

  const [amount, setAmount] = useState<any>();
  const [serviceType, setServiceType] = useState<string>('Consultation');
  const [documentType, setDocumentType] = useState<string>('');

  const [loading, setLoading] = useState(false);

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

  const initiateClaimRequestBody = {
    ...location.state.initiateClaimRequestBody,
    billingDeatils: {
      serviceType: serviceType,
      billAmount: amount,
    },
    supportingDocuments: {
      [documentType]:
        FileLists !== undefined
          ? FileLists.map((ele: any) => {
              return ele.name;
            })
          : [],
    },
  };

  // const submitClaim = async () => {
  //   try {
  //     setLoading(true);
  //     const submit = await generateOutgoingRequest(
  //       'claim/submit',
  //       initiateClaimRequestBody
  //     );
  //     console.log(submit);
  //     setLoading(false);
  //     navigate('/request-success', { state: 'new claim' });
  //   } catch (error) {
  //     console.log(error);
  //     setLoading(false);
  //     toast.error('Faild to submit claim, Please try again');
  //   }
  // };
  const claimRequestDetails: any = [
    {
      key: 'Provider name :',
      value: data?.initiateClaimRequestBody?.providerName || '',
    },
    {
      key: 'Participant code :',
      value: data?.initiateClaimRequestBody?.participantCode || '',
    },
    {
      key: 'Select insurance plan :',
      value: data?.initiateClaimRequestBody?.insurancePlan || '',
    },
    {
      key: 'Treatment/Service type :',
      value: data?.initiateClaimRequestBody?.serviceType || '',
    },
    {
      key: 'Payor name :',
      value: data?.initiateClaimRequestBody?.payor || '',
    },
    {
      key: 'Insurance ID :',
      value: data?.initiateClaimRequestBody?.insuranceId || '',
    },
  ];

  return (
    <div className="w-full pt-2 sm:p-12.5 xl:p-1">
      <h2 className="mb-4 text-2xl font-bold text-black dark:text-white sm:text-title-xl2">
        New claim request
      </h2>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        {claimRequestDetails.map((ele: any) => {
          return (
            <>
              <h2 className="text-bold mt-1 text-base font-bold text-black dark:text-white">
                {ele.key}
              </h2>
              <span className="text-base font-medium">{ele.value}</span>
            </>
          );
        })}
      </div>
      <div className="mt-4 rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <h2 className="text-bold text-base font-bold text-black dark:text-white">
          Treatment & billing details :
        </h2>
        <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
          Service type :
        </label>
        <div className="relative z-20 bg-white dark:bg-form-input">
          <select
            onChange={(e: any) => setServiceType(e.target.value)}
            required
            className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark"
          >
            <option value="Consultation">Consultation</option>
          </select>
          <span className="absolute top-1/2 right-4 z-10 -translate-y-1/2">
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <g opacity="0.8">
                <path
                  fillRule="evenodd"
                  clipRule="evenodd"
                  d="M5.29289 8.29289C5.68342 7.90237 6.31658 7.90237 6.70711 8.29289L12 13.5858L17.2929 8.29289C17.6834 7.90237 18.3166 7.90237 18.7071 8.29289C19.0976 8.68342 19.0976 9.31658 18.7071 9.70711L12.7071 15.7071C12.3166 16.0976 11.6834 16.0976 11.2929 15.7071L5.29289 9.70711C4.90237 9.31658 4.90237 8.68342 5.29289 8.29289Z"
                  fill="#637381"
                ></path>
              </g>
            </svg>
          </span>
        </div>
        <div className="mt-4 items-center">
          <h2 className="text-1xl sm:text-title-xl1 mb-4 mt-4 flex w-50 font-bold text-black dark:text-white">
            Bill amount :
          </h2>
          <input
            required
            type="number"
            placeholder="Enter amount"
            className={
              'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
            }
            onChange={(e: any) => setAmount(e.target.value)}
          />
        </div>
      </div>
      <div className="mt-4 rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <h2 className="text-1xl mb-4 font-bold text-black dark:text-white sm:text-title-xl2">
          Supporting documents :
        </h2>
        <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
          Document type :
        </label>
        <div className="relative z-20 mb-4 bg-white dark:bg-form-input">
          <select
            required
            onChange={(e: any) => setDocumentType(e.target.value)}
            className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark"
          >
            <option value="Prescription">Prescription</option>
            <option value="Bill/invoice">Bill/invoice</option>
          </select>
          <span className="absolute top-1/2 right-4 z-10 -translate-y-1/2">
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <g opacity="0.8">
                <path
                  fillRule="evenodd"
                  clipRule="evenodd"
                  d="M5.29289 8.29289C5.68342 7.90237 6.31658 7.90237 6.70711 8.29289L12 13.5858L17.2929 8.29289C17.6834 7.90237 18.3166 7.90237 18.7071 8.29289C19.0976 8.68342 19.0976 9.31658 18.7071 9.70711L12.7071 15.7071C12.3166 16.0976 11.6834 16.0976 11.2929 15.7071L5.29289 9.70711C4.90237 9.31658 4.90237 8.68342 5.29289 8.29289Z"
                  fill="#637381"
                ></path>
              </g>
            </svg>
          </span>
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
              Upload documents
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
                    Delete
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
        {!loading ? (
          <button
            disabled={false}
            onClick={(event: any) => {
              event.preventDefault();
              navigate('/view-claim-request', {
                state: { initiateClaimRequestBody },
              });
            }}
            type="submit"
            className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
          >
            Submit claim
          </button>
        ) : (
          <LoadingButton />
        )}
      </div>
    </div>
  );
};

export default InitiateNewClaimRequest;
