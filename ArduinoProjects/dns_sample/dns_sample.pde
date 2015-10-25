#include <Ethernet.h>
#include <Dns.h>

const byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };
const byte sip[] = { 192, 168, 1, 177 };
const byte gateway[] = { 192, 168, 1, 1 };
const byte Pri_DNS[] = { 192, 168, 100, 1 };
const char query_hamayan_name[] = "hamayan.ddo.jp";
const char query_google_name[] = "google.com";

Dns dns = Dns( Pri_DNS );

void setup()
{
  int result;
  byte dip[4];

  delay( 1000 );
  Ethernet.begin( (uint8_t *)mac, (uint8_t *)sip, (uint8_t *)gateway );
  Serial.begin( 38400 );
  delay( 3000 );

  if( (result = dns.getaddrbydns( query_hamayan_name, dip )) == 0 )
  {
    Serial.print( "IP Adr=" );
    Serial.print( dip[0], DEC );
    Serial.print( '.' );
    Serial.print( dip[1], DEC );
    Serial.print( '.' );
    Serial.print( dip[2], DEC );
    Serial.print( '.' );
    Serial.print( dip[3], DEC );
    Serial.println();
  }
  else
  {
    Serial.print( "NG1 type=" );
    Serial.println( result, DEC );
  }

  if( (result = dns.getaddrbydns( query_google_name, dip )) == 0 )
  {
    Serial.print( "IP Adr=" );
    Serial.print( dip[0], DEC );
    Serial.print( '.' );
    Serial.print( dip[1], DEC );
    Serial.print( '.' );
    Serial.print( dip[2], DEC );
    Serial.print( '.' );
    Serial.print( dip[3], DEC );
    Serial.println();
  }
  else
  {
    Serial.print( "NG2 type=" );
    Serial.println( result, DEC );
  }
}

void loop()
{
}

