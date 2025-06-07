
import React, { useState, useEffect } from 'react';
import {
  Text,
  View,
  TouchableOpacity,
  NativeModules,
  StatusBar,
  ActivityIndicator,
  Image
} from 'react-native';

import {
  exists,
  unlink,
  ExternalDirectoryPath,
  DocumentDirectoryPath
} from 'react-native-fs';

import styles from './styles';

let colors = {normal: "#00000099", danger: "#dc3545", success: "#198754"};
let available = false;

const BASEDIR = Platform.select({
  ios: `${DocumentDirectoryPath}/futronic/fingers`,
  android: `${ExternalDirectoryPath}/futronic/fingers`,
});

export default function App()
{  
  const [isStarted, setIsStarted] = useState(false);
  const [enabledFakeFinger, setEnabledFakeFinger] = useState(false);
  const [msgStatus, setMsgStatus] = useState("Connect the fingerprint scanner!");
  const [image, setImage] = useState(null);

  useEffect(()=>{  
    // startScan();
  },[]);

  const startScan = async () =>
  {
    setIsStarted(true);
    available = true;
    setImage(null);
    await fileFingerprintDelete('digital.bmp');
    await fileFingerprintDelete('digital.wsq');
  
    openDevice();
  }

  function openDevice()
  {
    if ( !available ) { return; }

    NativeModules.ModuleFutronic.checkDeviceFingerprint()
    .then( (deviceIsOpen) => 
    {
      if ( deviceIsOpen )
      {
        NativeModules.ModuleFutronic.StartScan(); 

        if ( enabledFakeFinger )
          NativeModules.ModuleFutronic.enableLFD();
        else 
          NativeModules.ModuleFutronic.disableLFD();

        startScanService();
      }else
      {
        setMsgStatus("Connect the fingerprint scanner!");
        setIsStarted(false);
        available = false;
      }
    });
  }

  function startScanService()
  {
    NativeModules.ModuleFutronic.checkDeviceIsOpen()
    .then( async(deviceIsOpen) => 
    {
      if ( !deviceIsOpen ) 
      {
        setMsgStatus("Connect the fingerprint scanner!");         
        setIsStarted(false);
        available = false;
        return;
      }

      setMsgStatus('Place your finger on the fingerprint scanner');

      let fileExists = await fileFingerprintExists('digital.bmp');

      if ( fileExists )
        setImage(`file://${ExternalDirectoryPath}/futronic/fingers/digital.bmp` + '?v=' + new Date());

      if ( available )
        setTimeout( () => startScanService(), 1000);            
    });
  }

  const fileFingerprintExists = async (fileName) =>
  {
    if ( await exists(`${BASEDIR}/${fileName}`) )
      return true;

    return false;
  }

  const fileFingerprintDelete = async (fileName) =>
  {
    try 
    {
      if ( await exists(`${BASEDIR}/${fileName}`) ) 
      {
        await unlink(`${BASEDIR}/${fileName}`);
      }
    } catch (err) {
      console.log("Error when deleting: ",`${BASEDIR}/${fileName}`,err);
    }
  }

  const stopScanService = () => 
  {
    NativeModules.ModuleFutronic.stopScan();
    setIsStarted(false);
    available = false;
    setMsgStatus("Start the fingerprint scanner service!");
  }

  const handleLDF = async () =>
  {
    const deviceIsOpen = await NativeModules.ModuleFutronic.checkDeviceFingerprint();
    const isScanning = await NativeModules.ModuleFutronic.checkDeviceIsOpen();
    const status = enabledFakeFinger ? false : true

    if ( deviceIsOpen && isScanning )
    {
      console.log("DETECT FAKE FINGER", enabledFakeFinger);
      NativeModules.ModuleFutronic.StartScan(); 

      if ( status )
        NativeModules.ModuleFutronic.enableLFD();
      else 
        NativeModules.ModuleFutronic.disableLFD();

      setEnabledFakeFinger( status );  
    }
  }

  return(
    <View style={styles.container}>
      <StatusBar backgroundColor="#FFFFFF" barStyle="dark-content"/>

      <Text style={styles.title}>
        React Native APP With SDK Futronic 
      </Text>

      <View style={styles.module}>
        <View style={styles.areaLeds}>
          <View style={styles.led}></View>
        </View>
        <View style={styles.body}>
          <View style={styles.areaFinger}>          

            {
              image && (
                <Image 
                  source={{ uri: image }} 
                  resizeMode="stretch"  
                  style={styles.image}
                  cache="reload"
                />
              )
            }
         
          </View>
        </View>
      </View>

      <View style={styles.areaStatus}>

        {
          isStarted && (
            <ActivityIndicator size="large" color={colors.normal} />
          )
        }

        <Text style={styles.txtStatus}> { msgStatus } </Text>
        
      </View>

      <TouchableOpacity 
        style={[styles.btn, { borderColor : !isStarted ? colors.normal : colors.danger }]} 
        onPress={ !isStarted ? startScan : stopScanService }
      >
        <Text style={[styles.btnTxt, { color : !isStarted ? colors.normal : colors.danger }]}> 
          { !isStarted ? "START SCAN" : "STOP SCAN " }           
        </Text>
      </TouchableOpacity>

      <TouchableOpacity 
        style={[styles.btnOption, { backgroundColor : enabledFakeFinger ? colors.success : colors.danger }]}
        onPress={ handleLDF }
      >
        <Text style={styles.btnOptionTxt}> LFD </Text>
      </TouchableOpacity>

    </View>
  );
}
