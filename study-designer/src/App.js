import React from 'react';
import logo from './logo.svg';
import './App.css';

import { Card, FormControl, InputGroup } from 'react-bootstrap';
import { FaHatWizard } from 'react-icons/fa'; 
import 'bootstrap/dist/css/bootstrap.css';

class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      query: ''
    }
  }

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <div className="queryInput">
            <InputGroup size="lg">
              <InputGroup.Prepend>
                <InputGroup.Text id="inputGroup-sizing-lg"><FaHatWizard /></InputGroup.Text>
              </InputGroup.Prepend>
              
              <FormControl 
                value={this.state.query}
                onChange={evt => this.setState({query: evt.target.value})}
                aria-label="Large" 
                aria-describedby="inputGroup-sizing-sm"
                placeholder="What do you want to know?"
                 />

            </InputGroup>

            { this.state.query !== '' && 
            <Card className="queryResults">
              <Card.Body>Showing search results for "{this.state.query}"</Card.Body>
            </Card>}
          </div>
        </header>
      </div>
    );
  }
}

export default App;
