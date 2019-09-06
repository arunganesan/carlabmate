import React from 'react';
import { 
    Button, 
    Container,
    FormControl,
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
                const coords = pos.coords;
                this.setState({
                    message: `${coords.latitude}, ${coords.longitude}`
                })
            })
        }
    }
    
    submitData() {
        this.props.onNewData('location', this.state.message);
    }

    render () {
        // Render a map picker where the user can pick their location
        // This gets uploaded to the server as "new information"
        return <Container>
        
            <FormControl 
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
