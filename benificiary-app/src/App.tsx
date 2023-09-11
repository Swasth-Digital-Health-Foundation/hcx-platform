import { Route, Routes } from 'react-router-dom';
import OTP from './pages/Authentication/OTP';
import VerifyOTP from './pages/Authentication/VerifyOTP';
import DefaultLayout from './layout/DefaultLayout';
import { useEffect, useState } from 'react';
import Loader from './common/Loader';
import Home from './pages/Home/Home';
import NewClaim from './pages/NewClaimCycle/NewClaim';
import SignUp from './pages/Authentication/SignUp';
import CoverageEligibility from './pages/CoverageEligibility/CoverageEligibility';
import CreateClaimRequest from './pages/CreateClaimRequest/CreateClaimRequest';
import ViewClaimRequestDetails from './pages/ViewClaimRequestDetails/ViewClaimRequestDetails';
import CoverageEligibilityRequest from './pages/CoverageEligibilityRequest/CoverageEligibilityRequest';

const App = () => {
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    setTimeout(() => setLoading(false), 1000);
  }, []);

  return loading ? (
    <Loader />
  ) : (
    <>
      <Routes>
        <Route path="/otp" element={<OTP />}></Route>
        <Route path="/signup" element={<SignUp />}></Route>
        <Route path="/verify-otp" element={<VerifyOTP />}></Route>
        <Route element={<DefaultLayout />}>
          <Route path="/home" element={<Home />} />
          <Route path="/new-claim" element={<NewClaim />} />
          <Route
            path="/coverage-eligibility-request"
            element={<CoverageEligibilityRequest />}
          />
          <Route
            path="/coverage-eligibility"
            element={<CoverageEligibility />}
          />
          <Route
            path="/create-claim-request"
            element={<CreateClaimRequest />}
          />
          <Route
            path="/view-claim-request"
            element={<ViewClaimRequestDetails />}
          />
        </Route>
      </Routes>
    </>
  );
};

export default App;
