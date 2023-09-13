const PayorDetailsCard = ({ onInputChange, cardKey }: any) => {
  const handleInputChange = (event: any) => {
    const { name, value } = event.target;
    // Pass the updated data back to the parent component
    onInputChange({ [name]: value });
  };
  return (
    <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
      <div className="flex flex-col gap-5.5 p-4">
        <div>
          <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
            Payor Details *
          </label>
          <div className="relative z-20 bg-white dark:bg-form-input">
            <select
              required
              name={`payor`}
              onChange={handleInputChange}
              className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-4 px-6 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input"
            >
              <option value="none">none</option>
              <option value="Swast payor">Swast payor</option>
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
        </div>
        <div>
          <label className="mb-2.5 block text-left font-medium text-black dark:text-white">
            Insurance ID *
          </label>
          <div className="relative">
            <input
              required
              name={`insurance_id`}
              onChange={handleInputChange}
              type="text"
              placeholder="Insurance ID"
              className={
                'w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary'
              }
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default PayorDetailsCard;
