import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import strings from '../../utils/strings';
import LoadingButton from '../../components/LoadingButton';
import { generateOutgoingRequest, handleUpload } from '../../services/hcxMockService';
import { toast } from 'react-toastify';
import { generateToken, searchParticipant } from '../../services/hcxService';
import * as _ from 'lodash';
import { postRequest } from '../../services/registryService';
import SupportingDocuments from '../../components/SupportingDocuments';

const PreAuthRequest = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const [selectedFile, setSelectedFile]: any = useState<FileList | undefined>(
    undefined
  );
  const [fileErrorMessage, setFileErrorMessage]: any = useState();
  const [isSuccess, setIsSuccess]: any = useState(false);

  const [estimatedAmount, setAmount] = useState<string>('');
  const [serviceType, setServiceType] = useState<string>('Consultation');
  const [documentType, setDocumentType] = useState<string>('Prescription');
  const [loading, setLoading] = useState(false);
  const [providerName, setProviderName] = useState<string>('');
  const [payorName, setPayorName] = useState<string>('');
  const [fileUrlList, setUrlList] = useState<any>([]);
  let mobile: any = localStorage.getItem('mobile');
  const [userInfo, setUserInformation] = useState<any>([]);

  let FileLists: any;
  if (selectedFile !== undefined) {
    FileLists = Array.from(selectedFile);
  }

  const dataFromCard = location.state;

  const preauthRequestDetails: any = [
    {
      key: 'Provider name :',
      value: dataFromCard?.providerName || '',
    },
    {
      key: 'Participant code :',
      value: dataFromCard?.participantCode || '',
    },
    {
      key: 'Treatment/Service type :',
      value: dataFromCard?.serviceType || '',
    },
    {
      key: 'Payor name :',
      value: payorName,
    },
    {
      key: 'Insurance ID :',
      value: dataFromCard?.insuranceId || '',
    },
  ];

  const filter = {
    entityType: ['Beneficiary'],
    filters: {
      mobile: { eq: localStorage.getItem('mobile') },
    },
  };

  useEffect(() => {
    const search = async () => {
      try {
        const searchUser = await postRequest('/search', filter);
        setUserInformation(searchUser.data);
      } catch (error) {
        console.log(error);
      }
    };
    search();
  }, []);

  const requestBody = {
    providerName: dataFromCard?.providerName || providerName,
    participantCode: dataFromCard?.participantCode,
    serviceType: dataFromCard?.serviceType,
    payor: payorName,
    insuranceId: dataFromCard?.insuranceId,
    mobile: localStorage.getItem('mobile'),
    billAmount: estimatedAmount,
    patientName: userInfo[0]?.name,
    workflowId: dataFromCard?.workflowId,
    supportingDocuments: [
      {
        documentType: documentType,
        urls: fileUrlList.map((ele: any) => {
          return ele.url;
        }),
      },
    ],
    type: "OPD"
  };

  const participantCodePayload = {
    filters: {
      participant_code: { eq: location.state?.participantCode },
    },
  };

  const payorCodePayload = {
    filters: {
      participant_code: { eq: location.state?.payorCode },
    },
  };

  useEffect(() => {
    try {
      const search = async () => {
        const tokenResponse = await generateToken();
        let token = tokenResponse.data?.access_token;
        const response = await searchParticipant(participantCodePayload, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setProviderName(response.data?.participants[0].participant_name);

        const payorResponse = await searchParticipant(payorCodePayload, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setPayorName(payorResponse.data?.participants[0].participant_name);
      };
      search();
    } catch (err) {
      console.log(err);
    }
  }, []);

  const submitPreauth = async () => {
    try {
      setLoading(true);
      handleUpload(mobile, FileLists, requestBody, setUrlList);
      setTimeout(async () => {
        let submitPreauth = await generateOutgoingRequest(
          'create/preauth/submit',
          requestBody
        );
        setLoading(false);
        toast.success("Pre-auth request initiated successfully!")
        navigate('/home', {
          state: {
            text: 'preauth',
            mobileNumber: localStorage.getItem('mobile'),
          },
        });
      }, 2000);
    } catch (err) {
      setLoading(false);
      toast.error('Faild to submit claim, try again!');
    }
  };

  return (
    <div className="w-full">
      <h2 className="mb-4 text-2xl font-bold text-black dark:text-white sm:text-title-xl2">
        {strings.NEW_PREAUTH_REQUEST}
      </h2>
      <div className="rounded-lg border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        {preauthRequestDetails.map((ele: any) => {
          return (
            <div className="mb-2">
              <h2 className="text-bold text-base font-bold text-black dark:text-white">
                {ele.key}
              </h2>
              <span className="text-base font-medium">{ele.value}</span>
            </div>
          );
        })}
      </div>
      <div className="mt-4 rounded-lg border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <h2 className="text-bold mb-4 text-base font-bold text-black dark:text-white">
          {strings.TREATMENT_AND_BILLING_DETAILS}
        </h2>
        <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
          {strings.SERVICE_TYPE}
        </label>
        <div className="relative z-20 bg-white dark:bg-form-input">
          <select
            onChange={(e: any) => {
              setServiceType(e.target.value);
            }}
            required
            className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark"
          >
            <option value="Consultation">Consultation</option>
            <option value="Drugs">Drugs</option>
            <option value="Wellness">Wellness</option>
            <option value="Diagnostics">Diagnostics</option>
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
          <h2 className="mb-2.5 block text-left font-medium text-black dark:text-white">
            {strings.ESTIMATED_BILL_AMOUNT}
          </h2>
          <input
            onChange={(e: any) => {
              setAmount(e.target.value);
            }}
            required
            type="number"
            placeholder="Enter amount"
            className={
              'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
            }
          />
        </div>
      </div>
      <SupportingDocuments
        setDocumentType={setDocumentType}
        setFileErrorMessage={setFileErrorMessage}
        setIsSuccess={setIsSuccess}
        setSelectedFile={setSelectedFile}
        isSuccess={isSuccess}
        FileLists={FileLists}
        fileErrorMessage={fileErrorMessage}
        selectedFile={selectedFile} />
      <div className="mb-5 mt-4">
        {!loading ? (
          <button
            disabled={estimatedAmount === '' || fileErrorMessage}
            onClick={(event: any) => {
              event.preventDefault();
              submitPreauth();
            }}
            type="submit"
            className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray"
          >
            {strings.SUBMIT_CLAIM}
          </button>
        ) : (
          <LoadingButton className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed" />
        )}
      </div>
    </div>
  );
};

export default PreAuthRequest;
