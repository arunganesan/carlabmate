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


class InputPhoneNumberBase extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      test: props.test === undefined ? false : props.test,
      required_info: [],
      outputSensors: ["phone-number"],
      refers_info: [],
      phonenumber: '',
    };

    this.libcarlab = props.libcarlab;
  }

  componentDidMount() {
    this.libcarlab.checkNewInfo((info, data) => {
      console.log("Got info ", info, "with data", data);
    });
  }

  submitData() {
    console.log('Sending ', this.state.phonenumber);
    this.libcarlab.outputNewInfo(
      new DataMarshal(
        Registry.PhoneNumber,
        this.state.phonenumber), 
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

export default class InputPhoneNumber extends InputPhoneNumberBase {
  render() {
    return (
      <Container>
        <Form.Label style={style.input}>Save phone number</Form.Label>
          
          
        <Form.Control
            type="text"
            value={this.state.phonenumber}
            onChange={(evt) =>
              this.setState({
                phonenumber: evt.target.value
              })
            }
          />
       
       
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
