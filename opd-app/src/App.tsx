import { Navigate, Route, Routes } from 'react-router-dom';
import DefaultLayout from './layout/DefaultLayout';
import { useEffect, useState } from 'react';
import Loader from './common/Loader';
import Home from './pages/Home/Home';
import InitiateNewClaimRequest from './pages/InitiateNewClaimRequest/InitiateNewClaimRequest';
import ViewClaimRequestDetails from './pages/ViewClaimRequestDetails/ViewClaimRequestDetails';
import PreAuthRequest from './pages/InitiatePreAuthRequest/PreAuthRequest';
import RequestSuccess from './components/RequestSuccess';
import { ToastContainer } from 'react-toastify';
import AddPatientAndInitiateCoverageEligibility from './pages/AddPatientAndInitiateCoverageEligibility/AddPatientAndInitiateCoverageEligibility';
import Login from './pages/Authentication/Login';
import AddConsultation from './pages/AddConsultation/AddConsultation';
import ViewPatientDetails from './pages/ViewPatientDetails/ViewPatientDetails';

const App = () => {
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    setTimeout(() => setLoading(false), 1000);
  }, []);

  return loading ? (
    <Loader />
  ) : (
    <>
      <ToastContainer
        position="top-right"
        autoClose={1500}
        hideProgressBar
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
      />
      <Routes>
        <Route path="/" element={<Login />}></Route>
        {/* <Route path="/signup" element={<SignUp />}></Route> */}
        {/* <Route path="/verify-otp" element={<VerifyOTP />}></Route> */}
        <Route element={<DefaultLayout />}>
          <Route path="/home" element={<Home />} />
          <Route
            path="/add-patient"
            element={<AddPatientAndInitiateCoverageEligibility />}
          />
          <Route
            path="/initiate-claim-request"
            element={<InitiateNewClaimRequest />}
          />
          <Route path="/add-consultation" element={<AddConsultation />} />
          {/* <Route
            path="/coverage-eligibility-request"
            element={<CoverageEligibilityRequest />}
          /> */}
          <Route
            path="/coverage-eligibility"
            element={<ViewPatientDetails />}
          />
          <Route
            path="/initiate-claim-request"
            element={<InitiateNewClaimRequest />}
          />
          <Route
            path="/initiate-preauth-request"
            element={<PreAuthRequest />}
          />
          {/* <Route
            path="/view-active-request"
            element={<ViewClaimRequestDetails />}
          /> */}
          <Route path="/request-success" element={<RequestSuccess />} />
          {/* <Route path="/profile" element={<Profile />} /> */}
          {/* <Route path="/bank-details" element={<SendBankDetails />} /> */}
          {/* <Route path="/success" element={<Success />} /> */}
        </Route>
      </Routes>
    </>
  );
};

export default App;
