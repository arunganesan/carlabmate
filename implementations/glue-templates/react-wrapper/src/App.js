import React from 'react';
import { LinkContainer } from 'react-router-bootstrap'
import { Nav, NavItem, Navbar, Button } from 'react-bootstrap'
import { BrowserRouter as Router, Route } from 'react-router-dom'
import 'bootstrap/dist/css/bootstrap.css'
import './App.css';

/*
import { Main as Alias } from './name-of-file.js'
*/
import { Main as WhereAreYou } from './WhereAreYou'
import { Main as WhereDoYouLive } from './WhereDoYouLive'


/*
User-specific
*/
const USERID = 2;


function MyLink (props) {
  return <NavItem>
    <LinkContainer to={'/' + props.to}>
      <Nav.Link eventKey={props.to}>
        { props.label ? props.label : props.to }
      </Nav.Link>
    </LinkContainer>
  </NavItem>;
}

function App() {
  return (
    <Router>
      <div id='wrapper'>
        <Navbar expand='lg' bg='primary' variant='dark'>
          <Nav className='mr-auto'>
            {/*
              for each implementation:

              <NavItem>
                <LinkContainer to={full url}>
                  <Nav.Link eventKey={to-address}>
                    {label}
                  </Nav.Link>
                </LinkContainer>
              </NavItem>
            */}

            <MyLink to='where-are-you' />
            <MyLink to='where-do-you-live' />
          </Nav>
        </Navbar>


        <div id='content'>
            {/*
            for each implementation:
              <Route path={full url} exact component={Component} />
            */}

            <Route path='/where-are-you' exact
              render={props => <WhereAreYou userid={USERID} />} />

            <Route 
              path='/where-do-you-live' 
              exact
              render={props => <WhereDoYouLive userid={USERID} />} />
        </div>
      </div>
    </Router>
  );
}

export default App;
