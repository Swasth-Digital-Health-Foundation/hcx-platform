import _ from 'lodash';
import React, { useState, useEffect, useRef } from 'react';



interface ModalUserUpdateProps {
  show:boolean,
  values:Object,
  onSubmitClick:(role:string) => void,
  onCancelClick: () =>void;
}


const ModalUserUpdate: React.FC<ModalUserUpdateProps> = ({show,values, onSubmitClick, onCancelClick}:ModalUserUpdateProps) => {
  
  const [modalOpen, setModalOpen] = useState(show);
  const [roleSelected, setRoleSelected] = useState('admin');
  const trigger = useRef<any>(null);
  const modal = useRef<any>(null);
  console.log("modalOpen", modalOpen);
  
  

  // // close on click outside
  // useEffect(() => {
  //   const clickHandler = ({ target }: MouseEvent) => {
  //     if (!modal.current) return;
  //     if (
  //       !modalOpen ||
  //       modal.current.contains(target) ||
  //       trigger.current.contains(target)
  //     )
  //       return;
  //     setModalOpen(false);
  //   };
  //   document.addEventListener('click', clickHandler);
  //   return () => document.removeEventListener('click', clickHandler);
  // });

  // // close if the esc key is pressed
  // useEffect(() => {
  //   const keyHandler = ({ keyCode }: KeyboardEvent) => {
  //     if (!modalOpen || keyCode !== 27) return;
  //     setModalOpen(false);
  //   };
  //   document.addEventListener('keydown', keyHandler);
  //   return () => document.removeEventListener('keydown', keyHandler);
  // });

  return (
    <div>
      <div
        className={`fixed top-0 left-0 z-999999 flex h-full min-h-screen w-full items-center justify-center bg-black/90 px-4 py-5 ${
          modalOpen ? 'block' : 'hidden'
        }`}
      >
        <div
          ref={modal}
          onFocus={() => setModalOpen(true)}
          onBlur={() => setModalOpen(false)}
          className="w-full max-w-142.5 rounded-lg bg-white py-12 px-8 text-center dark:bg-boxdark md:py-15 md:px-17.5"
        >
          <h3 className="pb-2 text-xl font-bold text-black dark:text-white sm:text-2xl">
            Update User Information
          </h3>
          <span className="mx-auto mb-6 inline-block h-1 w-22.5 rounded bg-primary"></span>
            <form action="#">
              <div className="p-6.5 mb-10">
                <div className='flex flex-wrap mb-5'>
                <label className="mb-2.5 w-1/3 block text-black dark:text-white text-start">
                      User Email:
                  </label>
                  <label className="mb-2.5 w-2/3 block font-medium dark:text-white text-start">
                      {_.get(values,"email")}
                  </label>
                </div>
                <div className='flex flex-wrap mb-5'>
                <label className="mb-2.5 w-1/3 block text-black dark:text-white text-start">
                      Participant Code: 
                  </label>
                  <label className="mb-2.5 w-2/3 block font-medium dark:text-white text-start">
                      {_.get(values,"participant_code")}
                  </label>
                </div>
                <div>
        <label className="mb-2.5 block font-medium text-black dark:text-white text-start">
          Role
        </label>
        <div className="relative z-20 bg-transparent dark:bg-form-input">
          <select className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-3 px-5 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
             onChange={(event) => setRoleSelected(event.target.value)}>
                                <option value="admin">Admin</option>
                                <option value="config-manager">Config-manager</option>
                                <option value="viewer">Viewer</option>
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
              </div>
            </form>

          <div className="-mx-3 flex flex-wrap gap-y-4">
            <div className="w-full px-3 2xsm:w-1/2">
              <button
                onClick={() => onCancelClick()}
                className="block w-full rounded border border-stroke bg-gray p-3 text-center font-medium text-black transition hover:border-meta-1 hover:bg-meta-1 hover:text-white dark:border-strokedark dark:bg-meta-4 dark:text-white dark:hover:border-meta-1 dark:hover:bg-meta-1"
              >
                Cancel
              </button>
            </div>
            <div className="w-full px-3 2xsm:w-1/2">
              <button className="block w-full rounded border border-primary bg-primary p-3 text-center font-medium text-white transition hover:bg-opacity-90"
                onClick={()=>onSubmitClick(roleSelected)}>
                Update
              </button>
            </div>
          </div>
        </div>
      </div>
      </div>
  );
};

export default ModalUserUpdate;
