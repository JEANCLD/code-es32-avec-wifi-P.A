#include <WiFi.h>
#include <ESPAsyncWebServer.h>
#include <AsyncTCP.h>
#include <EEPROM.h>
#include "esp_wifi.h"  // Ajout de cette bibliothèque pour gérer les clients WiFi

const char* ssid = "ESP32_AP";
const char* password = "123456789";

const int ledPin = 2;
bool lampState = false;

AsyncWebServer server(80);
AsyncWebSocket ws("/ws");

void saveStateToEEPROM() {
    EEPROM.begin(1);
    EEPROM.write(0, lampState);
    EEPROM.commit();
}

void loadStateFromEEPROM() {
    EEPROM.begin(1);
    lampState = EEPROM.read(0);
    digitalWrite(ledPin, lampState ? HIGH : LOW);
}

void notifyClients() {
    ws.textAll(lampState ? "ON" : "OFF");
}

void handleWebSocketMessage(void *arg, uint8_t *data, size_t len) {
    AwsFrameInfo *info = (AwsFrameInfo*)arg;
    if (info->final && info->index == 0 && info->len == len && info->opcode == WS_TEXT) {
        String message = String((char*)data).substring(0, len);
        if (message == "TOGGLE") {
            lampState = !lampState;
            digitalWrite(ledPin, lampState ? HIGH : LOW);
            saveStateToEEPROM();
            notifyClients();
        }
    }
}

void onWebSocketEvent(AsyncWebSocket *server, AsyncWebSocketClient *client, AwsEventType type, void *arg, uint8_t *data, size_t len) {
    if (type == WS_EVT_CONNECT) {
        Serial.printf("Nouvelle connexion: ID=%u, IP=%s\n", client->id(), client->remoteIP().toString().c_str());
        client->text(lampState ? "ON" : "OFF");
        listConnectedClients(); // Afficher les clients connectés
    } else if (type == WS_EVT_DATA) {
        handleWebSocketMessage(arg, data, len);
    } else if (type == WS_EVT_DISCONNECT) {
        Serial.printf("Client déconnecté: ID=%u\n", client->id());
    }
}

// Fonction pour afficher les IP et MAC des clients connectés
void listConnectedClients() {
    wifi_sta_list_t stationList;
    esp_wifi_ap_get_sta_list(&stationList);

    Serial.println("Liste des appareils connectés :");
    for (int i = 0; i < stationList.num; i++) {
        wifi_sta_info_t station = stationList.sta[i];
        Serial.printf("Client %d - MAC: %02X:%02X:%02X:%02X:%02X:%02X\n",
                      i + 1,
                      station.mac[0], station.mac[1], station.mac[2],
                      station.mac[3], station.mac[4], station.mac[5]);
    }
}

void setup() {
    Serial.begin(115200);
    pinMode(ledPin, OUTPUT);
    loadStateFromEEPROM();

    WiFi.softAP(ssid, password);
    Serial.println("Point d'accès créé");
    Serial.print("Adresse IP ESP32: ");
    Serial.println(WiFi.softAPIP());
    Serial.print("Adresse MAC ESP32: ");
    Serial.println(WiFi.softAPmacAddress());

    ws.onEvent(onWebSocketEvent);
    server.addHandler(&ws);
    server.begin();
}

void loop() {
    ws.cleanupClients();
    listConnectedClients(); // Affichage toutes les 5 secondes
    delay(5000);
} 




 package com.example.myweb;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {
    private ImageView lampButton;
    private static final String ESP32_WS_URL = "ws://192.168.4.1/ws";
    private WebSocket webSocket;
    private boolean lampState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lampButton = findViewById(R.id.lampButton);
        lampButton.setOnClickListener(v -> toggleLamp());

        connectWebSocket();
    }

    private void connectWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(ESP32_WS_URL).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connecté à l'ESP32", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> updateUI(text));
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                runOnUiThread(() -> updateUI(bytes.utf8()));
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void toggleLamp() {
        if (webSocket != null) {
            webSocket.send("TOGGLE");
        }
    }

    private void updateUI(String state) {
        lampState = state.equals("ON");
        lampButton.setImageResource(lampState ? R.drawable.ampoule_on : R.drawable.ampoule_off);
        Toast.makeText(this, lampState ? "Lampe allumée" : "Lampe éteinte", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity fermée");
        }
    }
}   