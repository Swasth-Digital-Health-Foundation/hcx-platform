import './App.css';
import { OnBoardingStaging } from './components/OnBoardingStaging';
import { OTPVerify } from './components/OTPVerify';
import { OnBoardingPoc } from './components/OnBoardingPoc';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import 'react-toastify/dist/ReactToastify.css';
import { OTPRegenerate } from './components/OTPRegenerate';
import { UpdateRegistry } from './components/UpdateRegistry';

function App() {
  return (
    <div className="App">
      <Router>
        <Switch>
          <Route path="/onboarding_poc">
            <OnBoardingPoc />
          </Route>
          <Route path="/onboarding_staging">
            <OnBoardingStaging />
          </Route>
          <Route path="/otp_verify">
            <OTPVerify />
          </Route>
          <Route path="/otp_regenerate">
            <OTPRegenerate />
          </Route>
          <Route path="/update_registry">
            <UpdateRegistry />
          </Route>
        </Switch>
      </Router>
    </div>
  );
}

export default App;


