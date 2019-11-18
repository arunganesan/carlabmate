import React from "react";
import "./App.css";
import Algorithm from "./InputPhoneNumber/Algorithm";

import { Libcarlab, Information, DataMarshal, Registry } from "./Libcarlab";

type AppState = {
  message: string,
  userid: string,
  test: boolean,
  required_info: Information[],
  outputSensors: string[]
}


class App extends React.Component<{}, AppState> {
  carlab: Libcarlab;

  constructor(props: any) {
    super(props);

    this.state = {
      message: "",
      userid: props.userid === undefined ? 21 : props.userid,
      test: props.test === undefined ? false : props.test,
      required_info: [],
      outputSensors: ["car-model"]
    };

    this.carlab = new Libcarlab(
      this.state.userid,
      this.state.required_info,
    )
  }

  render() {
    return <>
    WHERE IS IT
      <Algorithm libcarlab={this.carlab} />
    </>;
  }
}

// =  = () => {
//   new Libcarlab(
//     this.state.userid,
//     this.state.required_info
//   );

// }

export default App;
