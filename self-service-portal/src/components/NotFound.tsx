import React from "react";
import { Link } from "react-router-dom";

const NotFound = () => {
  return (
    <div style={{ textAlign: "center", padding: "50px" ,color:"black" }}>
      <h1><strong>404 - Page Not Found</strong></h1>
      <p>Sorry, the page you are looking for does not exist.</p>
      <Link to="/home">Go to Home</Link>
    </div>
  );
};

export default NotFound;
