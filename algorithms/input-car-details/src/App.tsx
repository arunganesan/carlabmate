import React from 'react';
import './App.css';
import Algorithm from './InputCarModel/Algorithm'

import { Libcarlab, DataMarshal, Registry } from "./LibcarlabReact";


class App extends React.Component {
  constructor(props) {
    this.state = {
      message: "",
      userid: props.userid === undefined ? 21 : props.userid,
      test: props.test === undefined ? false : props.test,
      required_info: [],
      outputSensors: ["car-model"]
    };
  }

  render() {
    return <Algorithm /> 
  }
}

// =  = () => {
//   new Libcarlab(
//     this.state.userid,
//     this.state.required_info
//   );

  
// }

export default App;
