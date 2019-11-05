import React from "react";
import ReactDOM from "react-dom";
import { createAlignedIMU, createWatchfone } from "./rete";

import "./App.css";

export default function App() {
  return (
    <div className="App">
      
      <div
        style={{ width: "100vw", height: "100vh" }}
        ref={ref => ref && createWatchfone(ref)}
      />
    </div>
  );
}


