#include <Wire.h>
#include "RTClib.h"
 
#define CMD_SEND_BEGIN  "AT+CIPSEND=0"
#define CMD_SEND_END    "AT+CIPCLOSE=0"
 
#define STDIO_PROTOCOL_HTTP     80
#define STDIO_PROTOCOL_HTTPS    443
#define STDIO_PROTOCOL_FTP      21
#define STDIO_PROTOCOL_CURRENT  STDIO_PROTOCOL_HTTP
 
#define STDIO_CHAR_CR     0x0D
#define STDIO_CHAR_LF     0x0A
 
#define STDIO_STRING_EMPTY  ""
 
#define STDIO_DELAY_SEED  1000
#define STDIO_DELAY_1X    (1*STDIO_DELAY_SEED)
#define STDIO_DELAY_2X    (2*STDIO_DELAY_SEED)
#define STDIO_DELAY_3X    (3*STDIO_DELAY_SEED)
#define STDIO_DELAY_4X    (4*STDIO_DELAY_SEED)
#define STDIO_DELAY_5X    (5*STDIO_DELAY_SEED)

bool hasRequest = false;

int cambienas1 = 10; //chan 10 cam bien
int cambienas2 = 9;
int led = 13; // relay led
RTC_DS1307 rtc;
 
void setup()
{
  delay(STDIO_DELAY_5X);
  Serial.begin(115200);
  
  if (! rtc.begin())
  {
    //Serial.println("Couldn't find RTC");
    while (1);
  }
  if (!rtc.isrunning())
  {
    //Serial.println("RTC is NOT running!");
  }
  
  pinMode(cambienas1, INPUT);
  pinMode(cambienas2, INPUT);
  pinMode(led, OUTPUT);
  //digitalWrite(led, LOW);
  
  initESP8266();
}

void HienThiTG(DateTime future)
{
    Serial.print(future.day(), DEC);
    Serial.print(":");
    Serial.print(future.month(), DEC);
    Serial.print(":");
    Serial.print(future.year(), DEC);
    Serial.print(" ");
    Serial.print(future.hour(), DEC);
    Serial.print(":");
    Serial.print(future.minute(), DEC);
    Serial.print(":");
    Serial.print(future.second(), DEC);
    Serial.println();
    
    delay(1000);    
}

void CamBien()
{
  int value1 = digitalRead(cambienas1);
  int value2 = digitalRead(cambienas2);
  //Serial.println((value1+value2)/2);
  if((value1 == 1)&&(value2 == 1)) // Troi Toi
  {
    digitalWrite(led, HIGH);
    //Serial.println("Den Bat");
  }
  else  if((value1 == 0)&&(value2 == 0))
    {
      digitalWrite(led, LOW);
      //Serial.println("Den Tat"); 
    }
    delay(100);
    
}

void TatBatDen(DateTime future)
{  
    if(future.hour()>=5 && future.hour()<18)
    {
      digitalWrite(led, LOW);
      //Serial.println("Den Tat");
    }
    else
    {
      digitalWrite(led, HIGH);
      //Serial.println("Den Bat");
    }

}

int dayspan = 2;
int hourspan = 2;
int minutespan = 2;
int secondspan = 2;

bool isChange=0;
int tYear=0, tMonth=0, tDay=0, tHour=0, tMinute=0, tSecond=0;

void loop()
{
  DateTime now = rtc.now();
  dayspan = countNgay(now.day(), now.month(), now.year(), tDay, tMonth, tYear);
  if(isChange == 1)
    changeTime(now);
  isChange=0;
  DateTime future (now + TimeSpan(dayspan,hourspan,minutespan,secondspan));

  //HienThiTG(future);
  if(future.hour()>=5 && future.hour()<18)
  {
    //Serial.println("HELLO");
    if(future.hour()<=6 ||future.hour() >= 17)
      CamBien();
    else
      TatBatDen(future);
  }
  else
  {
    //Serial.println("HELLO");
    TatBatDen(future);
  }
  
  while(Serial.available())
  {   
    bufferingRequest(Serial.read());
  }
  
  if(hasRequest == true) 
  {    
    String dates = (String)future.day()+":"+(String)future.month()+":"+(String)future.year()+" "+(String)future.hour()+":"+(String)future.minute();
    String htmlResponse = "<!doctype html>"
    "<html>"
      "<head>"
        "<title>SMART TRAFFIC LIGHT</title>"
      "</head>"
      "<body>"
        "<h1>"+dates+"</h1>"
      "</body>"
     "</html>";

    String beginSendCmd = String(CMD_SEND_BEGIN) + "," + htmlResponse.length();
    deliverMessage(beginSendCmd, STDIO_DELAY_1X);
    deliverMessage(htmlResponse, STDIO_DELAY_1X);
    deliverMessage(CMD_SEND_END, STDIO_DELAY_1X);

    hasRequest = false;
  }
}
 
void initESP8266()
{
  deliverMessage("AT+RST", STDIO_DELAY_2X);
  deliverMessage("AT+CWMODE=2", STDIO_DELAY_3X);
  deliverMessage("AT+CWSAP=\"Smart Traffic Light\",\"12345678\",1,4", STDIO_DELAY_3X);
  deliverMessage("AT+CIFSR", STDIO_DELAY_1X);
  deliverMessage("AT+CIPMUX=1", STDIO_DELAY_1X);
  deliverMessage(String("AT+CIPSERVER=1,") + STDIO_PROTOCOL_CURRENT, STDIO_DELAY_1X);  
}
 
void bufferingRequest(char c)
{
  static String bufferData = STDIO_STRING_EMPTY;
 
  switch (c)
  {
    case STDIO_CHAR_CR:
      break;
    case STDIO_CHAR_LF:
    {
      STDIOProcedure(bufferData);
      bufferData = STDIO_STRING_EMPTY;
    }
      break;
    default:
      bufferData += c;
  }
} 
 
void STDIOProcedure(const String& command)
{ 
  hasRequest = command.startsWith("+IPD,");

  int pos = command.indexOf("timer");
  if(pos != -1)
  { 
    isChange= 1;
    tMinute = (int)(command[pos-2]-'0')*10 + (int)(command[pos-1]-'0');
    tHour   = (int)(command[pos-4]-'0')*10 + (int)(command[pos-3]-'0');
    tDay    = (int)(command[pos-6]-'0')*10 + (int)(command[pos-5]-'0');
    tMonth  = (int)(command[pos-8]-'0')*10 + (int)(command[pos-7]-'0');
    tYear   = (int)(command[pos-12]-'0')*1000 + (int)(command[pos-11]-'0')*100 + (int)(command[pos-10]-'0')*10 + (int)(command[pos-9]-'0');
  }
}
 
void deliverMessage(const String& msg, int dt)
{
  Serial.println(msg);
  delay(dt);
}

bool isNamNhuan(int tYear){
  if(tYear%100==0)
    return (tYear%400==0)?1:0;
  else
    return (tYear%4==0)?1:0;
}

long countNgay(int ngQK, int tQK, int nQK, int ngTL, int tTL, int nTL){
  int ngayTh[13] = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
  long ngay=0;
  
  for(int i=nQK+1; i<nTL; i++)
    (isNamNhuan(i))?ngay+=366:ngay+=365;

  for(int i=tQK+1; i<=12; i++)
    (i==2&&isNamNhuan(nQK))?ngay+=ngayTh[i]:ngay+=ngayTh[i];
  ngay+=31-ngQK;
  
  for(int i=1; i<tTL; i++)
    (i==2&&isNamNhuan(nTL))?ngay+=ngayTh[i]:ngay+=ngayTh[i];
  ngay+=ngTL;
  
  return ngay+1;
}

void changeTime(DateTime timer){
  if(tHour>timer.hour()){
    hourspan = tHour-timer.hour();
  }else{
    hourspan = -(timer.hour()-tHour);
  }
  
  if(tMinute>timer.minute()){
    minutespan = tMinute-timer.minute();
  }else{
    minutespan = -(timer.minute()-tMinute);
  }
}
