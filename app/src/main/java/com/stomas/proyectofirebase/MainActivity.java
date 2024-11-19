package com.stomas.proyectofirebase;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Declaro variables
    private EditText txtCodigo, txtNombre, txtDueño, txtDireccion;
    private ListView lista;
    private Spinner spMascota;

    // Variable de la conexión de Firestore
    private FirebaseFirestore db;

    // Datos del Spinner de tipos de mascotas
    String[] TiposMascotas = {"Perro", "Gato", "Pájaro"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializo Firestore
        db = FirebaseFirestore.getInstance();
        Log.d("FirebaseInit", "Firestore initialized: " + (db != null)); // Validación de inicialización

        // Uno las variables con los del XML
        txtCodigo = findViewById(R.id.txtCodigo);
        txtNombre = findViewById(R.id.txtNombre);
        txtDueño = findViewById(R.id.txtDueño);
        txtDireccion = findViewById(R.id.txtDireccion);
        spMascota = findViewById(R.id.spMascota);
        lista = findViewById(R.id.lista);

        // Poblar Spinner con los tipos de Mascotas
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TiposMascotas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMascota.setAdapter(adapter);

        // Llamamos al método que carga la lista (después de inicializar Firestore)
        CargarListaFirestore();
    }

    // Método para enviar datos
    public void enviarDatosFirestore(View view) {
        // Obtengo los datos ingresados en el formulario
        String codigo = txtCodigo.getText().toString();
        String nombre = txtNombre.getText().toString();
        String dueño = txtDueño.getText().toString();
        String direccion = txtDireccion.getText().toString();
        String tipoMascota = spMascota.getSelectedItem().toString();

        // Creo un mapa con los datos a enviar
        Map<String, Object> mascota = new HashMap<>();
        mascota.put("codigo", codigo);
        mascota.put("nombre", nombre);
        mascota.put("dueño", dueño);
        mascota.put("direccion", direccion);
        mascota.put("tipoMascota", tipoMascota);

        // Envío los datos a Firestore
        db.collection("mascotas")
                .document(codigo) // Documento será el código
                .set(mascota)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Datos enviados a Firestore correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error al enviar datos a Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Botón que Carga la Lista
    public void CargarLista(View view) {
        CargarListaFirestore();
    }

    // Método Cargar Lista
    public void CargarListaFirestore() {
        if (db == null) {
            Log.e("FirestoreError", "Firestore no está inicializado");
            return; // Evita el intento de cargar datos si `db` es null
        }

        db.collection("mascotas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> listaMascotas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String linea = "|" + document.getString("codigo") + "|"
                                    + document.getString("nombre") + "|"
                                    + document.getString("dueño") + "|"
                                    + document.getString("direccion");
                            listaMascotas.add(linea);
                        }

                        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_list_item_1,
                                listaMascotas
                        );
                        lista.setAdapter(adaptador);
                    } else {
                        Log.e("TAG", "Error al obtener datos de Firestore", task.getException());
                    }
                });
    }
}
