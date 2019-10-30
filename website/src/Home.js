import React from 'react';
import './App.css';
import 'bootstrap/dist/css/bootstrap.css';
import Lorem from 'react-lorem-component'


export default class Home extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
    }
  }

  render() {
    return (
        <div>
            Home <Lorem />
        </div>
    );
  }
}
