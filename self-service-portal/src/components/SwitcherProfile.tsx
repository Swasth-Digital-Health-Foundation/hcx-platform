import { useState } from 'react';

interface SwitcherProps {
  value:boolean
  onSwitch:(value:boolean) => void;
}

const SwitcherFour:React.FC<SwitcherProps> = ({value, onSwitch}:SwitcherProps) => {
  const [enabled, setEnabled] = useState<boolean>(value);

  return (
    <div>
      <label
        htmlFor="toggle4"
        className="flex cursor-pointer select-none items-center"
      >
        <div className="relative">
          <input
            type="checkbox"
            id="toggle4"
            className="sr-only"
            onChange={() => {
              onSwitch(!enabled);
              setEnabled(!enabled);
            }}
          />
          <div className="block h-6 w-10 rounded-full bg-black"></div>
          <div
            className={`absolute left-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-white transition ${
              enabled && '!right-1 !translate-x-full'
            }`}
          ></div>
        </div>
      </label>
    </div>
  );
};

export default SwitcherFour;
