import * as React from "react";
import { Button, Container, Form } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.css";


/************************************
 * AUTO GENERATED - do not change
 **********************************/



type Props = { 
  produce: Function,
  update: Function,
  value: any
};


/************************************
 * End of auto generated
 **********************************/






interface TextInputProps {
  name: string,
  value: string,
  changeVal: Function,
  produce: Function,
 }


 

 const TextInput: React.SFC<TextInputProps> = (props) => <Container>
    <Form.Label style={{marginTop: "25p"}}>{props.name}</Form.Label>
      
    <Form.Control
        type="text"
        value={props.value}
        onChange={(evt: React.ChangeEvent<HTMLInputElement>) =>
          props.changeVal(evt.target.value)
        }
      />
   
   
    <Button
      onClick={() => props.produce(props.value)}
      style={{marginTop: "25px"}}
      size="lg"
    >
      Submit
    </Button>
</Container>;






export class acceptFuelLevel extends React.Component<Props, {}> {
  render() {
    const { update, produce, value } = this.props;

    return <TextInput
          name="Enter fuel level"
          value={value}
          changeVal={(val: string) => update(val)}
          produce={produce} />
  }
}



export class acceptPhoneNumber extends React.Component<Props, {}> {
  render() {
    const { update, produce, value } = this.props;

    return <TextInput
          name="Enter phone level"
          value={value}
          changeVal={(val: string) => update(val)}
          produce={produce} />
  }
}

