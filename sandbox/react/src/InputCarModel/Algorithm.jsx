import React from "react";
import { Button, Container, Form } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.css";

import { Libcarlab, DataMarshal, Registry } from "../LibcarlabReact";

const style = {
  input: {
    marginTop: "25px"
  },
  button: {
    marginTop: "25px"
  }
};

const carmodels = [
  "Abarth",
  "Alfa Romeo",
  "Aston Martin",
  "Audi",
  "Bentley",
  "BMW",
  "Bugatti",
  "Cadillac",
  "Chevrolet",
  "Chrysler",
  "Citroën",
  "Dacia",
  "Daewoo",
  "Daihatsu",
  "Dodge",
  "Donkervoort",
  "DS",
  "Ferrari",
  "Fiat",
  "Fisker",
  "Ford",
  "Honda",
  "Hummer",
  "Hyundai",
  "Infiniti",
  "Iveco",
  "Jaguar",
  "Jeep",
  "Kia",
  "KTM",
  "Lada",
  "Lamborghini",
  "Lancia",
  "Land Rover",
  "Landwind",
  "Lexus",
  "Lotus",
  "Maserati",
  "Maybach",
  "Mazda",
  "McLaren",
  "Mercedes-Benz",
  "MG",
  "Mini",
  "Mitsubishi",
  "Morgan",
  "Nissan",
  "Opel",
  "Peugeot",
  "Porsche",
  "Renault",
  "Rolls-Royce",
  "Rover",
  "Saab",
  "Seat",
  "Skoda",
  "Smart",
  "SsangYong",
  "Subaru",
  "Suzuki",
  "Tesla",
  "Toyota",
  "Volkswagen",
  "Volvo"
];

class InputCarModelBase extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      message: "",
      test: props.test === undefined ? false : props.test,
      required_info: [],
      outputSensors: ["car-model"]
    };

    this.libcarlab = props.libcarlab;
  }

  componentDidMount() {
    this.libcarlab.checkNewInfo((info, data) => {
      console.log("Got info ", info, "with data", data);
    });
  }

  submitData() {
    console.log('Sending ', this.state.message);
    this.libcarlab.outputNewInfo(
      new DataMarshal(
        Registry.CarModel,
        this.state.message), 
      res => {
        console.log("Response msessage ", res);
      });
  }

  componentWillUnmount() {
    // this.libcarlab.unscheduleUploads();
  }

  render() {
    return null;
  }
}

export default class InputCarModel extends InputCarModelBase {
  render() {
    return (
      <Container>
        <Form.Label style={style.input}>Select your car model</Form.Label>

        <Form.Control
          // style={style.input}
          aria-label="large"
          aria-describedby="inputGroup-sizing-lg"
          value={this.state.message}
          onChange={evt => this.setState({ message: evt.target.value })}
          as="select"
        >
          {carmodels.map(elt => (
            <option key={elt}>{elt}</option>
          ))}
        </Form.Control>

        <Button
          onClick={() => this.submitData()}
          style={style.button}
          size="lg"
        >
          Submit
        </Button>
      </Container>
    );
  }
}
