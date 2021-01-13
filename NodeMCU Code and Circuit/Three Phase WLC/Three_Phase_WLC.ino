//--------------------********** Smart Water Level Detector and Controller for Three Phase **********--------------------
//BY Sampad Hegde


#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>


#define SERVER_IP "192.168.1.5:8000"
#ifndef STASSID
#define STASSID "Havyaka_2.4"
#define STAPSK  "qd8qqnfe"
#endif


//Function Declarations

int getStatus(int , int , int);
int overHeadStatus();
int underGroundStatus();
void sendDataToServer(int , int , int);
String buildJSON(int , int , int);
void turnOffPump();
void turnOnPump();


//Pin declaration
int overhead_low = D0; //gpio 16
int overhead_mid = D1; //gpio 5
int overhead_high = D2; //gpio 4

int underground_low = D5; //gpio 14
int underground_mid = D6; //gpio 13
int underground_high = D7; //gpio 12

int on_relay_pin = 10; //sd3
int off_relay_pin = 9; //sd2

int pump_status=0;

int ol,om,oh,ul,um,uh,olevel,ulevel;

int Emergency_Stop = 0;
int Force_Stop = 0;

StaticJsonDocument<200> doc;


void setup()
{

  Serial.begin(115200);
  pinMode(overhead_low , INPUT);
  pinMode(overhead_mid , INPUT);
  pinMode(overhead_high , INPUT);

  pinMode(underground_low , INPUT);
  pinMode(underground_mid , INPUT);
  pinMode(underground_high , INPUT);

  pinMode(on_relay_pin , OUTPUT);
  pinMode(off_relay_pin , OUTPUT);

  WiFi.begin(STASSID, STAPSK);
  while (WiFi.status() != WL_CONNECTED) 
  {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println("");
  Serial.print("Connected! IP address: ");
  Serial.println(WiFi.localIP());
}


void loop()
{
  if(Emergency_Stop == 0)
  {
    if (Force_Stop == 0)
    {
      olevel=overHeadStatus();
      Serial.print("\t");
      ulevel=underGroundStatus();
      
      Serial.print("\tPump Satus : ");
      Serial.print(pump_status);
      Serial.print("\n");
    
      Serial.println("\nSending Data To Server");
      sendDataToServer(olevel,ulevel,pump_status);
    }
    else
    {
      turnOffPump();
      Serial.println("Force stop");
      Serial.println("\nSending Data To Server");
      sendDataToServer(olevel,ulevel,pump_status);
    }
    
  }
  else
  {
    turnOffPump();
    Serial.println("Emergency stop");
    Serial.println("\nSending Data To Server");
    sendDataToServer(olevel,ulevel,pump_status);
  }
  
}


void turnOffPump()
{
    digitalWrite(off_relay_pin , HIGH);
    delay(500);
    digitalWrite(off_relay_pin,LOW);
    pump_status = 0;
}
void turnOnPump()
{
    digitalWrite(on_relay_pin , HIGH);
    delay(500);
    digitalWrite(on_relay_pin , LOW);
    pump_status = 1;
}

int overHeadStatus()
{
  ol = digitalRead(overhead_low);
  om = digitalRead(overhead_mid);
  oh = digitalRead(overhead_high);

  Serial.print("OverHead ");
  int returnStatus=getStatus(ol,om,oh);

  
  if (returnStatus == 0)
  {
    turnOnPump();
  }
  if (returnStatus == 3)
  {
    turnOffPump();
  }
  
  return returnStatus;
}

int underGroundStatus()
{
  ul = digitalRead(underground_low);
  um = digitalRead(underground_mid);
  uh = digitalRead(underground_high);

  Serial.print("Underground ");
  int returnStatus=getStatus(ul,um,uh);
  
  if (returnStatus == 0)
  {
    turnOffPump();
  }
  return returnStatus;
}

int getStatus(int low , int mid , int high)
{
  if(high == HIGH )
  {
    Serial.print(" status : High");
    return 3;
  }
  else if(mid == HIGH)
  {
    Serial.print(" status : Mid");
    return 2;
    
  }
  else if(low == HIGH)
  {
    Serial.print(" status : LOW");
    return 1;
  }
  else
  {
    Serial.print(" status : !! Very LOW !!");
    return 0;
  }
}

void sendDataToServer(int olevel , int ulevel , int pump_stat)
{
  if ((WiFi.status() == WL_CONNECTED)) 
  {

    WiFiClient client;
    HTTPClient http;
    
    Serial.print("[HTTP] begin...\n");
    http.begin(client, "http://" SERVER_IP "/getdata");
    http.addHeader("Content-Type", "application/json");

    Serial.print("[HTTP] POST...\n");

    
    int httpCode = http.POST(buildJSON(olevel , ulevel , pump_stat));

    if (httpCode > 0) 
    {
      Serial.printf("[HTTP] POST... code: %d\n", httpCode);

      if (httpCode == HTTP_CODE_OK) 
      {
        const String& payload = http.getString();
        Serial.print("received payload:\n<<");
        Serial.println(payload);
        DeserializationError error = deserializeJson(doc, payload);
        if(!error)
        {
          Serial.print("Emergency Stop : ");
          Serial.print((int)doc["Emergency_Stop"]);
          if((int)doc["Emergency_Stop"] == 1)
          {
            Emergency_Stop = (int)doc["Emergency_Stop"];
          }
          Serial.print("\t\tForce Stop : ");
          Serial.println((int)doc["Force_Stop"]);
          Force_Stop =(int)doc["Force_Stop"];
        }
        Serial.println(">>");
      }
    } 
    else 
    {
      Serial.printf("[HTTP] POST... failed, error: %s\n", http.errorToString(httpCode).c_str());
    }

    http.end();
  }
  else
  {
    Serial.println("\n\n!!!!! WiFi Not Connected !!!!!!\n");
  }
}
String buildJSON(int olevel , int ulevel , int pump_stat)
{
  String str ="{\"pumpstatus\":";
  str = str + pump_stat ;
  str = str + "," ;
  str = str + "\"overheadlevel\":";
  str = str + olevel;
  str = str + "," ;
  str = str + "\"undergroundlevel\":" ;
  str = str + ulevel ;
  str = str + "}" ;

  return str;
}
