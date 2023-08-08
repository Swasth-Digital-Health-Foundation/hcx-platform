import { useEffect, useState } from "react";
import { useAuthActions } from "../../recoil/actions/auth.actions";
import logo from "../../swasth_logo_1.jpg";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../store";
import _ from 'lodash';
import { toast } from "react-toastify";
import { post } from "../../api/APIService";
import { addParticipantToken } from "../../reducers/token_reducer";
import { getParticipant } from "../../api/RegistryService";
import { addParticipantDetails } from "../../reducers/participant_details_reducer";
import 'flowbite';
import { navigate } from "raviger";
import Sidebar from "../common/SideTab";
import ParticipantInfo from "../common/ParticipantInfo";
import LinkedParticipant from "../common/LinkedParticipants";
import { constSelector } from "recoil";
import CreateUser from "../common/CreateUser";
import TermsOfUse from "../common/TermsOfUse";
import Users from "../common/Users";
import LaunchPayorApp from "../common/LaunchPayorApp";
import Header from "../common/Header";
import { addAppData } from "../../reducers/app_data";

export default function Dashboard() {

  const dispatch = useDispatch()
  const participantDetails : Object = useSelector((state: RootState) => state.participantDetailsReducer.participantDetails);
  const authToken = useSelector((state: RootState) => state.tokenReducer.participantToken);  
  const appData: Object = useSelector((state: RootState) => state.appDataReducer.appData);
  const appLink = process.env.REACT_APP_DASHBOARD;
  
  const [selectedSideTab, setSelectedSideTab] = useState('Profile');
  console.log("dashboard reloaded")
  

  const showComponent = (tabSelected:any) => {
      console.log("tabSelected", tabSelected);
      if(tabSelected == "Profile"){
        console.log("selectedSideTab", selectedSideTab);
         return (<ParticipantInfo></ParticipantInfo>) 
      }else if(tabSelected == "Manage Participants"){
        return (<LinkedParticipant></LinkedParticipant>)
      }else if(tabSelected == "Invite User"){
        return (<CreateUser></CreateUser>)
      }else if(tabSelected == "Manage Users"){
        return(<Users></Users>)
      }else if(tabSelected == "Launch Payor App"){
        return(<LaunchPayorApp></LaunchPayorApp>)
      }else if(tabSelected = "Launch Dashboard"){
        window.open(`${appLink}?jwt_token=${authToken}`);
        dispatch(addAppData({"sidebar":"Profile"}))
        return (<ParticipantInfo></ParticipantInfo>) 
      }
  }

  return (
<>
    <Header></Header>
    <Sidebar></Sidebar>
  {showComponent( _.get(appData,"sidebar") )}
</>
  );
}
