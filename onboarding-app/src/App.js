import './App.css';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import 'react-toastify/dist/ReactToastify.css';
import { Onboarding } from './components/Onboarding';
import {Home}  from './components/HomePage';
import { End } from './components/EndPage';
import { Onboarded } from './components/Onboarded';
import { PayorSystem } from './components/PayorSystem'; 
import { SetPassword } from './components/SetPassword';

function App() {

  return (
    <div className="App">
      <Router>
        <Switch>
          <Route path="/onboarding/process">
            <Onboarding />
          </Route>
          <Route path="/onboarding/end">
            <End />
          </Route>
          <Route path="/onboarded">
            <Onboarded />
          </Route>
          <Route path="/onboarding">
            <Home />
          </Route>
          <Route path="/payorsystem">
            <PayorSystem />
          </Route>
          <Route path="/password">
            <SetPassword />
          </Route>
        </Switch>
      </Router>
    </div>
  );
}


export default App;
