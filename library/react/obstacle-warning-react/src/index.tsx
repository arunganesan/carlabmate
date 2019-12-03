import * as React from "react";
// import { Button, Container, Form } from "react-bootstrap";

import GoogleMapReact from 'google-map-react';


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


export class acceptSightingsReport extends React.Component<Props, {}> {
  render() {
    // const { update, produce, value } = this.props;

    return <div style={{ height: '100vh', width: '100%' }}>
    <GoogleMapReact
      bootstrapURLKeys={{ key: 'AIzaSyCvsdzVGSonWxVoZ5-OUzaH0_iV-mosEnk' }}
      defaultCenter={{lat: 11.0168, lng: 76.9558 }}
      defaultZoom={11}
    >
      <AnyReactComponent
        lat={11.0168}
        lng={76.9558}
        text="My Marker"
      />
    </GoogleMapReact>
  </div>
  }
}

const AnyReactComponent = ({text}: any) => <div
style={{width: '20px', height: '20px', border: 'solid thin black', boxShadow: '12px 12px 2px 1px rgba(0, 0, 255, .2);', backgroundColor: 'white'}}>{text}</div>;
