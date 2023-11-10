import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter as Router } from 'react-router-dom';
import App from './App';
import './index.css';
import './satoshi.css';
import './simple-datatables.css';
import { Provider } from 'react-redux';
import { store } from './store';
import { ToastContainer } from 'react-toastify';
import "react-toastify/dist/ReactToastify.css";

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <Provider store={store}>
    <ToastContainer></ToastContainer>
    <Router> 
      <App />
    </Router>
    </Provider>
  </React.StrictMode>
);
