// RFID reader for Arduino 
// Wiring version by BARRAGAN <http://people.interaction-ivrea.it/h.barragan> 
// Modified for Arudino by djmatic
// Modified by Worapoht K.
// http://www.arduino.cc/playground/Learning/PRFID のサンプルを利用
 
#include <WString.h>
#include <MsTimer2.h>
#include <SoftwareSerial.h>

int  val = 0; 
char code[10]; 
int bytesread = 0; 

String strCode = String(10);
String strOldCode = String(10);

#define RFIDEnablePin 2
#define rxPin 8
#define txPin 9
// RFID reader SOUT pin connected to Serial RX pin at 2400bps to pin8
#define LED 5

#define CON_SPEED 9600      // bps
#define RFID_SPEED 2400     // bps
#define TIMER_INTERVAL 10000 // 10sec
#define WAIT_INTERVAL 1000  // 1sec

void setup()
{ 
  Serial.begin(CON_SPEED);  // Hardware serial for Monitor 9600bps

  pinMode(RFIDEnablePin,OUTPUT);       // Set digital pin 2 as OUTPUT to connect it to the RFID /ENABLE pin 
  digitalWrite(RFIDEnablePin, LOW);    // Activate the RFID reader 
  pinMode(LED,OUTPUT);                // input complete LED
  digitalWrite(LED,LOW);
  
  strCode = "";
  strOldCode = "";
  MsTimer2::set(TIMER_INTERVAL,intterupt);
}

void loop()
{ 
  SoftwareSerial RFID = SoftwareSerial(rxPin,txPin);
  RFID.begin(RFID_SPEED);
  digitalWrite(LED,LOW);

  if((val = RFID.read()) == 10)
  {   // check for header 
    bytesread = 0; 
    
    while(bytesread<10)
    {  // read 10 digit code 
      val = RFID.read(); 
      if((val == 10)||(val == 13))
      {  // if header or stop bytes before the 10 digit reading 
        break;                       // stop reading 
      } 
      code[bytesread] = val;         // add the digit
      bytesread++;                   // ready to read next digit  
    } 

    // if 10 digit read is complete 
    if(bytesread == 10)
    {
      strCode.clear();
      strCode.append(code);
      if( strCode.equals(strOldCode.getChars()) ) {
        // nop  
      }else{
        Serial.print(code);            // print the TAG code
        digitalWrite(LED,HIGH);
        strOldCode = strCode;
        MsTimer2::start();             // Timer start
      }
    }
    bytesread = 0; 
    delay(WAIT_INTERVAL);              // wait for a second
  } 
} 

void intterupt() {
  strOldCode = "";
  Serial.flush();
}

