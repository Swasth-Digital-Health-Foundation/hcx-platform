import axios from 'axios';
import React, { useEffect, useState } from 'react'
import { useDispatch } from 'react-redux';
import { DataTable } from 'simple-datatables'
import { addAppData } from '../reducers/app_data';

interface Terminology {
  Type: string;
  Name: string;
  URL: string;
  Publisher: string;
  Description: string;
  Action: any;
}

interface DTTerminologyProps {
    termList : Terminology[]
  }

const dataTableTerminology = ({ termList } : DTTerminologyProps ) => {
  console.log("term data", termList);
  const dispatch = useDispatch();
  const [terminologyList, setTerminologyList] = useState<Terminology[]>(termList);  
  const [serachList, setSearchList] = useState([]);

  const onClickTerminology = (url:string) => {
    let config = {
      method: 'get',
      maxBodyLength: Infinity,
      url: `https://staging-hcx.swasth.app/hapi-fhir/fhir/ValueSet/$expand?url=${url}`,
      headers: { }
    };
    
    axios.request(config)
    .then((response) => {
      let dataUser: any = [];
        response.data.expansion.contains.map((value: any, index: any) => {
          dataUser.push({"Code": value.code, "Display":value.display, "System": value.system})
        });
        setSearchList(dataUser);
        dispatch(addAppData({"termSearch":dataUser}));
        dispatch(addAppData({"showTermSearch":true}));
        dispatch(addAppData({"termSearchText": url}));
    })
    .catch((error) => {
      let config = {
        method: 'get',
        maxBodyLength: Infinity,
        url: `https://staging-hcx.swasth.app/hapi-fhir/fhir/ValueSet/?url=${url}`,
        headers: { }
      };
      
      axios.request(config)
      .then((response) => {
        let dataUser: any = [];
          response.data.entry[0].resource.expansion.contains.map((value: any, index: any) => {
            dataUser.push({"Code": value.code, "Display":value.display, "System": value.system})
          });
          setSearchList(dataUser);
          dispatch(addAppData({"termSearch":dataUser}));
          dispatch(addAppData({"showTermSearch":true}));
          dispatch(addAppData({"termSearchText": url}));
      })
      .catch((error) => {
        console.log(error);
      });
      console.log(error);
    });
    
  }

  useEffect(() => {
    const dataTableTwo = new DataTable('#dataTableTwo', {
      perPageSelect: [5, 10, 15, ['All', -1]],
    });
    dataTableTwo;
  });

  return (
    <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
      <div className="data-table-common data-table-two max-w-full overflow-x-auto">
        <table className="table w-full table-auto" id="dataTableTwo">
          <thead>
            <tr>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Type</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Name</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>URL</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Publisher</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
              <th data-type="date" data-format="YYYY/DD/MM">
                <div className="flex items-center justify-between gap-1.5">
                  <p>Description</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Action</p>
                  <div className="inline-flex flex-col space-y-[2px]">
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path d="M5 0L0 5H10L5 0Z" fill="" />
                      </svg>
                    </span>
                    <span className="inline-block">
                      <svg
                        className="fill-current"
                        width="10"
                        height="5"
                        viewBox="0 0 10 5"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M5 5L10 0L-4.37114e-07 8.74228e-07L5 5Z"
                          fill=""
                        />
                      </svg>
                    </span>
                  </div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            {terminologyList !== undefined ? terminologyList.map((value, index) => {
              return (
                <tr key={index}>
                  <td>{value.Type}</td>
                  <td>{value.Name}</td>
                  <td>{value.URL}</td>
                  <td>{value.Publisher}</td>
                  <td>{value.Description}</td>
                  <td className="py-5 px-4">
                  <div className="flex items-center space-x-3.5">
                  <button className="hover:text-primary"
                    onClick={(event) => {event.preventDefault(); onClickTerminology(value.URL);}}>
                    <svg
                      className="fill-current"
                      width="18"
                      height="18"
                      viewBox="0 0 18 18"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M8.99981 14.8219C3.43106 14.8219 0.674805 9.50624 0.562305 9.28124C0.47793 9.11249 0.47793 8.88749 0.562305 8.71874C0.674805 8.49374 3.43106 3.20624 8.99981 3.20624C14.5686 3.20624 17.3248 8.49374 17.4373 8.71874C17.5217 8.88749 17.5217 9.11249 17.4373 9.28124C17.3248 9.50624 14.5686 14.8219 8.99981 14.8219ZM1.85605 8.99999C2.4748 10.0406 4.89356 13.5562 8.99981 13.5562C13.1061 13.5562 15.5248 10.0406 16.1436 8.99999C15.5248 7.95936 13.1061 4.44374 8.99981 4.44374C4.89356 4.44374 2.4748 7.95936 1.85605 8.99999Z"
                        fill=""
                      />
                      <path
                        d="M9 11.3906C7.67812 11.3906 6.60938 10.3219 6.60938 9C6.60938 7.67813 7.67812 6.60938 9 6.60938C10.3219 6.60938 11.3906 7.67813 11.3906 9C11.3906 10.3219 10.3219 11.3906 9 11.3906ZM9 7.875C8.38125 7.875 7.875 8.38125 7.875 9C7.875 9.61875 8.38125 10.125 9 10.125C9.61875 10.125 10.125 9.61875 10.125 9C10.125 8.38125 9.61875 7.875 9 7.875Z"
                        fill=""
                      />
                    </svg>
                  </button>
                  </div>
                  </td>
                </tr>
              );
            }): null}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default dataTableTerminology;
