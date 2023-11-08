import React, { useRef } from 'react'
import { FaqItem } from '../types/faqItem';

const Accordion: React.FC<FaqItem> = ({ active, handleToggle, faq }) => {
    const contentEl = useRef<HTMLDivElement>(null);

    const { header, id, text } = faq;

    return (
        <div className="rounded-md border border-stroke p-4 shadow-9 bg-white dark:border-strokedark dark:shadow-none md:p-6 xl:p-7.5">
            <button
                className={`flex w-full items-center justify-between gap-2 ${active === id ? 'active' : ''
                    }`}
                onClick={() => handleToggle(id)}
            >
                <div>
                    <h4 className="text-left text-title-xsm font-bold text-black dark:text-white sm:text-title-md">
                        {header}
                    </h4>
                </div>
                <div className="flex h-9 w-full max-w-9 items-center justify-center rounded-full border border-primary dark:border-white">
                    <svg
                        className={`fill-primary duration-200 ease-in-out dark:fill-white ${active === id ? 'hidden' : ''
                            }`}
                        width="15"
                        height="15"
                        viewBox="0 0 15 15"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            d="M13.2969 6.51563H8.48438V1.70312C8.48438 1.15625 8.04688 0.773438 7.5 0.773438C6.95313 0.773438 6.57031 1.21094 6.57031 1.75781V6.57031H1.75781C1.21094 6.57031 0.828125 7.00781 0.828125 7.55469C0.828125 8.10156 1.26563 8.48438 1.8125 8.48438H6.625V13.2969C6.625 13.8438 7.0625 14.2266 7.60938 14.2266C8.15625 14.2266 8.53906 13.7891 8.53906 13.2422V8.42969H13.3516C13.8984 8.42969 14.2813 7.99219 14.2813 7.44531C14.2266 6.95312 13.7891 6.51563 13.2969 6.51563Z"
                            fill=""
                        />
                    </svg>

                    <svg
                        className={`fill-primary duration-200 ease-in-out dark:fill-white ${active === id ? 'block' : 'hidden'
                            }`}
                        width="15"
                        height="3"
                        viewBox="0 0 15 3"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            d="M13.503 0.447144C13.446 0.447144 13.503 0.447144 13.503 0.447144H1.49482C0.925718 0.447144 0.527344 0.902427 0.527344 1.47153C0.527344 2.04064 0.982629 2.43901 1.55173 2.43901H13.5599C14.129 2.43901 14.5273 1.98373 14.5273 1.41462C14.4704 0.902427 14.0151 0.447144 13.503 0.447144Z"
                            fill=""
                        />
                    </svg>
                </div>
            </button>

            <div
                ref={contentEl}
                className={`mt-5 duration-200 ease-in-out ${active === id ? 'block' : 'hidden'
                    }`}
            >
                {/* <p className="max-w-[830px] font-medium">{text}</p> */}
                {text}
            </div>
        </div>
    );
};

export default Accordion;
