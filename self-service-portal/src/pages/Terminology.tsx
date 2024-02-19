import React, { useEffect, useState } from 'react';
import { getPayorList } from '../api/RegistryService';
import axios from 'axios';
import DataTableTerminology from '../components/DataTableTerminology';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../store';
import _ from 'lodash';
import DataTableTermFilter from '../components/DataTableTermFilter';

type Payor = {
    participant_code: string,
    participant_name: string
  }


  interface TerminologyFilter {
    Code: string;
    Display: string;
    System: string;
  }
const Terminology: React.FC = () => {

    const [payors, setPayors] = useState<Payor[]>();
    const [termList, setTermList] = useState([]);
    const dispatch = useDispatch();
    const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);

    useEffect(() => {
        let config = {
        method: 'get',
        maxBodyLength: Infinity,
        url: 'https://staging-hcx.swasth.app/hapi-fhir/fhir/ValueSet',
        headers: { }
        };

        axios.request(config)
        .then((response:any) => {
        console.log(response.data);
        let dataUser: any = [];
        response.data.entry.map((value: any, index: any) => {
          dataUser.push({"Type": value.resource.resourceType, "Name":value.resource.name, "URL": value.resource.url, "Description":value.resource.description, "Publisher": value.resource.publisher, "Action":"action"})
        });
        setTermList(dataUser);
        })
        .catch((error:any) => {
        console.log(error);
        });
    },[])

    useEffect(() => {
        getPayorList().then(res => {
        const participantNames = res.data.participants.map((participant: { participant_name: any; }) => ({ value: participant.participant_name, ...participant }))
        setPayors(participantNames)
        })
    },[])

    function updateCreateUserData(value: string) {
        throw new Error('Function not implemented.');
    }

    return(
        <>
        {_.get(appData,"showTermSearch") == false ?
        <>
        <div className="mt-4 grid md:mt-6 md:gap-6 2xl:mt-7.5 2xl:gap-7.5">
        <div className="flex flex-wrap rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark">
            <div className="w-1/2 flex flex-col gap-5.5 p-6.5">
                <label className="mb-2.5 block font-medium text-black dark:text-white">
                    Select Payor
                </label>
                <div className="relative z-20 bg-transparent dark:bg-form-input">
                    <select className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-3 px-5 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                        onChange={(event) => { updateCreateUserData(event.target.value) }}>
                        <option value="All" selected>All</option>
                        {payors !== undefined ? payors.map((value, index) => {
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
            <div className="w-1/2 flex flex-col gap-5.5 p-6.5">
                <label className="mb-2.5 block font-medium text-black dark:text-white">
                    Select Category
                </label>
                <div className="relative z-20 bg-transparent dark:bg-form-input">
                    <select className="relative z-20 w-full appearance-none rounded border border-stroke bg-transparent py-3 px-5 outline-none transition focus:border-primary active:border-primary dark:border-form-strokedark dark:bg-form-input dark:focus:border-primary"
                        onChange={(event) => { updateCreateUserData(event.target.value) }}>
                        <option value="admin">All</option>
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
    </div>
    {termList.length !== 0 ? <div className="mt-4 grid md:mt-6 md:gap-6 2xl:mt-7.5 2xl:gap-7.5">
    <DataTableTerminology termList={termList}></DataTableTerminology>
    </div> : null } </> : 
    <DataTableTermFilter termList={_.get(appData,"termSearch")}></DataTableTermFilter>}
    </>
    )

}
export default Terminology;