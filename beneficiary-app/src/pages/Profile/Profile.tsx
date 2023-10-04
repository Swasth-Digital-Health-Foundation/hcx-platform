import React, { useEffect, useState } from 'react';
import { postRequest } from '../../services/registryService';

const Profile = () => {
  const getMobileFromLocalStorage: any = localStorage.getItem('mobile');

  const [userInfo, setUserInformation] = useState<any>([]);

  const filter = {
    entityType: ['Beneficiary'],
    filters: {
      mobile: { eq: getMobileFromLocalStorage },
    },
  };

  useEffect(() => {
    const search = async () => {
      try {
        const searchUser = await postRequest('/search', filter);
        setUserInformation(searchUser.data);
      } catch (error) {
        console.log(error);
      }
    };
    search();
  }, []);

  const userProfileCard = [
    {
      key: 'Name :',
      value: userInfo[0]?.name,
    },
    {
      key: 'Mobile :',
      value: userInfo[0]?.mobile,
    },
    {
      key: 'Email address :',
      value: userInfo[0]?.email,
    },
  ];

  // const insuranceDetails = [
  //   {
  //     key: 'Insurance ID :',
  //     value: userInfo[0]?.payor_details.map((ele: any) => {
  //       return ele.insurance_id;
  //     }),
  //   },
  //   {
  //     key: 'Payor :',
  //     value: userInfo[0]?.payor_details.map((ele: any) => {
  //       return ele.payor;
  //     }),
  //   },
  // ];
  console.log(userInfo);

  return (
    <>
      <h2 className="text-bold mb-3 text-2xl font-bold text-black dark:text-white">
        User profile
      </h2>
      <div className="rounded-sm border border-stroke bg-white p-2 px-3 shadow-default dark:border-strokedark dark:bg-boxdark">
        {userProfileCard.map((ele: any) => {
          return (
            <div className="mb-2 flex gap-2">
              <h2 className="text-bold text-base font-bold text-black dark:text-white">
                {ele.key}
              </h2>
              <span className="text-base font-medium">{ele.value}</span>
            </div>
          );
        })}
        <h2 className="text-bold -mb-2 text-2xl font-medium text-black dark:text-white">
          Insurance details :
        </h2>
        {userInfo[0]?.payor_details.map((ele: any) => {
          return (
            <div className="mt-5 rounded-sm border border-stroke bg-white p-2 shadow-default dark:border-strokedark dark:bg-boxdark">
              <div className="mb-2">
                <div className="flex gap-2">
                  <h2 className="text-bold text-base font-bold text-black dark:text-white">
                    Insurance ID :
                  </h2>
                  <span className="text-base font-medium">
                    {ele?.insurance_id}
                  </span>
                </div>
                <div className="flex gap-2">
                  <h2 className="text-bold text-base font-bold text-black dark:text-white">
                    Payor :
                  </h2>
                  <span className="text-base font-medium">{ele?.payor}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
};

export default Profile;
