import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Button } from 'react-bootstrap'

import routes from './routes'
import useRoutes from './hooks/routesHandler'
import { Link, useLocation } from 'react-router-dom';
import Nav from './components/navbar/nav'
function App() {
  const route = useRoutes(routes);
  const location = useLocation();

  return (
    <>
      {console.log("location",location.pathname)}
      {location.pathname !== "/mapchart" && <Nav></Nav>}
      <div className="App">
        {location.pathname === '/'  && <header className="App-header">
          <p>
            <Link to='/root'>
              <Button variant="outline-primary">Check Eligibility</Button>
            </Link>
          </p>
        </header>}
      </div>
      {route}

    </>
  );
}

export default App;
