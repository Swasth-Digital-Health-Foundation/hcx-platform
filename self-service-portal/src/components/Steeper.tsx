import React from 'react';

interface StepperProps {
  stage: string;
}

const Stepper: React.FC<StepperProps> = ({ stage }) => {

  const getStageColor = (step: number) => {
    const stages = ["roleSelection", "accountInfo", "setPassword","onboardingSuccess"];
    const currentStageIndex = stages.indexOf(stage);

    if (step < currentStageIndex) {
      return "bg-green";
    } else if (step === currentStageIndex) {
      return "bg-primary";
    } else {
      return "bg-gray";
    }
  };

  return (
    <div>
      <h2 className="sr-only">Steps</h2>
      <div>
        <ol className="mb-4 grid grid-cols-3 text-sm font-medium text-gray-500">
          <li className={"flex items-center justify-start sm:gap-1.5" + (stage === "roleSelection" ? " text-primary" : "")}>
            <span className="hidden sm:inline"> Role Selection </span>
          </li>
          <li className={"flex items-center justify-center sm:gap-1.5" + (stage === "accountInfo" ? " text-primary" : "")}>
            <span className="hidden sm:inline"> Account Info </span>
          </li>
          <li className={"flex items-center justify-end sm:gap-1.5" + (stage === "setPassword" ? " text-primary" : "")}>
            <span className="hidden sm:inline"> Set Password </span>
          </li>
        </ol>
        <div className="flex flex-wrap">
          <div className="overflow-hidden w-1/3 rounded-full bg-gray-200">
            <div className={"h-2 rounded-full " + getStageColor(0)} />
          </div>
          <div className="overflow-hidden w-1/3 rounded-full bg-gray-200">
            <div className={"h-2 rounded-full " + getStageColor(1)} />
          </div>
          <div className="overflow-hidden w-1/3 rounded-full bg-gray-200">
            <div className={"h-2 rounded-full " + getStageColor(2)} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Stepper;
