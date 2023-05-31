import { atom } from "recoil";

const getLocalToken = () => {
  const token = localStorage.getItem("ACCESS_KEY");
  if (token) {
    return JSON.parse(token);
  }
  return null;
};

type UnauthenticatedState = {
  isAuthenticated: "false";
  token: null;
};

type LoadingState = {
  isAuthenticated: "loading";
  token: string;
};

type AuthenticatedState = {
  isAuthenticated: "true";
  token: string;
  username: string;
};

type AuthState = UnauthenticatedState | LoadingState | AuthenticatedState;

const authAtom = atom<AuthState>({
  key: "auth",
  // get initial state from local storage to enable user to stay logged in
  default: {
    isAuthenticated: getLocalToken() ? "loading" : "false",
    token: getLocalToken(),
  },
});

export { authAtom };
