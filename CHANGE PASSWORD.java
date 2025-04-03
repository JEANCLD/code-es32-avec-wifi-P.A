#include <WiFi.h>
#include <ESPAsyncWebServer.h>
#include <AsyncTCP.h>
#include <EEPROM.h>
#include "esp_wifi.h"

char ssid[32] = "ESP32_AP";
char password[32] = "123456789";
const int ledPin = 2;
bool lampState = false;

AsyncWebServer server(80);
AsyncWebSocket ws("/ws");

void saveWiFiConfig() {
    EEPROM.begin(64);
    EEPROM.put(0, ssid);
    EEPROM.put(32, password);
    EEPROM.commit();
}

void loadWiFiConfig() {
    EEPROM.begin(64);
    EEPROM.get(0, ssid);
    EEPROM.get(32, password);
    if (strlen(ssid) == 0 || strlen(password) == 0) {
        strcpy(ssid, "ESP32_AP");
        strcpy(password, "123456789");
    }
}

void restartAP() {
    WiFi.softAPdisconnect(true);
    WiFi.softAP(ssid, password);
}

void handleWebSocketMessage(void *arg, uint8_t *data, size_t len) {
    AwsFrameInfo *info = (AwsFrameInfo*)arg;
    if (info->opcode == WS_TEXT) {
        String message = String((char*)data).substring(0, len);
        if (message == "TOGGLE") {
            lampState = !lampState;
            digitalWrite(ledPin, lampState ? HIGH : LOW);
            ws.textAll(lampState ? "ON" : "OFF");
        } else if (message.startsWith("SET_WIFI")) {
            sscanf(message.c_str(), "SET_WIFI %s %s", ssid, password);
            saveWiFiConfig();
            restartAP();
        }
    }
}

void onWebSocketEvent(AsyncWebSocket *server, AsyncWebSocketClient *client, AwsEventType type, void *arg, uint8_t *data, size_t len) {
    if (type == WS_EVT_CONNECT) {
        client->text(lampState ? "ON" : "OFF");
    } else if (type == WS_EVT_DATA) {
        handleWebSocketMessage(arg, data, len);
    }
}

void setup() {
    Serial.begin(115200);
    pinMode(ledPin, OUTPUT);
    loadWiFiConfig();
    WiFi.softAP(ssid, password);
    ws.onEvent(onWebSocketEvent);
    server.addHandler(&ws);
    server.begin();
}

void loop() {
    ws.cleanupClients();
}



package com.example.myweb;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_password) {
            startActivity(new Intent(this, ChangeMotdePasseMainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


package com.example.myweb;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChangeMotdePasseMainActivity extends AppCompatActivity {
    private EditText ssidInput, passwordInput;
    private Button saveButton;
    private WebSocket webSocket;
    private static final String ESP32_WS_URL = "ws://192.168.4.1/ws";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_motde_passe_main);

        ssidInput = findViewById(R.id.ssidInput);
        passwordInput = findViewById(R.id.passwordInput);
        saveButton = findViewById(R.id.saveButton);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(ESP32_WS_URL).build();
        webSocket = client.newWebSocket(request, new okhttp3.WebSocketListener() {});

        saveButton.setOnClickListener(v -> changeWiFi());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Activer la flèche de retour
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Afficher la flèche de retour
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.sharp_chevron_left_24); // Définir l'icône de retour
        }
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Vérifier si l'utilisateur clique sur la flèche de retour
        if (id == android.R.id.home) {
            onBackPressed(); // Revenir à MainActivity
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void changeWiFi() {
        String newSSID = ssidInput.getText().toString().trim();
        String newPassword = passwordInput.getText().toString().trim();
        if (!newSSID.isEmpty() && !newPassword.isEmpty()) {
            webSocket.send("SET_WIFI " + newSSID + " " + newPassword);
            Toast.makeText(this, "WiFi mis à jour!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Veuillez remplir tous les champs!", Toast.LENGTH_SHORT).show();
        }
    }
}
