import { useNavigate } from "react-router-dom";

const ActiveClaimCycleCard = (Props: any) => {
  const navigate = useNavigate();
  const date = new Date(parseInt(Props.date));
  const day = date.getDate().toString().padStart(2, "0");
  const month = (date.getMonth() + 1).toString().padStart(2, "0");
  const year = date.getFullYear();

  const formattedDate = `${day}-${month}-${year}`;
  const information = {
    insuranceId: Props.insurance_id,
    serviceType: Props.claimType,
    request_id: Props.claimID,
    status: Props.status,
    apiCallId: Props.apiCallId,
    participantCode: Props.participantCode,
    payorCode: Props.payorCode,
    mobile: localStorage.getItem("mobile"),
    billAmount: Props.billAmount,
    workflowId: Props.workflowId,
    patientMobile: Props.patientMobileNumber,
    patientName: Props.patientName,
  };

  const data: any = [
    {
      key: "PatientName",
      value: Props.patientName,
    },
    {
      key: "Date",
      value: formattedDate,
    },
    {
      key: "Insurance ID",
      value: `${Props.insurance_id || "null"}`,
    },
    {
      key: "ServiceType",
      value: `${Props.claimType}`,
    },
    {
      key: "API call ID",
      value: `${Props.apiCallId}`,
    },
    {
      key: "Status",
      value: (
        <span
          className={`${
            Props.status === "Pending"
              ? "mr-2 rounded bg-warning px-2.5 py-0.5 text-xs font-medium text-gray dark:bg-warning dark:text-gray"
              : "dark:text-green border-green mr-2 rounded bg-success px-2.5 py-0.5 text-xs font-medium text-gray"
          }`}
        >
          {Props.status}
        </span>
      ),
    },
  ];

  return (
    <div>
      <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="flex flex-col p-4">
          {data.map((ele: any, index: any) => {
            return (
              <div className="flex">
                <h2
                  key={index}
                  className="font-small mt-1 block text-left text-black dark:text-white"
                >
                  <b className="inline-block w-30">{ele.key}</b>{" "}
                </h2>
                <span>: {ele.value}</span>
              </div>
            );
          })}
          <span
            className="cursor-pointer text-right underline"
            onClick={() =>
              navigate(
                // Props.type === 'claim'
                //   ? '/coverage-eligibility'
                //   : Props.type === 'coverageeligibility'
                //   ? '/coverage-eligibility'
                //   : '/coverage-eligibility',
                "/coverage-eligibility",
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
