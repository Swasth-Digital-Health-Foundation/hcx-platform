import { useNavigate } from 'react-router-dom';

const ActiveClaimCycleCard = (Props: any) => {
  const navigate = useNavigate();
  return (
    <div>
      <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex flex-col p-4">
          <h2 className="block text-left font-medium text-black dark:text-white">
            Date : {Props.date}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Insurance ID : {Props.insurance_id}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Request Type : {Props.claimType}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Request ID : {Props.claimId}
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Status : {Props.status}
          </h2>
          <span
            className="cursor-pointer text-right underline"
            onClick={() => navigate(Props.link)}
          >
            View
          </span>
        </div>
      </div>
    </div>
  );
};

export default ActiveClaimCycleCard;
