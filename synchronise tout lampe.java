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

public class AjouterLampMainActivity extends AppCompatActivity {
    private EditText lampNameInput;
    private Button addLampButton;
    private WebSocket webSocket;
    private static final String ESP32_WS_URL = "ws://192.168.4.1/ws";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_lamp_main);

        lampNameInput = findViewById(R.id.lampNameInput);
        addLampButton = findViewById(R.id.addLampButton);

        connectWebSocket();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Activer la flèche de retour
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Afficher la flèche de retour
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.sharp_chevron_left_24); // Définir l'icône de retour
        }
        addLampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lampName = lampNameInput.getText().toString().trim();
                if (!lampName.isEmpty()) {
                    addLamp(lampName);
                } else {
                    Toast.makeText(AjouterLampMainActivity.this, "Veuillez entrer un nom de lampe", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private void connectWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(ESP32_WS_URL).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {});
    }

    private void addLamp(String lampName) {
        if (webSocket != null) {
            webSocket.send("ADD_LAMP " + lampName);
            Toast.makeText(this, "Lampe ajoutée: " + lampName, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}


package com.example.myweb;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {
    private static final String ESP32_WS_URL = "ws://192.168.4.1/ws";
    private WebSocket webSocket;
    private ImageView[] lamps = new ImageView[16];
    private boolean[] lampStates = new boolean[16];
    // Inflater le menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // Charger le menu XML
        return true;
    }

    // Gérer les clics sur les items du menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Vérifier l'élément du menu sélectionné et lancer l'activité correspondante
        if (id == R.id.change_password) {
            Intent passwordIntent = new Intent(this, ChangeMotdePasseMainActivity.class);
            startActivity(passwordIntent);
            return true;
        } else if (id == R.id.add_lamp) {
            // Lancer l'activité pour ajouter une lampe
            Intent addLampIntent = new Intent(this, AjouterLampMainActivity.class);
            startActivity(addLampIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i = 0; i < 16; i++) {
            int resID = getResources().getIdentifier("lamp_" + i, "id", getPackageName());
            lamps[i] = findViewById(resID);
            int finalI = i;
            lamps[i].setOnClickListener(v -> toggleLamp(finalI));
        }

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

    private void toggleLamp(int index) {
        if (webSocket != null) {
            webSocket.send("TOGGLE " + index);
        }
    }

    private void updateUI(String message) {
        String[] updates = message.split(";");
        for (String update : updates) {
            if (update.contains(":")) {
                String[] parts = update.split(":");
                int index = Integer.parseInt(parts[0]);
                boolean state = parts[1].equals("ON");
                lampStates[index] = state;
                lamps[index].setImageResource(state ? R.drawable.ampoule_on : R.drawable.ampoule_off);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity fermée");
        }
    }
}


<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white"
    android:orientation="vertical"
    android:gravity="center"
    tools:context=".MainActivity">

    <GridLayout
        android:id="@+id/lampGrid"
        android:background="@color/white"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:columnCount="4"
        android:rowCount="4"
        android:padding="10dp"
        android:layout_margin="5dp">

        <!-- Ampoule 1 -->
        <LinearLayout
            android:background="@color/white"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_0"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 1"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Ampoule 2 -->
        <LinearLayout
            android:background="@color/white"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_1"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 2"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Ampoule 3 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_2"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 3"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Ampoule 4 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_3"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 4"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Répétez cette structure jusqu'à Ampoule 16 -->

        <!-- Ampoule 5 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_4"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 4"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_5"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 6"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Ampoule 2 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_6"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 7"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Ampoule 3 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_7"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 8"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Ampoule 4 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_8"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 9"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Répétez cette structure jusqu'à Ampoule 16 -->

        <!-- Ampoule 5 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_9"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 10"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_10"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 11"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Ampoule 2 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_11"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 12"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Ampoule 3 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_12"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Ampoule 13"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <!-- Ampoule 4 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_13"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 14"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_14"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 15"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_rowWeight="1"
            android:layout_columnWeight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="5dp">

            <ImageView
                android:id="@+id/lamp_15"
                android:layout_width="wrap_content"
                android:layout_height="0dp"
                android:layout_weight="1"
                android:adjustViewBounds="true"
                android:clickable="true"
                android:focusable="true"
                android:scaleType="fitCenter"
                android:src="@drawable/ampoule_off"
                tools:ignore="SpeakableTextPresentCheck" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Lampe 16"
                android:textSize="14sp"
                android:textColor="@android:color/black"
                android:gravity="center"/>
        </LinearLayout>

    </GridLayout>

</LinearLayout>

<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:tools="http://schemas.android.com/tools"
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <item
        android:id="@+id/menu_main"
        android:title="Menu"
        android:icon="@drawable/baseline_menu_24"
        app:showAsAction="always">

        <menu>
            <item
                android:id="@+id/change_password"
                android:title="Changer le mot de passe"
                android:icon="@drawable/baseline_wifi_password_24"
                app:showAsAction="never"/>
            <item
                android:id="@+id/add_lamp"
                android:title="Ajouter une lampe"
               android:icon="@drawable/baseline_add_circle_outline_24"
                app:showAsAction="never"/>
        </menu>

    </item>
</menu>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/white"
    android:padding="16dp">
    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="54dp"
        android:background="#FFFFFF"
        android:title="ajouté une bouton"
        android:titleTextColor="#000000" />

    <EditText
        android:id="@+id/ssidInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Nouveau SSID"
        tools:ignore="TouchTargetSizeCheck" />

    <EditText
        android:id="@+id/passwordInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Nouveau mot de passe"
        android:inputType="textPassword"
        tools:ignore="TouchTargetSizeCheck" />

    <Button
        android:id="@+id/saveButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Enregistrer"/>
</LinearLayout>


<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white"
    android:orientation="vertical"
    tools:context=".AjouterLampMainActivity">
    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="54dp"
        android:background="#FFFFFF"
        android:title="ajouté une bouton"
        android:titleTextColor="#000000" />

    <EditText
        android:id="@+id/lampNameInput"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:layout_marginBottom="16dp"
        android:hint="Nom de la lampe"
        android:padding="10dp"
        android:textSize="18sp"
        tools:ignore="TouchTargetSizeCheck" />

    <Button
        android:id="@+id/addLampButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:backgroundTint="@color/black"
        android:layout_gravity="center"
        android:padding="12dp"
        android:text="Ajouter Lampe"
        android:textSize="18sp" />

</LinearLayout>


<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:tools="http://schemas.android.com/tools"
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <item
        android:id="@+id/menu_main"
        android:title="Menu"
        android:icon="@drawable/baseline_menu_24"
        app:showAsAction="always">

        <menu>
            <item
                android:id="@+id/change_password"
                android:title="Changer le mot de passe"
                android:icon="@drawable/baseline_wifi_password_24"
                app:showAsAction="never"/>
            <item
                android:id="@+id/add_lamp"
                android:title="Ajouter une lampe"
               android:icon="@drawable/baseline_add_circle_outline_24"
                app:showAsAction="never"/>
        </menu>

    </item>
</menu>





#include <WiFi.h>
#include <ESPAsyncWebServer.h>
#include <AsyncTCP.h>
#include <EEPROM.h>
#include "esp_wifi.h"

#define LATCH_PIN 5  // Broche ST_CP du 74HC595
#define CLOCK_PIN 18 // Broche SH_CP du 74HC595
#define DATA_PIN 23  // Broche DS du 74HC595

char ssid[32] = "ESP32_AP";
char password[32] = "123456789";
bool lampState[16] = {false}; // Stocke l'état de chaque lampe (0 à 15)

AsyncWebServer server(80);
AsyncWebSocket ws("/ws");

// Fonction pour mettre à jour le registre à décalage et afficher sur le Serial Monitor
void updateShiftRegister() {
    digitalWrite(LATCH_PIN, LOW);
    Serial.println("État des lampes :"); // Titre pour Serial Monitor
    for (int i = 15; i >= 0; i--) {
        digitalWrite(CLOCK_PIN, LOW);
        digitalWrite(DATA_PIN, lampState[i] ? HIGH : LOW);
        digitalWrite(CLOCK_PIN, HIGH);

        // Affichage sur Serial Monitor
        Serial.print("Lampe ");
        Serial.print(i);
        Serial.print(" : ");
        Serial.println(lampState[i] ? "ON" : "OFF");
    }
    digitalWrite(LATCH_PIN, HIGH);
}

// Fonction pour gérer les messages WebSocket reçus depuis Android
void handleWebSocketMessage(void *arg, uint8_t *data, size_t len) {
    AwsFrameInfo *info = (AwsFrameInfo*)arg;
    if (info->opcode == WS_TEXT) {
        String message = String((char*)data).substring(0, len);
        Serial.println("Message reçu d'Android : " + message); // Affichage sur Serial Monitor

        if (message.startsWith("TOGGLE")) {
            int index = message.substring(7).toInt(); // Récupérer l'index de la lampe
            if (index >= 0 && index < 16) {
                lampState[index] = !lampState[index]; // Inverser l'état de la lampe
                updateShiftRegister(); // Mettre à jour les sorties du 74HC595
                ws.textAll(String(index) + ":" + (lampState[index] ? "ON" : "OFF"));
            }
        }
    }
}

// Gestionnaire des événements WebSocket
void onWebSocketEvent(AsyncWebSocket *server, AsyncWebSocketClient *client, AwsEventType type, void *arg, uint8_t *data, size_t len) {
    if (type == WS_EVT_CONNECT) {
        String states = "";
        for (int i = 0; i < 16; i++) {
            states += String(i) + ":" + (lampState[i] ? "ON" : "OFF") + ";";
        }
        client->text(states); // Envoie l'état actuel des lampes au nouvel appareil connecté
    } else if (type == WS_EVT_DATA) {
        handleWebSocketMessage(arg, data, len);
    }
}

void setup() {
    Serial.begin(115200);
    
    // Initialisation des broches du registre à décalage
    pinMode(LATCH_PIN, OUTPUT);
    pinMode(CLOCK_PIN, OUTPUT);
    pinMode(DATA_PIN, OUTPUT);
    updateShiftRegister(); // Mettre à jour l'affichage initial

    // Création du point d'accès WiFi
    WiFi.softAP(ssid, password);
    
    // Configuration du WebSocket
    ws.onEvent(onWebSocketEvent);
    server.addHandler(&ws);
    server.begin();
}

void loop() {
    ws.cleanupClients();
}
