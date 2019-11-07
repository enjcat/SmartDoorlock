#include <Servo.h>
#include <SPI.h>
#include <MFRC522.h>
#include <ESP8266WiFi.h>
#include <WebSocketClient.h>
#include <ESP8266WebServer.h>

#define SS_PIN 15
#define RST_PIN 0
#define Smoter 4 
//ID 선언
#define ID_0 0xF0
#define ID_1 0x31
#define ID_2 0x98
#define ID_3 0xA3

MFRC522 rfid(SS_PIN, RST_PIN); // RFID 라이브러를 사용
MFRC522::MIFARE_Key key;
Servo myservo;
ESP8266WebServer server(80);


const char* ssid = "Sim";
const char* password = "";
char path[] = "/";
char host[] = "192.168.1.180";
int port = 180;

byte nuidPICC[4];
int toggle = 0;
int speaker = 16;
int button = 2;
int led = 5;
int state = LOW;
int cnt = 0; // 3번 실패시 알림음

WebSocketClient webSocketClient;

// Use WiFiClient class to create TCP connections
WiFiClient client;


void handleON() {
  server.send(200, "text/plane", "Door Open!!");
  Serial.println("Doorlock Open !!");
  myservo.write(90);
  delay(1500);
  myservo.write(0);
}


String recv_data()
{
  String data;
  while (1) {
        webSocketClient.getData(data);
        if (data.length() > 0)
        {
          Serial.print("\nReceived data: ");
          Serial.println(data);
          return data;
        }
      }
}

void send_uid()
{
  Serial.print("UID 전송\n");
  for(int i=0; i<4; i++)
    webSocketClient.sendData(String(rfid.uid.uidByte[i]));
}


int handshake_web(char *host_addr, char *path_host)
{
  webSocketClient.path = path_host;
  webSocketClient.host = host_addr;
  if (webSocketClient.handshake(client)) {
    return 1;
  }
  else {
    return 0;
  }
}

int connect_web(char *host_addr, int port_num)
{
  // Connect to the websocket server
  if (client.connect(host_addr, port_num)) {
    return 1;
  }
  else {
    return 0;
  }
}

int door_ctrl(String rcv_data)
{
  int flag = 0;
  if (rcv_data == "1")
      {
        cnt = 0;
        Serial.println("Open");
        myservo.write(90);
        delay(1500);
        Serial.println("\Close");
        myservo.write(0);
        flag = 1;
      }
      else Serial.print("\n등록되지 않은 사용자 입니다.\n");
      return flag;
}

void setup() {
  pinMode(speaker, OUTPUT);
  pinMode(led, OUTPUT);
  pinMode(button, INPUT_PULLUP);//버튼 입력 설정
  attachInterrupt(digitalPinToInterrupt(button), interrupt, RISING);
  //인터럽트 설정(인터럽트할 핀, 인터럽트 핸들러 함수, 인터럽트 방식(모드))
  //인터럽트 모드에는 FALLING(HIGH->LOW), RISING(LOW->HIGH), 
  //CHANGE(변경시), LOW(신호가 LOW일 경우)

  Serial.begin(9600);
  myservo.attach(Smoter);
  SPI.begin(); // SPI 시작
  rfid.PCD_Init(); // RFID 모듈시작

  for (byte i = 0; i < 6; i++) { //키ID 초기화
    key.keyByte[i] = 0xFF;
  }
  //Serial.print(F("Using the following key:"));
  printHex(key.keyByte, MFRC522::MF_KEY_SIZE);

  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  server.on("/on", handleON);
  server.begin();
  

  Serial.println("도어락 시스템을 시작합니다.");

}

void loop() {
  server.handleClient();
  digitalWrite(led, state);
  if (!rfid.PICC_IsNewCardPresent()) //카드가 인식 되지 않았다면
    return;

  if (!rfid.PICC_ReadCardSerial()) // 카드의 ID가 인식 되지 않았다면
    return;

  


  Serial.print(F("PICC type: "));
  MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak); //카드의 타입을 읽어옴
  Serial.println(rfid.PICC_GetTypeName(piccType));

  // 감지한 ID의 방식이 MIFARE가 아니라면
  if (piccType != MFRC522::PICC_TYPE_MIFARE_MINI &&
    piccType != MFRC522::PICC_TYPE_MIFARE_1K &&
    piccType != MFRC522::PICC_TYPE_MIFARE_4K) {
    Serial.println(F("Your tag is not of type MIFARE Classic."));
    return;
  }
  Serial.println("");
  Serial.print("taging UID : ");
  printHex(rfid.uid.uidByte, rfid.uid.size);//태깅된 UID출력
  Serial.println("");

  // Connect to the websocket server
  if(connect_web(host, port))
  {
    Serial.println("connection successful");
  }
  else
  {
    Serial.println("connection failed.");
  }

  // Handshake with the server
  if(handshake_web(host, path))
  {
    Serial.println("handshake successful");
  }
  else
  {
    Serial.println("handshake failed.");
  }


  if (client.connected()) {
    if (state == LOW) { //문여는 알고리즘.
      webSocketClient.sendData(String(1));//1은 문연다는 신호
      send_uid();//uid 전송
      if(!door_ctrl(recv_data()))
        cnt++;
      if(cnt == 3)
      {
        for(int i=0; i<3; i++)
        {
          tone(speaker, 330);
          delay(300);
          tone(speaker, 262);
          delay(300);
        }
        noTone(speaker);
        cnt = 0;
      }
    }
    else { //사용자 등록 알고리즘.
      Serial.print("\n사용자를 등록합니다.");
      webSocketClient.sendData("2");
      cnt = 0;
      send_uid();//uid 전송
      
      if (recv_data() == "2")
        Serial.print("\n사용자가 성공적으로 등록되었습니다.\n");
      else
        Serial.print("\n사용자 등록에 실패하였습니다.\n");
      state = LOW;//사용자 등록 마친 후 LOW로 바꿔줘 일반 모드로 들어감.

    }
  }
  else {
    Serial.println("Client disconnected.");
//    while (1) {
//      // Hang on disconnect.
//    }
  }
  client.stop();
  rfid.PICC_HaltA();//종료
  rfid.PCD_StopCrypto1();//다시 시작
}

// 카드 ID를 16진수로 바꿔주는 함수
void printHex(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}
void interrupt() {
  state = !state;
}
