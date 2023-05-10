import React from "react";
import { ToastContainer } from "react-toastify";
import { useRecoilState } from "recoil";
import { authAtom } from "./recoil/state/auth";
import PublicRouter from "./router/PublicRouter";

function App() {
  const [auth] = useRecoilState(authAtom);
  return (
    <div className="App">
      <ToastContainer
        position="top-right"
        autoClose={5000}
        hideProgressBar
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="dark"
      />
      <PublicRouter />
    </div>
  );
}

export default App;
