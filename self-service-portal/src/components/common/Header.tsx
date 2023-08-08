import React from "react";
import { addAppData } from "../../reducers/app_data";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../store";
import _ from "lodash";
import logo from "../../swasth_logo_1.jpg";
import { navigate } from "raviger";

const Header = () => {

    const dispatch = useDispatch();
    const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);


    return (
        <nav className="fixed top-0 z-50 w-full bg-white border-b border-gray-200 dark:bg-gray-800 dark:border-gray-700 gradient-blue-purple">
            <div className="px-3 py-3 lg:px-5 lg:pl-3">
                <div className="flex items-center justify-between">
                    <div className="flex items-center justify-start">
                        <button
                            data-drawer-target="logo-sidebar"
                            data-drawer-toggle="logo-sidebar"
                            aria-controls="logo-sidebar"
                            type="button"
                            className="inline-flex items-center p-2 text-sm text-gray-500 rounded-lg sm:hidden hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:text-gray-400 dark:hover:bg-gray-700 dark:focus:ring-gray-600"
                        >
                            <span className="sr-only">Open sidebar</span>
                            <svg
                                className="w-6 h-6"
                                aria-hidden="true"
                                fill="currentColor"
                                viewBox="0 0 20 20"
                                xmlns="http://www.w3.org/2000/svg"
                            >
                                <path
                                    clipRule="evenodd"
                                    fillRule="evenodd"
                                    d="M2 4.75A.75.75 0 012.75 4h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 4.75zm0 10.5a.75.75 0 01.75-.75h7.5a.75.75 0 010 1.5h-7.5a.75.75 0 01-.75-.75zM2 10a.75.75 0 01.75-.75h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 10z"
                                />
                            </svg>
                        </button>
                        <a href="https://docs.hcxprotocol.io/" target="_blank" className="flex ml-2 md:mr-24">
                            <img
                                src={logo}
                                className="h-8 mr-3"
                                alt="Swasth"
                            />
                            <span className="self-center text-xl font-semibold sm:text-2xl whitespace-nowrap text-white ">
                                Swasth HCX
                            </span>
                        </a>
                    </div>
                    <div className="flex items-center">
                        <p className="text-white">{`Welcome ${_.get(appData, "username")}`} </p>
                        <div className="flex items-center ml-3">
                            <button className="flex items-center gap-2 px-4 py-2 text-white rounded-md focus:outline-none"
                                onClick={() => navigate("/onboarding/login")}>
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" className="w-6 h-6">
                                    <circle cx="12" cy="12" r="11" className="text-white stroke-current" fill="transparent" />
                                    <path d="M14 5l7 7m0 0l-7 7m7-7H3" className="text-white" />
                                </svg>
                            </button>

                        </div>
                    </div>
                </div>
            </div>
        </nav>
    )
}

export default Header;