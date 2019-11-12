import React from "react";
import "./App.css";
import InputCarModel from "./InputCarModel/Algorithm";
import { Nav, NavItem, Navbar, Button } from "react-bootstrap";
import { Modal, Container, Row, Form, Col } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.css";
import {
  Libcarlab,
  Information,
  DataMarshal,
  Registry
} from "./LibcarlabReact";

type AppState = {
  message: string;
  session: string | null;
  showLoginForm: boolean;
  test: boolean;
  required_info: Information[];
  outputSensors: string[];
  username: string;
  password: string;
};

class App extends React.Component<{}, AppState> {
  carlab: Libcarlab;

  constructor(props: any) {
    super(props);

    let sessionString: string | null = window.localStorage.getItem(
      "localSession"
    );

    let sessionLocal = null;
    if (sessionString != null) sessionLocal = JSON.parse(sessionString);

    this.state = {
      message: "",
      test: props.test === undefined ? false : props.test,
      required_info: [],
      outputSensors: ["car-model"],
      showLoginForm: sessionLocal == null,
      session: sessionLocal,
      username: "",
      password: ""
    };

    this.carlab = new Libcarlab(this.state.session, this.state.required_info);

    // Go through all algorithms, get their required list of sensors
    // Instatiate them as objects, can render them later
    // Login page. That's the main thing TBH
  }

  tryLoggingIn() {
    let loginurl = `http://localhost:1234/login?username=${this.state.username}&password=${this.state.password}`;
    fetch(loginurl, {
      method: "post",
      mode: "cors",
      cache: "no-cache",
      headers: { "content-type": "application/json" }
    })
      .then(res => res.json())
      .then(data => {
        window.localStorage.setItem(
          "localSession",
          JSON.stringify(data["session"])
        );
        this.setState({
          session: data["session"],
          showLoginForm: false
        });
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
      <Container>
        {this.state.showLoginForm && this.generateLoginForm()}
        <Row>
          <Col>{this.showLoginInfo()}</Col>
        </Row>

        {this.state.session != null && [
          <InputCarModel libcarlab={this.carlab} />
        ]}
      </Container>
    );
  }
}

export default App;
