import { lazy, useEffect, useState } from 'react';
import { Route, Routes } from 'react-router-dom';

import SignIn from './pages/Authentication/SignIn';
import SignUp from './pages/Authentication/SignUp';
import Loader from './common/Loader';
import PageTitle from './components/PageTitle';
import ProfilePage from './pages/Dashboard/ProfilePage';
import CommunicationVerify from './components/CommunicationVerify';
import UserVerify from './components/UserVerify';
import UserList from './pages/Users/UsersList';
import ParticipantList from './pages/Participants/ParticipantsList';
import UsersInvite from './pages/Users/UsersInvite';
import ResetPassword from './pages/Authentication/ResetPassword';
import Terminology from './pages/Terminology';

const DefaultLayout = lazy(() => import('./layout/DefaultLayout'));


function App() {
  const [loading, setLoading] = useState<boolean>(true);
  console.log("i started");

  useEffect(() => {
    setTimeout(() => setLoading(false), 1000);
  }, []);

  return loading ? (
    <Loader />
  ) : (
    <>
      <Routes>
        <Route 
          path="/onboarding/login" 
          element={<SignIn/>}
        >  
        </Route>
        <Route 
          path="/onboarding/register" 
          element={<SignUp/>}
        >    
        </Route>
        <Route 
          path="/onboarding/resetpassword" 
          element={<ResetPassword/>}
        >    
        </Route>
        <Route 
          path="/onboarding/verify" 
          element={<CommunicationVerify/>}
        >
        </Route>
        <Route 
          path="/onboarding/user/invite" 
          element={<UserVerify/>}
        >
        </Route>
        <Route element={<DefaultLayout />}>
          <Route
            path="/onboarding/users"
            element={
              <>
                <PageTitle title="Users" />
                <UserList />
              </>
            }
          />
          <Route
            path="/onboarding/users/invite"
            element={
              <>
                <PageTitle title="Users" />
                <UsersInvite />
              </>
            }
          />
           <Route
            path="/onboarding/participants"
            element={
              <>
                <PageTitle title="Participants" />
                <ParticipantList/>
              </>
            }
          />
          <Route
            path="/onboarding/profile"
            element={
              <>
                <PageTitle title="Swasth HCX Self Service Portal" />
                <ProfilePage />
              </>
            }
          />
          <Route
            path="/onboarding/terminology"
            element={
              <>
                <PageTitle title="Terminology" />
                <Terminology />
              </>
            }
          />
        </Route>
        
      </Routes>
    </>
  );
}

export default App;
