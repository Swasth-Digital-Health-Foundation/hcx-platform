import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const ActiveClaimCycleCard = (Props: any) => {
  const navigate = useNavigate();
  const date = new Date(parseInt(Props.date));
  const day = date.getDate().toString().padStart(2, '0');
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const year = date.getFullYear();
  const [popup, setPopup] = useState(false);

  const formattedDate = `${day}-${month}-${year}`;
  const information = {
    insuranceId: Props.insurance_id,
    serviceType: Props.claimType,
    request_id: Props.claimID,
    status: Props.status,
    apiCallId: Props.apiCallId,
    participantCode: Props.participantCode,
    payorCode: Props.payorCode,
    mobile: localStorage.getItem('mobile'),
    billAmount: Props.billAmount,
    workflowId: Props.workflowId,
    patientName: Props.patientName,
  };

  const data: any = [
    {
      key: 'Patient name :',
      value: `${Props.patientName}`,
    },
    {
      key: 'Initiation date :',
      value: formattedDate,
    },
    {
      key: 'Insurance ID :',
      value: `${Props.insurance_id || 'null'}`,
    },
    {
      key: 'Treatment/ServiceType :',
      value: `${Props.claimType}`,
    },
    // {
    //   key: 'API call ID :',
    //   value: `${Props.apiCallId}`,
    // },
    {
      key: 'Status :',
      value: (
        <span
          className={`${Props.status === 'Pending'
            ? 'mr-2 rounded bg-warning px-2.5 py-0.5 text-xs font-medium text-gray dark:bg-warning dark:text-gray'
            : 'dark:text-green border-green mr-2 rounded bg-success px-2.5 py-0.5 text-xs font-medium text-gray'
            }`}
        >
          {Props.status}
        </span>
      ),
    },
  ];

  return (
    <div>
      <div className="relative rounded-lg border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex flex-col p-4">
          {data.map((ele: any, index: any) => {
            return (
              <h2
                key={index}
                className="font-small mt-1 block text-left text-black dark:text-white"
              >
                <b>{ele.key}</b> <span>{ele.value}</span>
              </h2>
            );
          })}
          <span
            className="cursor-pointer text-right underline"
            onClick={() =>
              navigate(
                Props.type === 'claim'
                  ? '/view-active-request'
                  : Props.type === 'coverageeligibility'
                    ? '/coverage-eligibility'
                    : '/view-active-request',
                { state: information }
              )
            }
          >
            View details
          </span>
        </div>
        <div className='absolute top-2 right-2' onClick={() => setPopup(!popup)}>
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6">
            <path strokeLinecap="round" strokeLinejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z" />
          </svg>
        </div>
        {popup ? <div className='absolute top-8 right-2 bg-black text-white p-4'>
          Api call Id : {Props.apiCallId}
        </div> : null}
      </div>
    </div>
  );
};

export default ActiveClaimCycleCard;
