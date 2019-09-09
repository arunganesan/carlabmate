import React from 'react';
import { 
    Button, 
    Container,
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

        this.libcarlab = Libcarlab(
            this.state.userid,
            this.state.required_info,
            this.state.test,
        )
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


        this.libcarlab.checkNewInfo((info, data) => {
            console.log('Got info ', info, 'with data', data);
        });
    }
    
    submitData() {
        this.props.onNewInfo('home-work', this.state.message);
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
