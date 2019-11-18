import * as React from "react";
import { Button, Container, Form } from "react-bootstrap";
import { Libcarlab, DataMarshal, Registry } from 'libcarlab';
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
  libcarlab: Libcarlab
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


  // TODO need to call this on a timer
  // Also how does this know what the data is?
  componentDidMount() {
    this.props.libcarlab.checkNewInfo((data: DataMarshal) => {
      console.log("Got info ", data.info, "with data", data.value);
    });
  }


  submitData() {
    console.log('Sending ', this.state.fuelLevel);
    this.props.libcarlab.outputNewInfo(
      new DataMarshal(
        Registry.FuelLevel,
        this.state.fuelLevel), 
      
        res => {
          console.log("Response msessage ", res);
        }
      );
  }

  render() {
    const { libcarlab } = this.props;


    return (
      <Container>
        <Form.Label style={style.input}>Save phone number</Form.Label>
          
          
        <Form.Control
            type="text"
            value={this.state.fuelLevel}
            onChange={(evt) =>
              this.setState({
                fuelLevel: evt.target.value
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



export class acceptPhoneNumber extends React.Component<Props> {
  render() {
    const { text } = this.props;

    return <div style={{ color: "blue" }}>Hello {text}</div>;
  }
}


export class acceptCarModel extends React.Component<Props> {
  render() {
    const { text } = this.props;

    return <div style={{ color: "blue" }}>Hello {text}</div>;
  }
}