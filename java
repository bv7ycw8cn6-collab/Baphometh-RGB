#include <WiFi.h>
#include <WebServer.h>

const char* ssid = "PakoSystems";
const char* password = "feed1234";

WebServer server(80);

const int rgbPin = 8; 

int currentR = 0;
int currentG = 0;
int currentB = 0;
int ledMode = 0; 

unsigned long previousMillis = 0;
const long blinkInterval = 500;
const long fadeInterval = 20;   
long fireInterval = 50;         

bool blinkState = false;
int fadeBrightness = 0;
int fadeDirection = 5;

void handleRoot() {
  String html = "<!DOCTYPE html><html><head>";
  html += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">";
  html += "<meta charset=\"UTF-8\"><title>Baphomet RGB</title>";
  html += "<style>";
  html += "body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #121212; color: #ffffff; text-align: center; margin: 0; padding: 20px; padding-bottom: 60px; }";
  html += ".container { max-width: 400px; margin: 30px auto; background-color: #1e1e1e; padding: 30px; border-radius: 20px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }";
  
  html += ".logo-container { display: flex; align-items: center; justify-content: center; gap: 15px; margin-bottom: 25px; position: relative; padding-top: 50px; animation: infernoFire 0.15s infinite alternate; }";
  html += ".logo-icon { width: 44px; height: 44px; fill: none; stroke: #ff3333; stroke-width: 1.2; filter: drop-shadow(0 0 8px rgba(255, 51, 51, 0.6)); z-index: 2; }";
  html += "h1 { color: #ff3333; font-size: 28px; margin: 0; font-weight: 900; text-transform: uppercase; letter-spacing: 3px; filter: drop-shadow(0 0 8px rgba(255, 51, 51, 0.6)); z-index: 2; }";
  
  html += ".fire-wrapper { position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 220px; height: 90px; pointer-events: none; overflow: hidden; }";
  html += ".flame { position: absolute; bottom: -10px; width: 30px; height: 30px; background: #ff5722; border-radius: 0 100% 100% 100%; transform: rotate(-45deg); mix-blend-mode: screen; animation: riseUp 0.8s infinite linear, flicker 0.1s infinite alternate; opacity: 0; }";
  
  html += ".flame:nth-child(1) { left: 20%; width: 35px; height: 35px; animation-duration: 0.7s; background: #ff3333; }";
  html += ".flame:nth-child(2) { left: 35%; width: 45px; height: 45px; animation-duration: 0.6s; animation-delay: 0.15s; background: #ff9800; }";
  html += ".flame:nth-child(3) { left: 50%; width: 50px; height: 50px; animation-duration: 0.5s; animation-delay: 0.3s; background: #ffeb3b; }"; 
  html += ".flame:nth-child(4) { left: 65%; width: 38px; height: 38px; animation-duration: 0.65s; animation-delay: 0.1s; background: #ff5722; }";
  html += ".flame:nth-child(5) { left: 78%; width: 28px; height: 25px; animation-duration: 0.75s; animation-delay: 0.4s; background: #ff3333; }";
  
  html += "@keyframes riseUp { 0% { transform: translateY(40px) rotate(-45deg) scale(0.5); opacity: 0; } 20% { opacity: 0.9; } 100% { transform: translateY(-50px) rotate(-45deg) scale(0); opacity: 0; } }";
  html += "@keyframes flicker { 0% { margin-left: -3px; filter: blur(1px); } 100% { margin-left: 3px; filter: blur(2px); } }";
  
  html += "p { color: #888; font-size: 14px; margin-top: 5px; margin-bottom: 25px; }";
  html += ".color-preview { width: 60px; height: 60px; border-radius: 50%; margin: 10px auto 25px auto; border: 3px solid #fff; box-shadow: 0 4px 10px rgba(0,0,0,0.3); transition: background-color 0.3s; }";
  html += ".picker-box { margin: 20px auto; }";
  html += "input[type='color'] { -webkit-appearance: none; border: none; width: 150px; height: 150px; border-radius: 50%; cursor: pointer; background: none; }";
  html += "input[type='color']::-webkit-color-swatch-wrapper { padding: 0; }";
  html += "input[type='color']::-webkit-color-swatch { border: 4px solid #333; border-radius: 50%; box-shadow: 0 4px 15px rgba(0,0,0,0.5); }";
  html += ".btn { display: block; width: 85%; margin: 15px auto; padding: 12px; font-size: 16px; font-weight: bold; color: #fff; border: none; border-radius: 12px; cursor: pointer; text-decoration: none; text-transform: uppercase; letter-spacing: 1px; transition: 0.2s; background: none; }";
  html += ".btn-blink { background-color: #007bff; box-shadow: 0 4px 15px rgba(0,123,255,0.4); }";
  html += ".btn-fade { background-color: #28a745; box-shadow: 0 4px 15px rgba(40,167,69,0.4); }";
  html += ".btn-fire { background-color: #ff5722; box-shadow: 0 4px 15px rgba(255,87,34,0.4); }";
  html += ".btn-off { background-color: #f44336; box-shadow: 0 4px 15px rgba(244,67,54,0.4); margin-top: 25px; font-size: 20px; padding: 15px; }";
  html += ".btn:active { transform: scale(0.98); }";
  
  // --- NOVÉ CSS PRO PODPIS VOID 1.1 V PRAVÉM DOLNÍM ROHU ---
  html += ".signature { position: fixed; bottom: 15px; right: 20px; font-size: 11px; font-weight: 600; color: #555; text-transform: uppercase; letter-spacing: 2px; font-family: monospace; transition: color 0.3s, filter 0.3s; pointer-events: auto; }";
  html += ".signature:hover { color: #ff3333; filter: drop-shadow(0 0 4px rgba(255, 51, 51, 0.8)); }";
  
  html += "#preloader { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: #121212; display: flex; justify-content: center; align-items: center; z-index: 9999; transition: opacity 0.4s ease, visibility 0.4s; }";
  html += ".preload-content { display: flex; flex-direction: column; align-items: center; animation: scaleUpAll 1.2s forwards cubic-bezier(0.6, -0.28, 0.735, 0.045); }";
  
  html += ".preload-logo { width: 140px; height: 140px; fill: none; stroke: #ff3333; stroke-width: 1.2; animation: pulseGlow 0.4s infinite alternate, infernoFire 0.12s infinite alternate; }";
  html += ".preload-title { color: #ff3333; font-size: 36px; font-weight: 900; text-transform: uppercase; margin-top: 25px; letter-spacing: 4px; filter: drop-shadow(0 0 8px rgba(255, 51, 51, 0.6)); }";
  
  html += "@keyframes scaleUpAll { 0% { transform: scale(1); opacity: 1; } 70% { transform: scale(1.1); opacity: 1; } 100% { transform: scale(6); opacity: 0; } }";
  html += "@keyframes pulseGlow { from { filter: drop-shadow(0 0 10px rgba(255, 51, 51, 0.7)); } to { filter: drop-shadow(0 0 40px rgba(255, 69, 0, 1)); } }";
  html += "@keyframes infernoFire { 0% { transform: skewX(-1.5deg) scaleY(1); } 100% { transform: skewX(1.5deg) scaleY(1.04); } }";
  html += "</style>";
  html += "<script>";
  html += "window.addEventListener('load', function() {";
  html += "  setTimeout(function() {";
  html += "    var preloader = document.getElementById('preloader');";
  html += "    if(preloader) { preloader.style.opacity = '0'; preloader.style.visibility = 'hidden'; }";
  html += "  }, 1200);"; 
  html += "});";
  html += "function sendColor(hexColor) {";
  html += "  var r = parseInt(hexColor.substr(1,2), 16);";
  html += "  var g = parseInt(hexColor.substr(3,2), 16);";
  html += "  var b = parseInt(hexColor.substr(5,2), 16);";
  html += "  document.getElementById('preview').style.backgroundColor = hexColor;";
  html += "  var xhr = new XMLHttpRequest();";
  html += "  xhr.open('GET', '/set-color?r=' + r + '&g=' + g + '&b=' + b, true);";
  html += "  xhr.send();";
  html += "}";
  html += "function triggerEffect(url) {";
  html += "  var xhr = new XMLHttpRequest();";
  html += "  xhr.open('GET', url, true);";
  html += "  xhr.send();";
  html += "}";
  html += "</script>";
  html += "</head><body>";
  
  html += "<div id='preloader'>";
  html += "  <div class='preload-content'>";
  html += "    <svg class='preload-logo' viewBox='0 0 24 24'>";
  html += "      <circle cx='12' cy='12' r='11' stroke='#ff3333' stroke-width='0.8' />";
  html += "      <path d='M12,22L15.5,13.5L23.5,13.5L16.8,8.5L19.5,0.5L12,6L4.5,0.5L7.2,8.5L0.5,13.5L8.5,13.5Z'/>";
  html += "      <path d='M12,6V12 M9.5,9.5C8,8.5 7,6.5 7.5,4 M14.5,9.5C16,8.5 17,6.5 16.5,4 M9,13C8.5,14 8,16 6.5,16.5 M15,13C15.5,14 16,16 17.5,16.5 M10.5,15.5L12,19.5L13.5,15.5' />";
  html += "    </svg>";
  html += "    <div class='preload-title'>Baphomet</div>";
  html += "  </div>";
  html += "</div>";

html += "<div class='container'>";
  html += "<div class='logo-container'>";
  html += "  <div class='fire-wrapper'>";
  html += "    <div class='flame'></div><div class='flame'></div><div class='flame'></div><div class='flame'></div><div class='flame'></div>";
  html += "  </div>";
  
  html += "<svg class='logo-icon' viewBox='0 0 24 24'>";
  html += "  <circle cx='12' cy='12' r='11' stroke='#ff3333' stroke-width='0.8' />";
  html += "  <path d='M12,22L15.5,13.5L23.5,13.5L16.8,8.5L19.5,0.5L12,6L4.5,0.5L7.2,8.5L0.5,13.5L8.5,13.5Z'/>";
  html += "  <path d='M12,6V12 M9.5,9.5C8,8.5 7,6.5 7.5,4 M14.5,9.5C16,8.5 17,6.5 16.5,4 M9,13C8.5,14 8,16 6.5,16.5 M15,13C15.5,14 16,16 17.5,16.5 M10.5,15.5L12,19.5L13.5,15.5' />";
  html += "</svg>";
  html += "<h1>Baphomet</h1>";
  html += "</div>";
  html += "<p>RGB jsrpt</p>";
  String hexStr = "#";
  if (currentR < 16) hexStr += "0";
  hexStr += String(currentR, HEX);
  if (currentG < 16) hexStr += "0";
  hexStr += String(currentG, HEX);
  if (currentB < 16) hexStr += "0";
  hexStr += String(currentB, HEX);
  hexStr.toUpperCase();

  html += "<div id='preview' class='color-preview' style='background-color: " + hexStr + ";'></div>";
  html += "<div class='picker-box'>";
  html += "  <input type='color' id='colorPicker' value='" + hexStr + "' onchange='sendColor(this.value)' oninput='sendColor(this.value)'>";
  html += "</div>";
  
  html += "<button onclick=\"triggerEffect('/effect/fire')\" class='btn btn-fire'>Oheň</button>";
  html += "<button onclick=\"triggerEffect('/effect/blink')\" class='btn btn-blink'>Blikání</button>";
  html += "<button onclick=\"triggerEffect('/effect/fade')\" class='btn btn-fade'>Fade</button>";
  html += "<button onclick=\"triggerEffect('/led/off')\" class='btn btn-off'>Zhasnout</button>";
  html += "</div>"; // Konec .container
  
  // --- NOVÉ HTML: Vložení samotného nápisu Void 1.1 ---
  html += "<div class='signature'>Void 1.1</div>";
  
  html += "</body></html>";
  server.send(200, "text/html", html);
}

void handleSetColor() {
  if (server.hasArg("r") && server.hasArg("g") && server.hasArg("b")) {
    currentR = server.arg("r").toInt();
    currentG = server.arg("g").toInt();
    currentB = server.arg("b").toInt();
    ledMode = 0; 
    rgbLedWrite(rgbPin, currentR, currentG, currentB);
    server.send(200, "text/plain", "OK");
  } else {
    server.send(400, "text/plain", "Bad Request");
  }
}


void handleBlinkEffect() {
  ledMode = 1; 
  previousMillis = millis(); 
  blinkState = false;        
  if (currentR == 0 && currentG == 0 && currentB == 0) {
    currentR = 255; currentG = 255; currentB = 255;
  }
  server.send(200, "text/plain", "OK");
}

void handleFadeEffect() {
  ledMode = 2; 
  previousMillis = millis(); 
  fadeBrightness = 0;        
  fadeDirection = 5;         
  if (currentR == 0 && currentG == 0 && currentB == 0) {
    currentR = 255; currentG = 255; currentB = 255;
  }
  server.send(200, "text/plain", "OK");
}

void handleFireEffect() {
  ledMode = 3; 
  previousMillis = millis();
  if (currentR == 0 && currentG == 0 && currentB == 0) {
    currentR = 255; currentG = 255; currentB = 255;
  }
  server.send(200, "text/plain", "OK");
}

void handleLedOff() { 
  ledMode = 0; 
  currentR = 0; currentG = 0; currentB = 0;
  rgbLedWrite(rgbPin, 0, 0, 0); 
  server.send(200, "text/plain", "OK");
}

void setup() {
  Serial.begin(115200);
  rgbLedWrite(rgbPin, 0, 0, 0); 
  WiFi.disconnect(true);
  WiFi.persistent(false);
  delay(500);
  WiFi.mode(WIFI_AP);
  delay(100);
  WiFi.softAP(ssid, password); 
  server.on("/", handleRoot);
  server.on("/set-color", handleSetColor);
  server.on("/effect/blink", handleBlinkEffect);
  server.on("/effect/fade", handleFadeEffect);
  server.on("/effect/fire", handleFireEffect);
  server.on("/led/off", handleLedOff);
  server.begin();
}

void loop() {
  server.handleClient();
  unsigned long currentMillis = millis();
  
  if (ledMode == 1) {
    if (currentMillis - previousMillis >= blinkInterval) {
      previousMillis = currentMillis;
      blinkState = !blinkState;
      if (blinkState) {
        rgbLedWrite(rgbPin, currentR, currentG, currentB);
      } else {
        rgbLedWrite(rgbPin, 0, 0, 0);
      }
    }
  }
  else if (ledMode == 2) {
    if (currentMillis - previousMillis >= fadeInterval) {
      previousMillis = currentMillis;
      fadeBrightness += fadeDirection;
      if (fadeBrightness <= 0 || fadeBrightness >= 255) {
        fadeDirection = -fadeDirection;
      }
      int r = (currentR * fadeBrightness) / 255;
      int g = (currentG * fadeBrightness) / 255;
      int b = (currentB * fadeBrightness) / 255;
      rgbLedWrite(rgbPin, r, g, b);
    }
  }
  else if (ledMode == 3) {
    if (currentMillis - previousMillis >= fireInterval) {
      previousMillis = currentMillis;
      int r = random(180, 256); 
      int g = random(20, 90);   
      int b = random(0, 10);    
      rgbLedWrite(rgbPin, r, g, b);
      fireInterval = random(30, 110); 
    }
  }
}
