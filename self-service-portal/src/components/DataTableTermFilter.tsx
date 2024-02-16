import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom';
import { DataTable } from 'simple-datatables'
import { addAppData, appDataReducer } from '../reducers/app_data';
import { useDispatch, useSelector } from 'react-redux';
import { useDebounce } from 'use-debounce';
import axios from 'axios';
import { RootState } from '../store';
import _ from 'lodash';

interface TerminologyFilter {
  Code: string;
  Display: string;
  System: string;
}

interface TerminologyFilterProps {
    termList : any
  }

const DataTableTermFilter : React.FC<TerminologyFilterProps> = ({ termList } : TerminologyFilterProps ) => {
  const [terminologyList, setTerminologyList] = useState<TerminologyFilter[]>(termList);  
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  const [text, setText] = useState('');
  const [debouncedText] = useDebounce(text, 500);
  const [flag, setFlag] = useState(true);
  const dispatch = useDispatch();  

  const onClickTerminology = (url:string) => {
    let config = {
      method: 'get',
      maxBodyLength: Infinity,
      url: `https://staging-hcx.swasth.app/hapi-fhir/fhir/ValueSet/$expand?url=${url}&filter=${debouncedText}`,
      headers: { }
    };
    
    axios.request(config)
    .then((response) => {
      let dataUser: any = [];
        response.data.expansion.contains.map((value: any, index: any) => {
          dataUser.push({"Code": value.code, "Display":value.display, "System": value.system})
        });
        setTerminologyList(dataUser);
        dispatch(addAppData({"termSearch":dataUser}));
        dispatch(addAppData({"showTermSearch":true}));
        setFlag(false);
    })
    .catch((error) => {
      let config = {
        method: 'get',
        maxBodyLength: Infinity,
        url: `https://staging-hcx.swasth.app/hapi-fhir/fhir/ValueSet/?url=${url}&filter=${debouncedText}`,
        headers: { }
      };
      
      axios.request(config)
      .then((response) => {
        let dataUser: any = [];
          response.data.entry[0].resource.expansion.contains.map((value: any, index: any) => {
            dataUser.push({"Code": value.code, "Display":value.display, "System": value.system})
          });
          setTerminologyList(dataUser);
          dispatch(addAppData({"termSearch":dataUser}));
          dispatch(addAppData({"showTermSearch":true}));
          setFlag(false);
      })
      .catch((error) => {
        console.log(error);
      });
      console.log(error);
    });
    
  }

  useEffect(() => {
    const source = axios.CancelToken.source();
    if (debouncedText) {
        onClickTerminology(_.get(appData,"termSearchText") || '');
    } else {
        setTerminologyList(termList);
    }
    return () => {
      source.cancel(
        "Canceled because of component unmounted or debounce Text changed"
      );
    };
  }, [debouncedText]);
  
  useEffect(() => {
    console.log("term list", terminologyList);
    if(flag == true){
     let dataTableTwo = new DataTable('#dataTableTwo21', {
      perPageSelect: [5, 10, 15, ['All', -1]],
      searchable:false,
      //data:{headings:["Code","Display","System"], data:terminologyList.map(value => {return [value.Code, value.Display, value.System]})}
    });
    }
  },[flag]);

  useEffect(() => {
    if(flag == false){
        setFlag(true);
    }
  },[flag])

  
  return (<>
    <div className="mt-4 mb-4 grid md:mt-6 md:gap-6 2xl:mt-2.5 2xl:gap-7.5">
    <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
        <div className="border-b border-stroke py-4 px-6.5 dark:border-strokedark flex flex-wrap justify-between">
                <div>
                  <div className="relative">
                    <input
                      type="email"
                      placeholder="Enter the search text"
                      className="w-full rounded-lg border border-stroke bg-transparent py-3 pl-6 pr-10 outline-none focus:border-primary focus-visible:shadow-none dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                      onChange={(event) => {setText(event.target.value)}}
                    />

                    <span className="absolute right-4 top-4">
                    <svg className="fill-body hover:fill-primary dark:fill-bodydark dark:hover:fill-primary" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M9.16666 3.33332C5.945 3.33332 3.33332 5.945 3.33332 9.16666C3.33332 12.3883 5.945 15 9.16666 15C12.3883 15 15 12.3883 15 9.16666C15 5.945 12.3883 3.33332 9.16666 3.33332ZM1.66666 9.16666C1.66666 5.02452 5.02452 1.66666 9.16666 1.66666C13.3088 1.66666 16.6667 5.02452 16.6667 9.16666C16.6667 13.3088 13.3088 16.6667 9.16666 16.6667C5.02452 16.6667 1.66666 13.3088 1.66666 9.16666Z" fill=""></path><path fill-rule="evenodd" clip-rule="evenodd" d="M13.2857 13.2857C13.6112 12.9603 14.1388 12.9603 14.4642 13.2857L18.0892 16.9107C18.4147 17.2362 18.4147 17.7638 18.0892 18.0892C17.7638 18.4147 17.2362 18.4147 16.9107 18.0892L13.2857 14.4642C12.9603 14.1388 12.9603 13.6112 13.2857 13.2857Z" fill=""></path></svg>
                    </span>
                  </div>
                </div>
            <Link
                to="#"
                className="inline-flex items-center justify-center rounded-md border border-primary py-2 px-10 text-center font-medium text-primary hover:bg-opacity-90 lg:px-8 xl:px-10"
                onClick={() => {console.log("i am clicked"); dispatch(addAppData({"showTermSearch":false}));}}
            >
                Back to Terminology
            </Link>
        </div>
    </div>
</div>
    {flag ? 
    <div className="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
      <div className="data-table-common data-table-two max-w-full overflow-x-auto">
        <table className="table w-full table-auto" id="dataTableTwo21">
          <thead>
            <tr>
              <th>
                <div className="flex items-center justify-between gap-1.5">
                  <p>Code</p>
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
                  <p>Display</p>
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
                  <p>System</p>
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
                  <td>{value.Code}</td>
                  <td>{value.Display}</td>
                  <td>{value.System}</td>
                </tr>
              );
            }): null}
          </tbody>
        </table>
      </div>
    </div> : null }
    </> );
};

export default DataTableTermFilter;
