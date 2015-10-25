// Temperature for Arduino 
#include "WProgram.h"
void setup();
void loop();
int  val = 0; 
char code[6]; 
int bytesread = 0;

#define TEMP_SENSOR 0
#define CON_SPEED 9600      // bps
#define WAIT_INTERVAL 200   // msec
#define SAMPLING 30

void setup()
{ 
  analogReference(INTERNAL);
  Serial.begin(CON_SPEED);  // Hardware serial for Monitor 9600bps
}

void loop()
{ 
  val = 0;
  bytesread = 0; 

  while((val != 10) && (val != 13))
  {
    if ( Serial.available() > 0 ) {
      val = Serial.read(); 
      if( val == 10 || val == 13 ) {
        code[bytesread] = '\0';        
        break;
      }
      code[bytesread] = val;         // add the digit
      bytesread++;                   // ready to read next digit  
    }
  } 

  // if read is complete 
  if(bytesread == 5)
  {
    if( strcmp(code,"00001") == 0 ) {
      int val = 0;
      double volt,temperature;
      int loop;

      // get temperature
      for( loop = 0; loop < SAMPLING; loop++ ) {
        val += analogRead(TEMP_SENSOR);
        delay(10);
      }
      val /= SAMPLING;  // average
      volt = 1.1 * val / 1024.0;
      temperature = ( volt - 0.6 ) * 100.0; // 10mV=1c
      
      // send temperature
      Serial.println(temperature);
    }
  }
  delay(WAIT_INTERVAL);              // wait for a second
} 


int main(void)
{
	init();

	setup();
    
	for (;;)
		loop();
        
	return 0;
}

