//シリアル通信ライブラリを取り入れる
import processing.serial.*;
//ポートのインスタンス
Serial port;

void setup(){
  
  //シリアルポート設定
  //port = new Serial(this,"/dev/tty.FireFly-1686-SPP-1",9600);
  //port = new Serial(this,"COM40",9600); // COM40 = BlueSMiRF
  //port = new Serial(this,"COM7",9600); // COM7 = MicroEDR1X
  port = new Serial(this,"COM3",9600); // COM3 = USB
  //println(port.list());
  
  //念のためバッファを空にする
  //port.clear();
  //「10」(ラインフィード)が来る度に
  //serialEvent()を発動させる
  port.bufferUntil(10); 
}

void draw(){
  //特になし  
  String str = "00003\n";
  port.write(str);
  println(str);
  delay(3000);

  str = "99999\n";
  port.write(str);
  println(str);
  delay(3000);
}

void serialEvent(Serial p){
   //文字列の変数stringDataを用意し、
  //「10」(ラインフィード)が来るまで読み込む
  String stringData=trim(port.readStringUntil(10));
  println(stringData);
}
