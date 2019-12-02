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



const carmodels = [
  "Ford Focus 2016", // https://media.ford.com/content/dam/fordmedia/North%20America/US/product/2016/focus/2016-Ford-Focus-Tech-Specs-FINAL.pdf
  "Ford Explorer 2016", // https://media.ford.com/content/dam/fordmedia/North%20America/US/product/2016/2016-ford-explorer-tech-specs.pdf
  "Ford Lincoln MKZ 2018", // https://media.lincoln.com/content/dam/lincolnmedia/lna/us/product/2016/17MKZ-TechSpecs.pdf
  "Ford Fiesta 2017", // https://www.ford.com/cars/fiesta/2017/models/fiesta-se-hatchback/
  "Ford Escape 2017" // https://media.ford.com/content/dam/fordmedia/North%20America/US/Events/17-LAAS/2017-ford-escape-tech-specs.pdf
]

export class acceptCarModel extends React.Component<Props, {}> {
  render() {
    const { update, produce, value } = this.props;
    return <Container>
              <Form.Label style={{marginTop: "25p"}}>Choose car model</Form.Label>
                
              <Form.Control
                aria-label="large"
                aria-describedby="inputGroup-sizing-lg"
                value={value}
                onChange={(evt: React.ChangeEvent<HTMLInputElement>) =>
                   update(evt.target.value)}
                as="select"
              >
                {carmodels.map(elt => (
                  <option key={elt}>{elt}</option>
                ))}
              </Form.Control>
              
              <Button
                onClick={() => produce(value)}
                style={{marginTop: "25px"}}
                size="lg"
              >
                Submit
              </Button>
          </Container>;
  }
}