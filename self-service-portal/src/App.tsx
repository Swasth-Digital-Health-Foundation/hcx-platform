import { lazy, useEffect, useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';

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
import AdminPayorApprove from './pages/AdminApprovePayors';
import NotFound from './components/NotFound';

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
      <Route path="/" element={<Navigate to="/login" />} />
        <Route 
          path="/login" 
          element={<SignIn/>}
        >  
        </Route>
        <Route 
          path="/register" 
          element={<SignUp/>}
        >    
        </Route>
        <Route 
          path="/resetpassword" 
          element={<ResetPassword/>}
        >    
        </Route>
        <Route 
          path="/verify" 
          element={<CommunicationVerify/>}
        >
        </Route>
        <Route 
          path="/user/invite" 
          element={<UserVerify/>}
        >
        </Route>
        <Route element={<DefaultLayout />}>
          <Route
            path="/users"
            element={
              <>
                <PageTitle title="Users" />
                <UserList />
              </>
            }
          />
          <Route
            path="/users/invite"
            element={
              <>
                <PageTitle title="Users" />
                <UsersInvite />
              </>
            }
          />
           <Route
            path="/participants"
            element={
              <>
                <PageTitle title="Participants" />
                <ParticipantList/>
              </>
            }
          />
          <Route
            path="/profile"
            element={
              <>
                <PageTitle title="Swasth HCX Self Service Portal" />
                <ProfilePage />
              </>
            }
          />
          <Route
            path="/terminology"
            element={
              <>
                <PageTitle title="Terminology" />
                <Terminology />
              </>
            }
          />
          <Route
            path="/adminconsole"
            element={
              <>
                <PageTitle title="Admin Console" />
                <AdminPayorApprove/>
              </>
            }
          />
        </Route>
        <Route path="*" element={<NotFound />} /> {/* Wildcard route */}
      </Routes>
    </>
  );
}

export default App;

