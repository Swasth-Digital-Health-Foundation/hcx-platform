import { useNavigate } from 'react-router-dom';

const ActiveClaimCycleCard = () => {
  const navigate = useNavigate();
  return (
    <div>
      <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex flex-col p-4">
          <h2 className="block text-left font-medium text-black dark:text-white">
            Date :
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Insurance ID :
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Request Type :
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Request ID :
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Status :
          </h2>
          <span
            className="cursor-pointer text-right underline"
            onClick={() => navigate('/coverage-eligibility')}
          >
            View
          </span>
        </div>
      </div>
      <div className="mt-2 rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex flex-col p-4">
          <h2 className="block text-left font-medium text-black dark:text-white">
            Date :
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Insurance ID :
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Request Type :
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Request ID :
          </h2>
          <h2 className="block text-left font-medium text-black dark:text-white">
            Status :
          </h2>
          <span
            className="cursor-pointer text-right underline"
            onClick={() => navigate('/coverage-eligibility')}
          >
            View
          </span>
        </div>
      </div>
    </div>
  );
};

export default ActiveClaimCycleCard;
