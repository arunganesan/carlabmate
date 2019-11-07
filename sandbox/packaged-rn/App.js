import React from "react";
import { StyleSheet, Text, View, AppRegistry } from "react-native";
import { NativeRouter, Route, Link } from "react-router-native";


/*
import { Main as ModuleName } from './module-name/Main'
*/

/*
User-specific
*/
const USERID = 2;

const MyLink = (props) =>  (
  <Link to={`/${props.to}`} style={styles.navItem}>
    <Text>{props.label ? props.label : props.to}</Text>
  </Link>);

export default App = () => (
  <NativeRouter>
    <View style={styles.container}>
      <View style={styles.nav}>

        {/*
        For each implementation:
        <MyLink to='module-name' />
        */}        
      </View>


      {/*
      For each implementation:
      <Route exact path={/module-name} component={ModuleName} />
      */}
    </View>
  </NativeRouter>
);

const styles = StyleSheet.create({
  container: {
    marginTop: 25,
    padding: 10
  },
  header: {
    fontSize: 20
  },
  nav: {
    flexDirection: "row",
    justifyContent: "space-around"
  },
  navItem: {
    flex: 1,
    alignItems: "center",
    padding: 10
  },
  subNavItem: {
    padding: 5
  },
  topic: {
    textAlign: "center",
    fontSize: 15
  }
});

AppRegistry.registerComponent("MyApp", () => App);