import { Navigate, Route, Routes } from 'react-router-dom';
import OTP from './pages/Authentication/OTP';
import VerifyOTP from './pages/Authentication/VerifyOTP';
import DefaultLayout from './layout/DefaultLayout';
import { useEffect, useState } from 'react';
import Loader from './common/Loader';
import Home from './pages/Home/Home';
import NewClaim from './pages/NewClaimCycle/NewClaim';
import SignUp from './pages/Authentication/SignUp';
import CoverageEligibility from './pages/ViewCoverageEligibilityDetails/CoverageEligibility';
import InitiateNewClaimRequest from './pages/InitiateNewClaimRequest/InitiateNewClaimRequest';
import ViewClaimRequestDetails from './pages/ViewClaimRequestDetails/ViewClaimRequestDetails';
import CoverageEligibilityRequest from './pages/CoverageEligibilityRequest/CoverageEligibilityRequest';
import PreAuthRequest from './pages/InitiatePreAuthRequest/PreAuthRequest';
import RequestSuccess from './components/RequestSuccess';
import { ToastContainer } from 'react-toastify';

const App = () => {
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    setTimeout(() => setLoading(false), 1000);
  }, []);

  return loading ? (
    <Loader />
  ) : (
    <>
        {/* <ToastContainer
          position="top-right"
          autoClose={5000}
          hideProgressBar
          newestOnTop={false}
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
        /> */}
      <Routes>
        <Route path="/" element={<Navigate to="/otp" />} />
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
            path="/initiate-claim-request"
            element={<InitiateNewClaimRequest />}
          />
          <Route
            path="/initiate-preauth-request"
            element={<PreAuthRequest />}
          />
          <Route
            path="/view-active-request"
            element={<ViewClaimRequestDetails />}
          />
          <Route path="/request-success" element={<RequestSuccess />} />
        </Route>
      </Routes>
    </>
  );
};

export default App;
