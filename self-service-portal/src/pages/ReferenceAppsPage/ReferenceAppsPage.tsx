import { useState } from "react";

const ReferenceAppsPage = () => {
  const [selectedValue, setSelectedValue] = useState("");

  const appLabels = [
    { label: "Visit Other Apps", value: "" },
    { label: "Self-Service Portal (SSP)", value: process.env.ssp },
    { label: "Provider App (OPD)", value: process.env.opd },
    { label: "Beneficiary Service Platform (BSP)", value: process.env.bsp },
    { label: "Assisted Beneficiary Service Platform (ABSP)", value: process.env.absp },
    { label: "Payor App ", value: process.env.payor },
  ];

  const handleNavigation = (event: any) => {
    const url = event.target.value;
    if (url) {
      window.open(url, '_blank');
    }
    setSelectedValue("");
  }

  return (
    <div className="absolute right-0 top-0 p-3 z-20 inline-block rounded bg-white  dark:bg-boxdark">
      <select
        value={selectedValue} 
        onChange={(event) => {
          setSelectedValue(event.target.value); 
          handleNavigation(event); 
        }}
        className="z-20 inline-flex appearance-none rounded border border-stroke bg-transparent py-2 pl-4 pr-9 text-sm font-medium outline-none dark:border-strokedark"
      >
        {appLabels.map((opt: any, index: any) => (
          <option key={index} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
      <span className="absolute right-5 top-1/2 z-10 -translate-y-1/2">
        <svg
          width="18"
          height="18"
          viewBox="0 0 18 18"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M3.96967 6.21967C4.26256 5.92678 4.73744 5.92678 5.03033 6.21967L9 10.1893L12.9697 6.21967C13.2626 5.92678 13.7374 5.92678 14.0303 6.21967C14.3232 6.51256 14.3232 6.98744 14.0303 7.28033L9.53033 11.7803C9.23744 12.0732 8.76256 12.0732 8.46967 11.7803L3.96967 7.28033C3.67678 6.98744 3.67678 6.51256 3.96967 6.21967Z"
            fill="#64748B"
          />
        </svg>
      </span>
    </div>
  );
};

export default ReferenceAppsPage;
