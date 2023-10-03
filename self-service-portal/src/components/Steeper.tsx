
import React, { useState, useRef, useEffect } from 'react'

interface StepperProps {
  stage:string
}



const Stepper: React.FC<StepperProps> = ({stage}:StepperProps) => {

  const colorNumber = () =>{
    if(stage == "roleSelection"){
      return 0
    }else if(stage == "accountInfo"){
      return 1
    }else if(stage == "setPassword"){
      return 2
    }else{
      return 3
    }
  }

  window.console.log("color stepper ", colorNumber())

return(
<div>
  <h2 className="sr-only">Steps</h2>
  <div>
    <ol className="mb-4 grid grid-cols-3 text-sm font-medium text-gray-500">
      <li className={"flex items-center justify-start sm:gap-1.5" + (1 <= colorNumber() ? " text-primary" : "")}>
        <span className="hidden sm:inline"> Role Selection </span>
      </li>
      <li className={"flex items-center justify-center text-blue-600 sm:gap-1.5" + (2 <= colorNumber() ? " text-primary" : "")}>
        <span className="hidden sm:inline"> Account Info </span>
      </li>
      <li className={"flex items-center justify-end sm:gap-1.5" + (3 <= colorNumber() ? " text-primary" : "")}>
        <span className="hidden sm:inline"> Set Password </span>
      </li>
    </ol>
    <div className="flex flex-wrap">
    <div className="overflow-hidden w-1/3 rounded-full bg-gray-200">
      <div className={"h-2 rounded-full" + (1 <= colorNumber() ? " bg-primary" : " bg-gray")} />
    </div>
    <div className="overflow-hidden w-1/3 rounded-full bg-gray-200">
      <div className={"h-2 rounded-full" + (2 <= colorNumber() ? " bg-primary" : " bg-gray")}/>
    </div>
    <div className="overflow-hidden w-1/3 rounded-full bg-gray-200">
      <div className={"h-2 rounded-full" + (3 <= colorNumber() ? " bg-primary" : " bg-gray")}/>
    </div>
    </div>
  </div>
</div>

)
}
export default Stepper;