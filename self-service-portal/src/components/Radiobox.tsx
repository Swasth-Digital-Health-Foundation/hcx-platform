import { useState } from 'react';

interface RadioProps {
  text: string,
  onRadioCheck:(text:string) => void;
}


const RadioBox:React.FC<RadioProps> = ({ text, onRadioCheck}: RadioProps) => {
  const [isChecked, setIsChecked] = useState<boolean>(false);

  return (
    <div>
      <label
        htmlFor={text}
        className="flex cursor-pointer select-none items-center"
      >
        <div className="relative">
          <input
            type="checkbox"
            id={text}
            className="sr-only"
            onChange={() => {
              setIsChecked(!isChecked);
              onRadioCheck(text);
            }}
          />
          <div
            className={`mr-4 flex h-5 w-5 items-center justify-center rounded-full border ${
              isChecked && 'border-primary'
            }`}
          >
            <span
              className={`h-2.5 w-2.5 rounded-full bg-transparent ${
                isChecked && '!bg-primary'
              }`}
            >
              {' '}
            </span>
          </div>
        </div>
        {text}
      </label>
    </div>
  );
};

export default RadioBox;
