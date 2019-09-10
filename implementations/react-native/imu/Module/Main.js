import React  from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Libcarlab } from '../Libcarlab'
import { Accelerometer } from 'expo-sensors';

export class Main extends React.Component {
    constructor (props) {
        super(props);

        this.state = {
            message: '',
            userid: (props.userid === undefined) ? 0 : props.userid,
            test: (props.test === undefined) ? false : props.test,
            required_info: [],

            accelerometerData: {},

        }
        this.libcarlab = new Libcarlab(
            this.state.userid,
            this.state.required_info,
            this.state.test)

    }
    
    componentDidMount() {
        this._toggle();
    }

    componentWillUnmount() {
        this._unsubscribe();
    }

    _toggle = () => {
        if (this._subscription) {
          this._unsubscribe();
        } else {
          this._subscribe();
        }
    };


    _subscribe = () => {
        this._subscription = Accelerometer.addListener(accelerometerData => {
            this.setState({ accelerometerData });
        });

        Accelerometer.setUpdateInterval(50);
    };
    
    _unsubscribe = () => {
        this._subscription && this._subscription.remove();
        this._subscription = null;
    };


    render() {
        let { x, y, z } = this.state.accelerometerData;


        return (
        <View style={styles.container}>
              <Text>Accelerometer:</Text>
            <Text>
                x: {round(x)} y: {round(y)} z: {round(z)}
            </Text>
        </View>)
    }
}

function round(n) {
    if (!n) {
      return 0;
    }
  
    return Math.floor(n * 100) / 100;
  }

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
