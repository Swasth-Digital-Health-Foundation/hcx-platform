import React from "react";
import {
  BrowserRouter as Router,
  Link,
  useLocation
} from "react-router-dom";


export const useQuery = () =>{
    const { search } = useLocation();
    return React.useMemo(() => new URLSearchParams(search), [search]);
}