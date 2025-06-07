import {StyleSheet} from 'react-native';

export default StyleSheet.create({
  container: {
    flex: 1, 
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#FFFFFF'
  },
  columnLeft: {
    flex: 1,
    paddingTop: 30,
    alignItems: 'center',
    justifyContent: 'space-between'
  },
  title: {
    fontWeight: 'bold',
    color: '#000',
    fontSize: 20,
    marginTop: 20,
    marginBottom: 40
  },  
  module: {
    width: '65%',
    height: 220,
    backgroundColor: '#000',
    borderRadius: 10,
  },
  areaLeds: {
    height: 50,
    width: '100%',
    padding: 10,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems:'center',
  },
  led:{
    width: 80,
    height: 15,
    borderRadius: 100,
    backgroundColor: '#00ff65'
  },
  body: {
    alignItems: 'center',
    justifyContent: 'flex-end',
    width: '100%',
    height: 170,
  },
  areaFinger: {
    borderLeftWidth: 10,
    borderRightWidth: 10,
    borderTopWidth: 15,
    borderTopStartRadius: 10,
    borderTopEndRadius: 10,
    borderColor: '#5F6368',
    width: '55%',
    height: 150,
    backgroundColor: '#FFF',
    padding: 5
  },
  areaStatus: {
    marginVertical: 50,
    padding: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  txtStatus: {
    fontSize: 16,
    color: '#000000',
    textAlign: 'center',
    justifyContent: 'center',
    alignItems: 'center',
  },
  btn: {
    borderWidth: 2,
    width: '60%',
    height: 80,
    justifyContent: 'center',
    alignItems:'center',
    borderRadius: 10
  },
  btnTxt: {
    fontWeight: 'bold',
    fontSize: 22,
  },
  image: {
    borderWidth: 1, 
    width: '100%', 
    height: '100%'
  },
  btnOption: {
    position: 'absolute',
    bottom: 10,
    left: 10,
    borderRadius: 5,
    padding: 5
  },
  btnOptionTxt:{
    color: "#FFF",
    fontSize: 12,
    fontWeight: 'bold'
  }
});
