import { useRoutes } from "raviger";
import Login from "../components/auth/Login";
import NotFound from "../components/common/NotFound";
import Register from "../components/auth/Register";
import Dashboard from "../components/auth/dashboard";
import CommunicationVerify from "../components/auth/CommunicationVerify";

const routes = {
  "/onboarding/login": () => <Login />,
  "/onboarding/register": () => <Register />,
  "/onboarding/dashboard": () => <Dashboard />,
  "/onboarding/verify": () => <CommunicationVerify />,
  "*": () => <NotFound />
}
export default function PublicRouter() {
  let route = useRoutes(routes);
  return (
    <div>
      <div></div>
      {route}
    </div>
  );
}
