import * as React from "react";
import { ButtonToolbar, Button, Row, Container, Col } from "react-bootstrap";

import GoogleMapReact from 'google-map-react';

import { MdDirectionsBike, MdDirectionsRun, MdLocationOn,  } from "react-icons/md";

import { GiHole } from "react-icons/gi";



import "bootstrap/dist/css/bootstrap.css";


/************************************
 * AUTO GENERATED - do not change
 **********************************/



type Props = { 
  produce: Function,
  update: Function,
  value: any,

  location: any,
  sightingsMap: any,
};


/************************************
 * End of auto generated
 **********************************/


export class acceptSightingsReport extends React.Component<Props, {}> {

  flagHazard(hazardType: any) {
    const { location, produce } = this.props;
    let hazard = {
      lat: location.lat,
      lng: location.lng,
      hazard: hazardType
    }
    produce(hazard)
  }

  render() {
    //  update, produce, value,
    const { sightingsMap, location } = this.props;

    return <Container>
      <Row><Col><div style={{ height: '70vh', width: '100%' }}>
    <GoogleMapReact
      bootstrapURLKeys={{ key: 'AIzaSyCvsdzVGSonWxVoZ5-OUzaH0_iV-mosEnk' }}
      center={location}
      defaultZoom={17}
    >
      <MyMarker
        lat={location['lat']}
        lng={location['lng']}
        type='current'
      />

      { sightingsMap.map((sighting: any) => {
        return <MyMarker
          lat={sighting.lat}
          lng={sighting.lng}
          type={sighting.hazard} />
      }) }
    </GoogleMapReact>
  </div></Col></Row>

  <Row><Col>
    <ButtonToolbar>
      <Button size="lg" variant="primary" onClick={() => this.flagHazard('pothole')}><GiHole /></Button>
      <Button size="lg" variant="secondary"><MdDirectionsRun /></Button>
      <Button size="lg" variant="success"><MdDirectionsBike /></Button>
    </ButtonToolbar>
  </Col></Row>
      </Container>
  }
}

const MyMarker = ({type}: any) => {
  if (type == 'current') 
    return <MdLocationOn size={30} />
  else if (type == 'pothole')
    return <GiHole size={30} />
  else if (type == 'pedestrian')
    return <MdDirectionsRun size={30} />
  // else if (type == 'biker') 
  return <MdDirectionsBike size={30} />
}