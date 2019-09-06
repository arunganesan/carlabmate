import React from 'react';
import './App.css';
import { Main } from './Main'

function uploadData (name, info) {
  console.log(`Uploadnig ${name} - ${info}`)
}

function checkNewData () {
  // Get new data
  // check with local storage first to only get relevant data
}


function App() {
  return (
    <Main
      checkNewData={() => checkNewData()}
      onNewData={(n,d) => uploadData(n, d)}  
    />
  );
}

export default App;
