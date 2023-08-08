
import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../../store';

interface ChildProps {
    onChildClickYes: (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => void;
    onChildClickNo: (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => void;
    text: string;
}

const ModalYesNo: React.FC<ChildProps> = ({ onChildClickYes,  onChildClickNo ,text }) => {

    const dispatch = useDispatch();
    const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);

    useEffect(() => {
        console.log("reloaded app data", appData);
    }, [appData])


    function clickYes(event: React.MouseEvent<HTMLButtonElement, MouseEvent>) {
        console.log("I am here in yes");
        return onChildClickYes(event);
    }

    function clickNo(event: React.MouseEvent<HTMLButtonElement, MouseEvent>) {
        console.log("I am here in yes");
        return onChildClickNo(event);
    }

    return (
        <>
            <div
                id="popup-modal"
                tabIndex={-1}
                className="fixed top-0 left-0 right-0 z-50 p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full justify-center items-center flex"
                role="dialog"
            >
                <div className="relative w-full max-w-md max-h-full ">
                    <div className="relative bg-white rounded-lg shadow dark:bg-gray-700">
                        <button
                            type="button"
                            className="absolute top-3 right-2.5 text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm w-8 h-8 ml-auto inline-flex justify-center items-center dark:hover:bg-gray-600 dark:hover:text-white"
                            data-modal-hide="popup-modal"
                        >
                            <svg
                                className="w-3 h-3"
                                aria-hidden="true"
                                xmlns="http://www.w3.org/2000/svg"
                                fill="none"
                                viewBox="0 0 14 14"
                            >
                                <path
                                    stroke="currentColor"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6"
                                />
                            </svg>
                            <span className="sr-only">Close modal</span>
                        </button>
                        <div className="p-6 text-center">
                            <svg
                                className="mx-auto mb-4 text-gray-400 w-12 h-12 dark:text-gray-200"
                                aria-hidden="true"
                                xmlns="http://www.w3.org/2000/svg"
                                fill="none"
                                viewBox="0 0 20 20"
                            >
                                <path
                                    stroke="currentColor"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="M10 11V6m0 8h.01M19 10a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"
                                />
                            </svg>
                            <h3 className="mb-5 text-lg font-normal text-gray-500 dark:text-gray-400">
                                {text}
                            </h3>
                            <button
                                data-modal-hide="popup-modal"
                                type="button"
                                className="button-secondary"
                                onClick={(event) => clickYes(event)}
                            >
                                Submit
                            </button>
                            <button
                                data-modal-hide="popup-modal"
                                type="button"
                                className="button-secondary"
                                onClick={(event) => clickNo(event)}
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <div className={"fixed inset-0 bg-black bg-opacity-30 z-40 "} ></div>
        </>
    )
}

export default ModalYesNo;