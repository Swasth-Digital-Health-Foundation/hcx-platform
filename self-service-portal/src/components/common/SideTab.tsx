import React from "react";

interface ChildProps {
    onDataUpdate: (data: string) => void
  }

const SideBar: React.FC<ChildProps> = ({ onDataUpdate }) => {
    const handleChange = (value:string) => {
      const data = value
      onDataUpdate(data);
    };

    const sideBarNames = ["Profile", "Create User","Participants", "Users"];
    return(
    <aside
    id="logo-sidebar"
    className="fixed top-0 left-0 z-40 w-64 h-screen pt-20 transition-transform -translate-x-full bg-white border-r border-gray-200 sm:translate-x-0 dark:bg-gray-800 dark:border-gray-700 shadow shadow-xl shadow-right"
    aria-label="Sidebar"
  >
    <div className="h-full overflow-y-auto bg-white dark:bg-gray-800 shadow shadow-xl shadow-right">
      <ul className="space-y-2 font-medium">
        {sideBarNames.map((value,index) => { 
            return (
                <li>
                    <a
                    href="#"
                    className="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 ml-4"
                    onClick={(event)=> handleChange(value)}
                >
                    <svg
                    aria-hidden="true"
                    className="w-6 h-6 text-gray-500 transition duration-75 dark:text-gray-400 group-hover:text-gray-900 dark:group-hover:text-white"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                    xmlns="http://www.w3.org/2000/svg"
                    >
                    <path d="M2 10a8 8 0 018-8v8h8a8 8 0 11-16 0z" />
                    <path d="M12 2.252A8.014 8.014 0 0117.748 8H12V2.252z" />
                    </svg>
                    <span className="ml-3">{value}</span>
                </a>
                </li>
            );
        } )}
       </ul>
    </div>
  </aside>
)
};

export default SideBar;