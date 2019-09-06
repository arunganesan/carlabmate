import React from 'react';
import { 
    Button, 
    Container,
    Form,
} from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';

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
           message: ''
        }
    }

    componentDidMount () {
        if (navigator && navigator.geolocation) {
            navigator.geolocation.getCurrentPosition((pos) => {
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
        this.props.onNewData('home-work', this.state.message);
    }

    render () {
        // Render a map picker where the user can pick their location
        // This gets uploaded to the server as "new information"
        return <Container>
            <Form.Label style={style.input}>Where do you live?</Form.Label>
            <Form.Control 
                style={style.input}
                aria-label="large" 
                aria-describedby="inputGroup-sizing-lg"
                value={this.state.message} 
                onChange={(evt) => this.setState({ 
                    message: evt.target.value })} />
        
            <Button onClick={() => this.submitData()} style={style.button} size="lg">Submit</Button>    
        </Container>
    }
}
