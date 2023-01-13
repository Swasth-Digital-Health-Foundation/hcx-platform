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

import { useMultipleForm } from "usetheform";
import { useState } from 'react'
import TabExampleVerticalTabular from './components/TabExample';


// import { Tab } from 'semantic-ui-react'

// const panes = [
//   { menuItem: 'Tab 1', render: () => <Tab.Pane><OnBoarding/></Tab.Pane> },
//   { menuItem: 'Tab 2', render: () => <Tab.Pane>Tab 2 Content</Tab.Pane> },
//   { menuItem: 'Tab 3', render: () => <Tab.Pane>Tab 3 Content</Tab.Pane> },
// ]

// function App() {
//   return (
//   <Tab menu={{ fluid: true, vertical: true, tabular: true }} panes={panes} />
//   );
//   }


// function App() {
//   const [currentPage, setPage] = useState(1);
//   const nextPage = () => setPage((prev) => ++prev);
//   const prevPage = () => setPage((prev) => --prev);

//   const [getWizardState, wizard] = useMultipleForm();
//   const onSubmitWizard = () => console.log(getWizardState());

//   return (
//    <div className="App">
//      {currentPage === 1 && (
//       //  <WizardFormFirstPage {...wizard} onSubmit={nextPage} />
//       <OnBoarding/>
//      )}
//      {currentPage === 2 && (
//        <OTPVerify
//          prevPage={prevPage}
//          onSubmit={onSubmitWizard}
//        />
//      )}
//    </div>
//   );
// }



function App() {
  return (
    <div className="App">
      <Router>
        <Switch>
          <Route path="/basic_info">
            <TabExampleVerticalTabular />
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
