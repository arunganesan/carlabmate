import React from 'react';
import { 
    Button, 
    Container,
    InputGroup,
    Form,
} from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import { Libcarlab } from './Libcarlab'

const style = {
    input: {
      marginTop: '25px'  
    },
    button: {
        marginTop: '25px'
    }
}

export class Main extends React.Component { 
    constructor (props) {
        super(props);
        
        this.state = {
            message: '',
            userid: (props.userid === undefined) ? 0 : props.userid,
            test: (props.test === undefined) ? false : props.test,
            required_info: [],
        }

        this.libcarlab = new Libcarlab(
            this.state.userid,
            this.state.required_info,
            this.state.test,
        )
    }

    componentDidMount () {
        this.libcarlab.checkNewInfo((info, data) => {
            console.log('Got info ', info, 'with data', data);
        });
    }

    locateMe() {
        console.log('Locating!')
        if (navigator && navigator.geolocation) {
            navigator.geolocation.getCurrentPosition((pos) => {
                console.log('Received response?', pos);
                this.setState({
                    message: JSON.stringify({
                        'lat': pos.coords.latitude,
                        'lng': pos.coords.longitude,
                    })
                })
            })
        }
    }
    
    submitData() {
        this.libcarlab.outputNewInfo('location', this.state.message, (res) => {
            console.log('Response:', res);
        });
    }

    render () {
        // Render a map picker where the user can pick their location
        // This gets uploaded to the server as "new information"
        return <Container>
            <Form.Label style={style.input}>Enter your location</Form.Label>

            <InputGroup>
                <InputGroup.Prepend>
                    <Button 
                        onClick={() => this.locateMe()}
                        variant="secondary">Locate Me</Button>
                </InputGroup.Prepend>
                
            <Form.Control 
                style={style.input}
                // aria-label="large" 
                // aria-describedby="inputGroup-sizing-lg"
                value={this.state.message} 
                onChange={(evt) => this.setState({ 
                    message: evt.target.value })} />

            </InputGroup>
            <Button 
                onClick={() => this.submitData()} 
                style={style.button} 
                size="lg">Submit</Button>    
        </Container>;
    }
}
