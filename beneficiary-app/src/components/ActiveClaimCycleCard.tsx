import { useNavigate } from 'react-router-dom';

const ActiveClaimCycleCard = (Props: any) => {
  const navigate = useNavigate();
  const date = new Date(parseInt(Props.date));
  const day = date.getDate().toString().padStart(2, '0');
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const year = date.getFullYear();
  console.log(Props);

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
  };

  console.log(information);

  const data: any = [
    {
      key: 'Date :',
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
    {
      key: 'API call ID :',
      value: `${Props.apiCallId}`,
    },
    {
      key: 'Status :',
      value: `${Props.status}`,
    },
  ];

  return (
    <div>
      <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex flex-col p-4">
          {data.map((ele: any, index: any) => {
            return (
              <h2
                key={index}
                className="font-small block text-left text-black dark:text-white"
              >
                {ele.key} {ele.value}
              </h2>
            );
          })}
          {/* <h2 className="block text-left font-medium text-black dark:text-white">
            Date : {formattedDate}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Insurance ID : {Props.insurance_id || 'null'}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Treatment/ServiceType : {Props.claimType}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Request ID : {Props.claimID}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Status : {Props.status}
          </h2> */}
          <span
            className="cursor-pointer text-right underline"
            onClick={() =>
              navigate(
                Props.type === 'claim'
                  ? '/view-active-request'
                  : Props.type === undefined
                  ? '/coverage-eligibility'
                  : '/view-active-request',
                { state: information }
              )
            }
          >
            View
          </span>
        </div>
      </div>
    </div>
  );
};

export default ActiveClaimCycleCard;
