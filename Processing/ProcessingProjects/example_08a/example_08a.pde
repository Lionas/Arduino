// Example 08A: 

import processing.serial.*;

String feed = "http://arduino-lionas.appspot.com/arduino";

int interval = 30;
int lastTime;

int love = 0;
int network = 0;
int arduino = 0;

int light = 512;

Serial port;
color c;
String cs;

String buffer = "";
PFont font;

void setup() {
  size(640,480);
  frameRate(10);
  font = loadFont("MS-PGothic-32.vlw");
  fill(255);
  textFont(font,32);
  
  println(Serial.list());
  String arduinoPort = Serial.list()[1];
  
  port = new Serial(this,arduinoPort,9600);
  
  lastTime = 0;
  fetchData();
}

void draw() {
  background(c);
  int n = (interval - ((millis() - lastTime)/1000));
  
  c = color(network,love,arduino);
  cs = "#" + hex(c,6);
  
  text("Arduino Networked Lamp", 10, 40);
  text("Reading feed:", 10, 100);
  text(feed, 10, 140);
  
  text("Next update in " + n + " seconds", 10, 450);
  text("network", 10, 200);
  text(" " + network, 130, 200);
  rect(200, 172, network, 28);
  
  text("love ", 10, 240);
  text(" " + love, 130, 240);
  rect(200, 212, love, 28);
  
  text("arduino ", 10, 280);
  text(" " + arduino, 130, 280);
  rect(200, 252, arduino, 28);
  
  text("sending", 10, 340);
  text(cs, 200, 340);
  text("light level", 10, 380);
  rect(200, 352, light/10.23, 28);
  
  if( n <= 0 ) {
    fetchData();
    lastTime = millis();
  }
  
  port.write(cs);
  
  if( port.available() > 0 ) {
    int inByte = port.read();
    if( inByte != 10 ) {  // 10 = LF
      buffer = buffer + char(inByte);
    }else{
      if( buffer.length() > 1 ) {
        buffer = buffer.substring(0,buffer.length() - 1);
        light = int(buffer);
        buffer = "";
        port.clear();
      }
    }
  }
}

void fetchData() {
  String data;
  String chunk;
  
  love = 0;
  network = 0;
  arduino = 0;
  
  try {
    URL url = new URL(feed);
    URLConnection conn = url.openConnection();
    conn.connect();
    
    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    while((data = in.readLine()) != null) {
      StringTokenizer st = new StringTokenizer(data,"\"<>,.()[] ");
      while(st.hasMoreTokens()) {
        chunk = st.nextToken().toLowerCase();
        if( chunk.indexOf("love") >= 0 ) love++;
        if( chunk.indexOf("network") >= 0 ) network++;
        if( chunk.indexOf("arduino") >= 0 ) arduino++;
      }
    }
    
    if( network > 64 ) network = 64;
    if( love > 64 ) love = 64;
    if( arduino > 64 ) arduino = 64;
    
    network = network * 4 * light / 1024;
    love = love * 4 * light / 1024;
    arduino = arduino * 4 * light / 1024;
  }
  catch(Exception ex) {
    ex.printStackTrace();
    System.out.println("ERROR: " + ex.getMessage());
  }
}
