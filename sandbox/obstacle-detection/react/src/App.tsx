import React from "react";
import "./App.css";
import { Nav, NavItem, Navbar, Button } from "react-bootstrap";
import { Modal, Container, Row, Form, Col } from "react-bootstrap";
import { Libcarlab, Information, DataMarshal, Registry } from 'libcarlab';

import "bootstrap/dist/css/bootstrap.css";

import { acceptSightingsReport as AcceptSightingsReport } from "obstacle-warning-react";

type AppState = {
  message: string;
  session: string | null;
  showLoginForm: boolean;
  required_info: Information[];
  username: string;
  password: string;

  sightingsMap: any,
  location: any
};

class App extends React.Component<{}, AppState> {
  libcarlab: Libcarlab;

  constructor(props: any) {
    super(props);

    let sessionString: string | null = window.localStorage.getItem(
      "localSession"
    );

    let sessionLocal = null;
    if (sessionString != null) sessionLocal = JSON.parse(sessionString);
    else
      sessionLocal = {
        session: null,
        username: ""
      };
    
    this.state = {
      message: "",
      required_info: [Registry.SightingsMap, Registry.Location],
      showLoginForm: sessionLocal["session"] == null,
      session: sessionLocal["session"],
      username: sessionLocal["username"],
      password: "",  
      sightingsMap: [
        { hazard: 'pothole', lat: 42.3065, lng: -83.707684},
        { hazard: 'pedestrian', lat: 42.307, lng: -83.707684},
        { hazard: 'biker', lat: 42.308, lng: -83.707684}
      ],
      location: { lat: 42.305958,lng: -83.707684},
    };

    this.libcarlab = new Libcarlab(
      this.state.session,
      this.state.required_info
    );
  }

  componentDidMount() {
    if (this.state.session != null) 
      this.getLatest();

    setTimeout(() => this.downloadNewInfo(), 1000);
  }

  downloadNewInfo() {
    if (this.state.session != null)
      this.libcarlab.checkNewInfo((data: DataMarshal) => {
        console.log("RECEIVED FROM SERVER", data);
        if (data.info.name == 'sightings-map' && data.message != null) {
          this.setState({
            sightingsMap: data.message.message
          });
        } else if (data.info.name == 'location' && data.message != null && data.message.message != null) {
          this.setState({
            location: data.message.message
          });
        }
      });

    setTimeout(() => this.downloadNewInfo(), 1000);
  }

  getLatest() {
    this.libcarlab.checkLatestInfo((data: DataMarshal) => {
      console.log(data);
      if (data.info.name == 'sightings-map' && data.message != null) {
        this.setState({
          sightingsMap: data.message.message
        });
      } else if (data.info.name == 'location' && data.message != null) {
        this.setState({
          location: data.message.message
        });
      }
    });
  }

  tryLoggingIn() {
    let loginurl = `http://localhost:8080/login?username=${this.state.username}&password=${this.state.password}`;
    fetch(loginurl, {
      method: "post",
      mode: "cors",
      cache: "no-cache",
      headers: { "content-type": "application/json" }
    })
      .then(res => res.json())
      .then(data => {
        window.localStorage.setItem("localSession", JSON.stringify(data));
        this.setState({
          session: data["session"],
          username: data["username"],
          showLoginForm: false
        });

        this.getLatest();
      })
      .catch(function() {
        console.log("error");
      });
  }

  generateLoginForm() {
    let onHide = () => this.setState({ showLoginForm: false });

    return (
      <Modal
        show={this.state.showLoginForm}
        onHide={onHide}
        {...this.props}
        size="lg"
        aria-labelledby="contained-modal-title-vcenter"
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title id="contained-modal-title-vcenter">
            Please log in
          </Modal.Title>
        </Modal.Header>

        <Modal.Body>
          <Form.Group>
            <Form.Label>Username</Form.Label>
            <Form.Control
              type="text"
              value={this.state.username}
              onChange={(evt: any) =>
                this.setState({
                  username: evt.target.value
                })
              }
            />
          </Form.Group>

          <Form.Group>
            <Form.Label>Password</Form.Label>
            <Form.Control
              type="password"
              value={this.state.password}
              onChange={(evt: any) =>
                this.setState({
                  password: evt.target.value
                })
              }
            />
          </Form.Group>

          <Button onClick={() => this.tryLoggingIn()}>Login</Button>
        </Modal.Body>
      </Modal>
    );
  }

  showLoginInfo() {
    if (this.state.session == null) {
      return (
        <Button
          onClick={() =>
            this.setState({
              showLoginForm: true
            })
          }
        >
          Log in
        </Button>
      );
    } else {
      return (
        <>
          Logged in as {this.state.username}
          <Button
            style={{marginLeft: 10}}
            onClick={() =>
              this.setState({
                session: null
              })
            }
          >
            Log out
          </Button>
        </>
      );
    }
  }

  render() {
    return (
      <Container style={{paddingTop: 25}}>
        {this.state.showLoginForm && this.generateLoginForm()}
        <Row>
          <Col>{this.showLoginInfo()}</Col>
        </Row>

        {this.state.session != null && [
          <AcceptSightingsReport
            value={this.state.sightingsMap}
            location={this.state.location}
            sightingsMap={this.state.sightingsMap}
            update={(val: string) => {}}
            produce={(val: string) => {
                alert('submitting: ' + JSON.stringify(val));
                this.libcarlab.outputNewInfo(
                    new DataMarshal(Registry.Sighting, JSON.stringify(val)),
                    () => {}
                );
            }}
          />
        ]}
      </Container>
    );
  }
}

export default App;
