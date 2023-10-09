import React from 'react';

const CustomButton = ({ disabled, onClick, text,className }: any) => {
  return (
    <button
      disabled={disabled}
      onClick={onClick}
      className={`${className} align-center mt-4 flex w-full justify-center rounded py-3 font-medium text-gray ${
        disabled
          ? 'cursor-not-allowed bg-secondary text-gray'
          : 'cursor-pointer bg-primary text-white'
      }`}
    >
      {text}
    </button>
  );
};

export default CustomButton;
