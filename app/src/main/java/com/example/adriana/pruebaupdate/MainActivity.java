package com.example.adriana.pruebaupdate;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PERMISOS";
    private Autoupdater updater;
    private RelativeLayout loadingPanel;
    private Context context;
    private Button actualizarBTN;
    private int REQUEST_WRITE_STORAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
            //Esto sirve si la actualizacion no se realiza al principio.
            loadingPanel.setVisibility(View.GONE);
            //Comentamos esa linea para que no se ejecute al principio.
            //comenzarActualizacion();
            actualizarBTN = (Button) findViewById(R.id.botonActualizar);
            actualizarBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    comenzarActualizar();
                }
            });
        }catch (Exception ex){
            //Por Cualquier error.
            Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
        }

        requestAppPermissions();
    }

    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }


    private void comenzarActualizar(){
        //Para tener el contexto mas a mano.
        context = this;
        //Creamos el Autoupdater.
        updater = new Autoupdater(this);
        //Ponemos a correr el ProgressBar.
        loadingPanel.setVisibility(View.VISIBLE);
        //Ejecutamos el primer metodo del Autoupdater.
        updater.DownloadData(finishBackgroundDownload);
    }

    /**
     * Codigo que se va a ejecutar una vez terminado de bajar los datos.
     */
    private Runnable finishBackgroundDownload = new Runnable() {
        @Override
        public void run() {
            //Volvemos el ProgressBar a invisible.
            loadingPanel.setVisibility(View.GONE);
            //Comprueba que halla nueva versión.
            if(updater.isNewVersionAvailable()){
                //Crea mensaje con datos de versión.
                String msj = "Nueva Version: " + updater.isNewVersionAvailable();
                msj += "\nCurrent Version: " + updater.getCurrentVersionName() + "(" + updater.getCurrentVersionCode() + ")";
                msj += "\nLastest Version: " + updater.getLatestVersionName() + "(" + updater.getLatestVersionCode() +")";
                msj += "\nDesea Actualizar?";
                //Crea ventana de alerta.
                AlertDialog.Builder dialog1 = new AlertDialog.Builder(context);
                dialog1.setMessage(msj);
                dialog1.setNegativeButton("Cancelar", null);
                //Establece el boton de Aceptar y que hacer si se selecciona.
                dialog1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Vuelve a poner el ProgressBar mientras se baja e instala.
                        loadingPanel.setVisibility(View.VISIBLE);
                        //Se ejecuta el Autoupdater con la orden de instalar. Se puede poner un listener o no
                        updater.InstallNewVersion(null);
                    }
                });

                //Muestra la ventana esperando respuesta.
                dialog1.show();
            }else{
                //No existen Actualizaciones.
                Log.d("Error","No Hay actualizaciones");
            }
        }
    };
}
