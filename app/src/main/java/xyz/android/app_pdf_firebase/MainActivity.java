package xyz.android.app_pdf_firebase;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 1;

    private TextInputEditText fileNameEditText;
    private MaterialButton uploadButton, listButton;
    private TextView fileListTextView;
    private ListView listaArchivos;
    FirebaseStorage storage;
    StorageReference fileRef;
    String filename;
    ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtiene la referencia al almacenamiento de Firebase
        storage = FirebaseStorage.getInstance();

        fileNameEditText = findViewById(R.id.edit_text_file_name);
        uploadButton = findViewById(R.id.button_upload);
        listButton = findViewById(R.id.button_list);
        listaArchivos = findViewById(R.id.lvDatos);

        uploadButton.setOnClickListener(v -> {
            uploadArchivo();
        });

        listButton.setOnClickListener(v -> {
            listarArchivos();
        });

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {

                // Sube el archivo a Firebase Storage utilizando la URL del archivo seleccionado
                fileRef.putFile(result)
                        .addOnSuccessListener(taskSnapshot -> {
                            Toast.makeText(MainActivity.this, "Archivo cargado exitosamente", Toast.LENGTH_SHORT).show();
                            fileNameEditText.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error al cargar el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void listarArchivos() {

        // Obtiene la referencia a la raíz del almacenamiento de Firebase
        fileRef = storage.getReference();
        List<String> archivos = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, archivos);
        listaArchivos.setAdapter(adapter);

        fileRef.listAll()
                .addOnSuccessListener(listResult -> {
                    listResult.getItems().stream().forEach(item -> archivos.add(item.getName()));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error al cargar el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadArchivo() {
        filename = fileNameEditText.getText().toString();

        // Verificar si se ingreso un nombre de arhivo valido
        if (filename.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre de archivo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crea una referencia al archivo en Firebase Storage utilizando el nombre especificado
        fileRef = storage.getReference().child(filename);

        // Llama al ActivityResultLauncher para abrir el selector de archivos
        // "application/pdf"
        filePickerLauncher.launch("*/*");
    }
}