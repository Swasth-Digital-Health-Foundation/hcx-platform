import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { handleFileChange } from '../../utils/attachmentSizeValidation';
import strings from '../../utils/strings';
import LoadingButton from '../../components/LoadingButton';
import { generateOutgoingRequest } from '../../services/hcxMockService';
import { toast } from 'react-toastify';
import { generateToken, searchParticipant } from '../../services/hcxService';
import * as _ from 'lodash';
import axios from 'axios';
import { postRequest } from '../../services/registryService';

const PreAuthRequest = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const [selectedFile, setSelectedFile]: any = useState<FileList | undefined>(
    undefined
  );
  const [fileErrorMessage, setFileErrorMessage]: any = useState();
  const [isSuccess, setIsSuccess]: any = useState(false);

  const [estimatedAmount, setAmount] = useState<any>();
  const [serviceType, setServiceType] = useState<string>('Consultation');
  const [documentType, setDocumentType] = useState<string>('Prescription');

  const [loading, setLoading] = useState(false);

  const [token, setToken] = useState<string>('');

  const [providerName, setProviderName] = useState<string>('');
  const [payorName, setPayorName] = useState<string>('');
  const [fileUrlList, setUrlList] = useState<any>([]);

  const [userInfo, setUserInformation] = useState<any>([]);

  let FileLists: any;
  if (selectedFile !== undefined) {
    FileLists = Array.from(selectedFile);
  }

  // const data = location.state;

  const handleDelete = (name: any) => {
    if (selectedFile !== undefined) {
      const updatedFilesList = selectedFile.filter(
        (file: any) => file.name !== name
      );
      setSelectedFile(updatedFilesList);
    }
  };

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

  const requestPayload = {
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

  const tokenRequestBody = {
    username: process.env.TOKEN_GENERATION_USERNAME,
    password: process.env.TOKEN_GENERATION_PASSWORD,
  };

  const config = {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };

  useEffect(() => {
    const search = async () => {
      try {
        const tokenResponse = await generateToken(tokenRequestBody);
        if (tokenResponse.statusText === 'OK') {
          setToken(tokenResponse.data.access_token);
        }
      } catch (err) {
        console.log(err);
      }
    };
    search();
  }, []);

  useEffect(() => {
    try {
      if (token !== undefined) {
        const search = async () => {
          const response = await searchParticipant(
            participantCodePayload,
            config
          );
          setProviderName(response.data?.participants[0].participant_name);

          const payorResponse = await searchParticipant(
            payorCodePayload,
            config
          );
          setPayorName(payorResponse.data?.participants[0].participant_name);
        };
        search();
      }
    } catch (err) {
      console.log(err);
    }
  }, [token]);

  const handleUpload = async () => {
    // const mobile = localStorage.getItem('monile');
    // try {
    //   setLoading(true);
    //   toast.info('Uploading documents please wait...!');
    //   const formData = new FormData();
    //   formData.append('mobile', `${mobile}`);

    //   FileLists.forEach((file: any) => {
    //     console.log(file);
    //     formData.append(`file`, file);
    //   });

    //   const headers = {
    //     Authorization: `Bearer ${token}`,
    //   };

    //   const response = await axios({
    //     url: 'https://dev-hcx.swasth.app/api/v0.7/upload/documents',
    //     method: 'POST',
    //     headers: headers,
    //     data: formData,
    //   });
    //   let responseData = response.data;
    //   setUrlList(responseData);
    //   toast.info('Documents uploaded successfully!');
    //   if (response.status === 200) {
    //     const submit = await generateOutgoingRequest(
    //       'create/preauth/submit',
    //       requestPayload
    //     );
    //     console.log(submit);
    //     // if (response.status === 202) {
    //     navigate('/request-success', {
    //       state: {
    //         text: 'new preauth',
    //         mobileNumber: location.state?.mobile,
    //       },
    //     });
    //     // }
    //   }
    //   setLoading(false);
    // } catch (error) {
    //   setLoading(false);
    //   console.error('Error uploading file', error);
    // }
    let mobile = localStorage.getItem('mobile');
    try {
      setLoading(true);
      const formData = new FormData();
      formData.append('mobile', `${mobile}`);

      FileLists.forEach((file: any) => {
        formData.append(`file`, file);
      });

      toast.info('Uploading documents please wait...!');
      const response = await axios({
        url: `${process.env.hcx_mock_service}/upload/documents`,
        method: 'POST',
        data: formData,
      });
      let obtainedResponse = response.data;
      setUrlList((prevFileUrlList: any) => [
        ...prevFileUrlList,
        ...obtainedResponse,
      ]);
      toast.info('Documents uploaded successfully!');
      setLoading(false);
    } catch (error) {
      setLoading(false);
      console.error('Error uploading file', error);
    }
  };

  const submitPreauth = async () => {
    try {
      setLoading(true);
      let getUrl = await generateOutgoingRequest(
        'create/preauth/submit',
        requestPayload
      );
      setLoading(false);
      navigate('/request-success', {
        state: {
          text: 'preauth',
          mobileNumber: localStorage.getItem('mobile'),
        },
      });
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
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
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
      <div className="mt-4 rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
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
      <div className="mt-4 rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        <h2 className="text-1xl mb-4 font-bold text-black dark:text-white sm:text-title-xl2">
          {strings.SUPPORTING_DOCS}
        </h2>
        <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
          {strings.DOC_TYPE}
        </label>
        <div className="relative z-20 mb-4 bg-white dark:bg-form-input">
          <select
            onChange={(e: any) => {
              setDocumentType(e.target.value);
            }}
            required
            className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark"
          >
            <option value="Bill/invoice">Medical Bill/invoice</option>
            <option value="Payment Receipt">Payment Receipt</option>
            <option value="Prescription">Prescription</option>
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
        {!loading ? (
          <div
            onClick={() => {
              if (fileUrlList !== 0) {
                handleUpload();
              }
            }}
            className="mx-auto"
          >
            <button className="align-center text-balck m-auto mt-4 flex w-60 justify-center rounded bg-gray font-medium disabled:cursor-not-allowed disabled:bg-secondary disabled:text-gray">
              Click here to upload documents
            </button>
          </div>
        ) : (
          <span className='m-auto'>Please wait</span>
        )}
      </div>
      <div className="mb-5 mt-4">
        {!loading ? (
          <button
            disabled={estimatedAmount === '' || fileUrlList.length === 0}
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
          <LoadingButton className="align-center mt-4 flex w-full justify-center rounded bg-primary py-4 font-medium text-gray disabled:cursor-not-allowed"/>
        )}
      </div>
    </div>
  );
};

export default PreAuthRequest;
