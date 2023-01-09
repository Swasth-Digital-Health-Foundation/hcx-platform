import './App.css';
import { OnBoarding } from './components/OnBoarding';
import { OTPVerify } from './components/OTPVerify';
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
          <Route path="/basic_info">
            <OnBoarding />
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
