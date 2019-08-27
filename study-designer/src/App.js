import React from 'react';
import './App.css';

import { 
  Button, 
  Card, 
  Form, 
  FormControl,
  InputGroup } from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';

class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      inprogressQuery: '',
      submittedQuery: '',
      loading: false,
    }

    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleSubmit (event) {
    this.setState({ 
      loading: true,
      submittedQuery: this.state.inprogressQuery
    });


    setTimeout(() => {
      this.setState({loading: false});
    }, 3000);
    event.preventDefault();
  }

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <div className="queryInput">
            <Form onSubmit={this.handleSubmit}>
              <InputGroup size="lg">
              
              <FormControl 
                value={this.state.inprogressQuery}
                onChange={evt => this.setState({inprogressQuery: evt.target.value})}
                aria-label="Large" 
                aria-describedby="inputGroup-sizing-sm"
                placeholder="What do you want to know?"
                 />
              
              <InputGroup.Append>
                <Button type="submit">Search</Button>              
              </InputGroup.Append>
            </InputGroup>
            </Form>
          </div>
        </header>


        <div className="App-body">
            { this.state.loading && 
              <>
                Creating study, please wait...
              </>
            }
            
            { this.state.submittedQuery !== '' && !this.state.loading && 
              <Card className="queryResults">
                <Card.Body>Showing search results for "{this.state.submittedQuery}"</Card.Body>
              </Card>
            }
          </div>
      </div>
    );
  }
}

export default App;
