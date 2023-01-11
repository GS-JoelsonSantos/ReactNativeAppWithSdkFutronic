
import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  NativeModules
} from 'react-native';

export default function App(){

  const [startScan, setStartScan] = useState(0);

  useEffect(()=>{  
    NativeModules.ModuleFutronic.isStoragePermissionGranted();
  },[]);
  
  function changeStatusScanner(){
    if(startScan){
      console.log("Stoping Scan")
      NativeModules.ModuleFutronic.stopScan();
      setStartScan(0);
    }else{
      NativeModules.ModuleFutronic.checkFingerprint().then((res)=>{
        if(res){
          console.log("-- Starting Scan --")
          NativeModules.ModuleFutronic.stopScan();
          NativeModules.ModuleFutronic.StartScan();
          setStartScan(1);
        }else{
          alert("Verifique se o leitor está conectado ao smartphone \n Habilite todas as permissões solicitadas");
        }
      });
    }
  }

  return(
    <View style={styles.container}>

      <Text style={styles.title}>
        React Native APP With SDK Futronic 
      </Text>

      <TouchableOpacity 
        onPress={changeStatusScanner} 
        style={[styles.btn, {backgroundColor: (startScan) ? '#f91d1d': '#FFF'}]}
      >
        <Text style={[styles.btnTxt, {color: (startScan) ? '#FFF': '#7E7E7E'}]}>
            {(startScan) ? 'Stop': 'Start'} Scan
        </Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  title:{
    fontWeight: 'bold',
    fontSize: 16
  },  
  btn: {
    borderWidth: 1, 
    justifyContent: 'center', 
    alignItems: 'center', 
    padding: 20, 
    marginTop: 20
  },
  btnTxt: {
    fontSize: 15,
    fontWeight: 'bold'
  }
});
