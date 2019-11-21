import * as React from "react";
import { Button, Container, Form } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.css";

const style = {
  input: {
    marginTop: "25px"
  },
  button: {
    marginTop: "25px"
  }
};



type acceptFuelLevelProps = { 
  produce: Function
};

type acceptFuelLevelState = {
  fuelLevel: string
}




export class acceptFuelLevel extends React.Component<acceptFuelLevelProps, acceptFuelLevelState> {

  constructor(props: acceptFuelLevelProps) {
    super(props);

    this.state = {
      // This value changes per thing
      fuelLevel: '',
    };
  }


  render() {
    const { produce } = this.props;


    return (
      <Container>
        <Form.Label style={style.input}>Enter fuel level</Form.Label>
          
          
        <Form.Control
            type="text"
            value={this.state.fuelLevel}
            onChange={(evt: React.ChangeEvent<HTMLInputElement>) =>
              this.setState({
                fuelLevel: evt.target.value
              })
            }
          />
       
       
        <Button
          onClick={() => produce(this.state.fuelLevel)}
          style={style.button}
          size="lg"
        >
          Submit
        </Button>
      </Container>
    );
  }
}



type acceptPhoneNumberProps = { 
  produce: Function
};

type acceptPhoneNumberState = {
  phoneNumber: string
}


export class acceptPhoneNumber extends React.Component<acceptPhoneNumberProps, acceptPhoneNumberState> {

  constructor(props: acceptPhoneNumberProps) {
    super(props);

    this.state = {
      // This value changes per thing
      phoneNumber: '',
    };
  }

  render() {
    const { produce } = this.props;


    return (
      <Container>
        <Form.Label style={style.input}>Enter phone number</Form.Label>
          
          
        <Form.Control
            type="text"
            value={this.state.phoneNumber}
            onChange={(evt: React.ChangeEvent<HTMLInputElement>) =>
              this.setState({
                phoneNumber: evt.target.value
              })
            }
          />
       
       
        <Button
          onClick={() => produce(this.state.phoneNumber)}
          style={style.button}
          size="lg"
        >
          Submit
        </Button>
      </Container>
    );
  }
}


// export class acceptCarModel extends React.Component<Props> {
//   render() {
//     const { text } = this.props;

//     return <div style={{ color: "blue" }}>Hello {text}</div>;
//   }
// }


// const carmodels = [
//   "Abarth",
//   "Alfa Romeo",
//   "Aston Martin",
//   "Audi",
//   "Bentley",
//   "BMW",
//   "Bugatti",
//   "Cadillac",
//   "Chevrolet",
//   "Chrysler",
//   "Citroën",
//   "Dacia",
//   "Daewoo",
//   "Daihatsu",
//   "Dodge",
//   "Donkervoort",
//   "DS",
//   "Ferrari",
//   "Fiat",
//   "Fisker",
//   "Ford",
//   "Honda",
//   "Hummer",
//   "Hyundai",
//   "Infiniti",
//   "Iveco",
//   "Jaguar",
//   "Jeep",
//   "Kia",
//   "KTM",
//   "Lada",
//   "Lamborghini",
//   "Lancia",
//   "Land Rover",
//   "Landwind",
//   "Lexus",
//   "Lotus",
//   "Maserati",
//   "Maybach",
//   "Mazda",
//   "McLaren",
//   "Mercedes-Benz",
//   "MG",
//   "Mini",
//   "Mitsubishi",
//   "Morgan",
//   "Nissan",
//   "Opel",
//   "Peugeot",
//   "Porsche",
//   "Renault",
//   "Rolls-Royce",
//   "Rover",
//   "Saab",
//   "Seat",
//   "Skoda",
//   "Smart",
//   "SsangYong",
//   "Subaru",
//   "Suzuki",
//   "Tesla",
//   "Toyota",
//   "Volkswagen",
//   "Volvo"
// ];