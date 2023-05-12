import { useRecoilState } from "recoil";

import { authAtom } from "../state/auth";

import * as Api from "../../api/api";
//import { navigate } from "raviger";
import { toast } from "react-toastify";

export function useAuthActions() {
  const [auth, setAuth] = useRecoilState(authAtom);

  return {
    login: login,
    loginApi: temp_login,
    logout,
    currentUser,
  };

  function temp_login(username: string, password: string) {
    setAuth(() => ({
      username: "username",
      token: "access_token",
      isAuthenticated: "true",
    }));
    toast("Logged in successfully", { type: "success" });
  }

  function login(username: string,password: string) {
    return Api.login({ username, password }).then((res: any) => {
      // store user details and jwt token in local storage to keep user logged in between page refreshes
      localStorage.setItem("ACCESS_TOKEN", res.access_token);
      setAuth({
        username: res.username,
        token: res.access_token,
        isAuthenticated: "true",
      });
    });
  }

  function logout() {
    // remove user from local storage, set auth state to null and redirect to login page
    localStorage.removeItem("user");
    setAuth(() => ({
      isAuthenticated: "false",
      token: null,
    }));
    //navigate("/");
  }

  function currentUser() {
    return auth;
  }
}
