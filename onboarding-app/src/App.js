import './App.css';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import 'react-toastify/dist/ReactToastify.css';
import { Onboarding } from './components/Onboarding';
import { Home } from './components/HomePage';
import { Success } from './components/Success';
import { Onboarded } from './components/Onboarded';
import { PayorSystem } from './components/PayorSystem';
import { UpdateDetails } from './components/UpdateDetails';

function App() {

  const env = process.env.REACT_APP_ENV;

  return (
    <div className="App">
      <Router>
        <Switch>
          <Route path="/participant/update/endpointurl">
            <UpdateDetails />
          </Route>
          {env === 'staging' ?
            <Route path="/onboarding/payorsystem">
              <PayorSystem />
            </Route> : null}
          <Route path="/onboarding/process">
            <Onboarding />
          </Route>
          <Route path="/onboarding/success">
            <Success />
          </Route>
          <Route path="/onboarded">
            <Onboarded />
          </Route>
          <Route path="/onboarding">
            <Home />
          </Route>
        </Switch>
      </Router>
    </div>
  );
}


export default App;
