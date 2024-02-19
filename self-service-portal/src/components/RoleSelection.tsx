import React, { useState, useRef, useEffect, ChangeEvent } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { addAppData } from '../reducers/app_data';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import _ from 'lodash';
import { RootState } from '../store';
import TermsOfUse from './TermsOfUse';
import MultiSelectDropdown from './MultiSelectDropdown';

type Payor = {
  participant_code: string,
  participant_name: string
}

interface RoleSelectionProps {
  payorList: Payor[] | undefined,
  onRoleSubmit: () => void;
}


const RoleSelection: React.FC<RoleSelectionProps> = ({ payorList, onRoleSubmit }: RoleSelectionProps) => {

  const dispatch = useDispatch();
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  const [radioRole, setRadioRole] = useState('provider');
  const [payorSelected, setPayorSelected] = useState(process.env.REACT_APP_MOCK_PAYOR_CODE);
  const [applicantCode, setApplicantCode] = useState('');
  const [applicantError, setApplicantError] = useState(false);
  const [providerOptions, setProviderOptions] = useState<Array<string>>(["provider.hospital"]);

  useEffect(() => {
    dispatch(addAppData({ "roleSelectedRegister":"provider"}));
    dispatch(addAppData({ "payorSelectedCodeRegister":process.env.REACT_APP_MOCK_PAYOR_CODE}));
    dispatch(addAppData({ "applicantCodeRegister":""}));
  },[])

  const onSubmit = () => {
    window.console.log("applicant missing", radioRole, payorSelected, applicantCode);
    if(radioRole == "provider" && payorSelected == process.env.REACT_APP_MOCK_PAYOR_CODE){
      dispatch(addAppData({ "payorSelectedCodeRegister": "" }))
      dispatch(addAppData({"providerOptions" : providerOptions}));
      setApplicantCode("");
      return onRoleSubmit();
    }
    if(radioRole == "provider" && payorSelected !== process.env.REACT_APP_MOCK_PAYOR_CODE && applicantCode == ""){
      toast.error("Applicant code can not be empty");
      setApplicantError(true);
    }else{
    return onRoleSubmit();
    }
  }

  const setShowTerms = (event: ChangeEvent<HTMLInputElement>) => {
    console.log("event.target.value", event.target.checked);
    dispatch(addAppData({ "showTerms": event.target.checked }));
    dispatch(addAppData({ "termsAccepted": event.target.checked }));
  }

  return (
    <>
      <TermsOfUse></TermsOfUse>
      <div className="mb-4">
        <label className="mb-2.5 block font-medium text-black dark:text-white">
          Role
        </label>
        <div className="relative z-20 bg-transparent dark:bg-form-input">
          <select className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-3 px-5 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
            onChange={(event) => { setRadioRole(event.target.value); dispatch(addAppData({ "roleSelectedRegister": event.target.value })) }}>
            <option selected value="provider">Provider</option>
            <option value="payor">Payor</option>
            <option value="bsp">BSP</option>
            <option value="member.isnp">Member ISNP</option>
        
          </select>
          <span className="absolute top-1/2 right-4 z-30 -translate-y-1/2">
            <svg
              className="fill-current"
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
                  fill=""
                ></path>
              </g>
            </svg>
          </span>
        </div>
      </div>
      {radioRole == "provider" ?
        <>
        <div className="mb-4">
            <label className="mb-2.5 block font-medium text-black dark:text-white">
              Select Provider Roles
            </label>
            <MultiSelectDropdown options={["provider.hospital","provider.clinic","provider.practitioner","provider.diagnostics","provider.pharmacy"]} onSelect={function (selected: string[]): void {
                setProviderOptions(selected); console.log(selected);
            } }></MultiSelectDropdown>
          </div>
          <div className="mb-4">
            <label className="mb-2.5 block font-medium text-black dark:text-white">
              Select Payor
            </label>
            <div className="relative z-20 bg-transparent dark:bg-form-input">
              <select className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-3 px-5 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                onChange={(event) => { setPayorSelected(event.target.value); dispatch(addAppData({ "payorSelectedCodeRegister": event.target.value })) }} >
                {payorList !== undefined ? payorList.map((value, index) => {
                  if(value.participant_code == process.env.REACT_APP_MOCK_PAYOR_CODE){
                    return <option selected value={value.participant_code}>{value.participant_name}</option>  
                  }
                  return <option value={value.participant_code}>{value.participant_name}</option>
                }) : null}
              </select>
              <span className="absolute top-1/2 right-4 z-30 -translate-y-1/2">
                <svg
                  className="fill-current"
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
                      fill=""
                    ></path>
                  </g>
                </svg>
              </span>
            </div>

          </div>
          {payorSelected !== process.env.REACT_APP_MOCK_PAYOR_CODE ?
          <div className="mb-4">
            <label className="mb-2.5 block font-medium text-black dark:text-white">
              Applicant code
            </label>
            <div className="relative">
              <input
                type="text"
                placeholder="Enter application role"
                value={applicantCode}
                required
                className={"w-full rounded-lg border border-stroke bg-transparent py-4 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary" + (applicantError ? " !border-danger" : "")}
                onChange={(event) => { setApplicantCode(event.target.value); dispatch(addAppData({ "applicantCodeRegister": event.target.value })); setApplicantError(false)}}
              />

              <span className="absolute right-4 top-4">
                <svg
                  className="fill-current"
                  width="22"
                  height="22"
                  viewBox="0 0 22 22"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <g opacity="0.5">
                    <path
                      d="M11.0008 9.52185C13.5445 9.52185 15.607 7.5281 15.607 5.0531C15.607 2.5781 13.5445 0.584351 11.0008 0.584351C8.45703 0.584351 6.39453 2.5781 6.39453 5.0531C6.39453 7.5281 8.45703 9.52185 11.0008 9.52185ZM11.0008 2.1656C12.6852 2.1656 14.0602 3.47185 14.0602 5.08748C14.0602 6.7031 12.6852 8.00935 11.0008 8.00935C9.31641 8.00935 7.94141 6.7031 7.94141 5.08748C7.94141 3.47185 9.31641 2.1656 11.0008 2.1656Z"
                      fill=""
                    />
                    <path
                      d="M13.2352 11.0687H8.76641C5.08828 11.0687 2.09766 14.0937 2.09766 17.7719V20.625C2.09766 21.0375 2.44141 21.4156 2.88828 21.4156C3.33516 21.4156 3.67891 21.0719 3.67891 20.625V17.7719C3.67891 14.9531 5.98203 12.6156 8.83516 12.6156H13.2695C16.0883 12.6156 18.4258 14.9187 18.4258 17.7719V20.625C18.4258 21.0375 18.7695 21.4156 19.2164 21.4156C19.6633 21.4156 20.007 21.0719 20.007 20.625V17.7719C19.9039 14.0937 16.9133 11.0687 13.2352 11.0687Z"
                      fill=""
                    />
                  </g>
                </svg>
              </span>
            </div>
            {applicantError ? <p className='text-danger italic'>* Please enter valid applicant code</p> : null }
          </div> : null}
        </> : null}
        <div className="flex items-center justify-center mt-10 mb-4">
                                <input id="link-checkbox" type="checkbox" value="" className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600"
                                  onChange={(event) => setShowTerms(event)}
                                  checked={_.get(appData, "termsAccepted")}></input>
                                <label htmlFor="link-checkbox" className="ml-2 label-primary">I agree with the <a href="#" onClick={() => dispatch(addAppData({ "showTerms": true }))} className="text-blue-600 dark:text-blue-500 hover:underline">terms and conditions</a>.</label>
                              </div>
      {_.get(appData, "termsAccepted") ?
      <div className="my-5">
        <input
          type="submit"
          value="Next"
          className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90"
          onClick={(event) => { event.preventDefault(); onSubmit() }}
        />
      </div> : 
      <div className="my-5">
            <input
              type="submit"
              disabled
              value="Next"
              className="w-full cursor-pointer rounded-lg border border-primary bg-primary p-4 text-white transition hover:bg-opacity-90 disabled:bg-opacity-90"
              onClick={(event) => { event.preventDefault(); onSubmit() }}
            />
          </div>}

      <div className="mt-6 text-center">
        <p>
          Already have an account?{' '}
          <Link to="/onboarding/login" className="text-primary">
            Sign in
          </Link>
        </p>
      </div>
    </>
  )
}

export default RoleSelection;