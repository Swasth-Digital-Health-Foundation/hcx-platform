import { useNavigate } from 'react-router-dom';

const ActiveClaimCycleCard = (Props: any) => {
  const navigate = useNavigate();
  const date = new Date(parseInt(Props.date));
  const formattedDate = date.toISOString().split('T')[0];
  const information = {
    insurance_id: Props.insurance_id,
    request_type: Props.claimType,
    request_id: Props.claimID,
    status: Props.status,
    claim_id: Props.claimID,
    participant_code: Props.participantCode,
  };

  return (
    <div>
      <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex flex-col p-4">
          <h2 className="block text-left font-medium text-black dark:text-white">
            Date : {formattedDate}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Insurance ID : {Props.insurance_id || 'null'}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Request Type : {Props.claimType}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Request ID : {Props.claimID}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Status : {Props.status}
          </h2>
          <span
            className="cursor-pointer text-right underline"
            onClick={() => navigate(Props.link, { state: information })}
          >
            View
          </span>
        </div>
      </div>
    </div>
  );
};

export default ActiveClaimCycleCard;
