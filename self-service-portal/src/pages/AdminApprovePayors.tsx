import React, { useEffect, useState } from 'react';
import { approvePayorList } from '../api/RegistryService';
import Loader from '../common/Loader';
import AdminPayorApproveList from './AdminPayorApproveList';

const AdminPayorApprove: React.FC  = () => {
    console.log("i am here in approve"); 
    const [payorList, setPayorList] = useState([{"participant_code": "", "participant_name": "", "role": "", "primary_email": "", "primary_mobile": "", "status": ""}]); 
    const [flag, setFlag] = useState(false);
    
    useEffect(() => {
        if(flag == false){
        approvePayorList().then((res:any) => {
            let user = res["data"]["participants"];
            let dataUser: any = [];
            user.map((val: any, index: any) => {
            if(val.identity_verification){
                dataUser.push({"participant_code": val.participant_code, "participant_name": val.participant_name, "role": val.roles[0], "primary_email": val.primary_email, "primary_mobile": val.primary_mobile, "status": val.identity_verification});
            }
            });
            setPayorList(dataUser);
            setFlag(true);
            console.log("payor approve list", res);
        }).catch((err:any) => {
            console.log("error in fetching the list");
        });
    }
    },[]);



    return(
        <>
        { flag ? <AdminPayorApproveList payorList={payorList}></AdminPayorApproveList> : 
        <>
        <Loader></Loader>
        <label className="m-10 p-5 block text-black dark:text-white">
                Fetching Payor Approval List
        </label>        
        </>  }
        </>
    );
}

export default AdminPayorApprove;