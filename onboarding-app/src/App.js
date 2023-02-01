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
        </Switch>
      </Router>
    </div>
  );
}


export default App;
