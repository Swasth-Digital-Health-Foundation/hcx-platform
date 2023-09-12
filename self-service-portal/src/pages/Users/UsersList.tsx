import { Link } from "react-router-dom";
import DataTableUsers from "../../components/DataTableUsers";

const UserList = () => {
    return (
        <>
            <div className="mt-4 mb-4 grid md:mt-6 md:gap-6 2xl:mt-2.5 2xl:gap-7.5">
                <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
                    <div className="border-b border-stroke py-4 px-6.5 dark:border-strokedark flex flex-wrap justify-between">
                        <h3 className="font-medium text-black dark:text-white py-2">
                            INVITE USERS TO MANAGE THE PARTICIPANTS
                        </h3>
                        <Link
                            to="/onboarding/users/invite"
                            className="inline-flex items-center justify-center rounded-md border border-primary py-2 px-10 text-center font-medium text-primary hover:bg-opacity-90 lg:px-8 xl:px-10"
                        >
                            Invite Users
                        </Link>
                    </div>
                </div>
            </div>
            <DataTableUsers></DataTableUsers>
        </>
    );
}

export default UserList;