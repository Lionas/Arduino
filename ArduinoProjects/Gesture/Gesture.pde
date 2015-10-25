// Gesuture command for Arduino 
int  val = 0; 
char code[6]; 
int bytesread = 0; 

#define LEFT_F 2
#define RIGHT_F 4
#define LEFT_R 3
#define RIGHT_R 5

#define CON_SPEED 9600      // bps
#define WAIT_INTERVAL 500

void setup()
{ 
  Serial.begin(CON_SPEED);  // Hardware serial for Monitor 9600bps
  pinMode(RIGHT_F,OUTPUT);
  pinMode(LEFT_F,OUTPUT);
  pinMode(RIGHT_R,OUTPUT);
  pinMode(LEFT_R,OUTPUT);
  digitalWrite(RIGHT_F,LOW);
  digitalWrite(LEFT_F,LOW);
  digitalWrite(RIGHT_R,LOW);
  digitalWrite(LEFT_R,LOW);
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
    if( strcmp(code,"RIGHT") == 0 ) {
      digitalWrite(RIGHT_F,LOW);
      digitalWrite(LEFT_F,HIGH);
      digitalWrite(RIGHT_R,LOW);
      digitalWrite(LEFT_R,LOW);
    }
    else if( strcmp(code,"LEFT*") == 0 ) {
      digitalWrite(RIGHT_F,HIGH);
      digitalWrite(LEFT_F,LOW);
      digitalWrite(RIGHT_R,LOW);
      digitalWrite(LEFT_R,LOW);
    }
    else if( strcmp(code,"STOP*") == 0 ) {
      digitalWrite(RIGHT_F,LOW);
      digitalWrite(LEFT_F,LOW);
      digitalWrite(RIGHT_R,LOW);
      digitalWrite(LEFT_R,LOW);
    }      
    else if( strcmp(code,"FORWD") == 0 ) {
      digitalWrite(RIGHT_F,HIGH);
      digitalWrite(LEFT_F,HIGH);
      digitalWrite(RIGHT_R,LOW);
      digitalWrite(LEFT_R,LOW);
    }      
    else if( strcmp(code,"BACK*") == 0 ) {
      digitalWrite(RIGHT_F,LOW);
      digitalWrite(LEFT_F,LOW);
      digitalWrite(RIGHT_R,HIGH);
      digitalWrite(LEFT_R,HIGH);
    }      
  }
  delay(WAIT_INTERVAL);              // wait for a second
} 

