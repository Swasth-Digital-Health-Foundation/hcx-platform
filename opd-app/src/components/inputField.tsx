import React from 'react';

const TextInputWithLabel = ({
  label,
  value,
  onChange,
  placeholder,
  disabled,
  type,
}: any) => {
  return (
    <div className="text-bold mt-3 text-base font-bold text-black dark:text-white">
      {label}
      <div>
        <input
          onChange={onChange}
          value={value}
          disabled={disabled}
          type={type}
          placeholder={placeholder}
          className="mt-2 w-full rounded-lg border-[1.5px] border-stroke bg-white py-3 px-5 font-medium outline-none transition focus:border-primary active:border-primary disabled:cursor-default disabled:bg-whiter dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
        />
      </div>
    </div>
  );
};

export default TextInputWithLabel;
