/**
 * T�tulo: Android4Education Gestos App
 * Licencia P�blica General de GNU (GPL) versi�n 3 
 * C�digo Fuente: https://github.com/pebosch/a4e-gestosapp
 * Autor: Pedro Fern�ndez Bosch
 * Fecha de la �ltima modificaci�n: 28/01/2015
 */

package com.a4e.gestosapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.a4e.gestosapp.R;
import java.util.List;
import java.util.ArrayList;

/**
 * Declaraci�n de componentes
 */
public class MainActivity extends Activity implements OnGesturePerformedListener{

	// Componentes gesture library para el reconocimiento de gestos
	private GestureOverlayView gesture;
	private GestureLibrary gLibrary;	
	
	// Componentes Barcode Scanner para la lectura de c�digos QR
	private static final String BS_PACKAGE = "com.google.zxing.client.android";
	public static final int SCANNER_REQUEST_CODE = 0x0000c0de;
	TextView tvScanResults;
	private Activity activity;

	/**
	 * OnCreate Method Override 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
		gesture = (GestureOverlayView)findViewById(R.id.gestureOverlayView1); // Relaci�n con el XML
		gesture.addOnGesturePerformedListener(this); // Listener    
		gLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures); // Inclusi�n del fichero gestures a  la carpeta res/raw. Se a�ade el raw a gLibrary
		gLibrary.load(); // Carga...
		
		activity=this;
	}
	
	/**
	 * Reconocimiento gestual
	 */	
	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		
		ArrayList<Prediction> predictions = gLibrary.recognize(gesture); // Array de resultados cotejados con el fichero gestures y ordenados de mayor a menor fiabilidad
		
		if (predictions.size() > 0) { // Se ha encontrado alg�n resultado
			Prediction prediction = predictions.get(0);
			
			if (prediction.score > 1.5) { // Debe tener un parecido m�nimo
				
				if(predictions.get(0).name.contains("t")){ 
					// Llamada al scanner de QR 
					Intent intentScan = new Intent(BS_PACKAGE + ".SCAN");
				    String targetAppPackage = findTargetAppPackage(intentScan);
				    if (targetAppPackage == null){ // La aplicaci�n lectora de QR no est� instalada
				    	showDownloadDialog(); 
				    } 
				    else{ // Comenzar el escaneo
				    	startActivityForResult(intentScan, SCANNER_REQUEST_CODE); 
				    }
				}else{ // No se ha renocido un gesto
					Toast.makeText(this, "Gesto no reconocido.",Toast.LENGTH_SHORT).show();
				}
			}else{ // No supera el 1.5 de fiabilidad
				Toast.makeText(this, "Gesto no reconocido.",Toast.LENGTH_SHORT).show();
			}
		}  
	}
	
	/**
	 * Obtener el listado de paquetes instalados en el dispositivo 
	 */	
	private String findTargetAppPackage(Intent intent) {
		PackageManager pm = activity.getPackageManager();
		List<ResolveInfo> availableApps = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY); // Listado de paquetes (aplicaciones) instalados
        if (availableApps != null) {
        	for (ResolveInfo availableApp : availableApps) {
        		String packageName = availableApp.activityInfo.packageName;
        		if (BS_PACKAGE.contains(packageName)) {
        			return packageName; // Devuelve el nombre del paquete
        		}
        	}
        }
        return null;
	}
	
	/**
	 * Mensaje de alerta para la instalaci�n de paquetes
	 */	
	private AlertDialog showDownloadDialog() {
		// Output del mensaje de alerta
		final String DEFAULT_TITLE = "Instalar Barcode Scanner";
		final String DEFAULT_MESSAGE = "Esta aplicaci�n requiere Barcode Scanner. �Desea instalarlo?";
		final String DEFAULT_YES = "Si";
		final String DEFAULT_NO = "No";

		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
		downloadDialog.setTitle(DEFAULT_TITLE);
		downloadDialog.setMessage(DEFAULT_MESSAGE);
		downloadDialog.setPositiveButton(DEFAULT_YES, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				Uri uri = Uri.parse("market://details?id=" + BS_PACKAGE); //Acceder al paquete desde "Play Store"
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				try {
					activity.startActivity(intent);
				} catch (ActivityNotFoundException anfe) { // El paquete "Play Store" no est� instalado
					Toast.makeText(MainActivity.this, "No se ha encontrado Play Store en su dispositivo. La instalaci�n de Barcode Scanner ha sido interrumpida.", Toast.LENGTH_LONG).show();
				}
			}
		});
		downloadDialog.setNegativeButton(DEFAULT_NO, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {}
			});
		return downloadDialog.show();
	}

	/**
	 * Esc�ner QR
	 */	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == SCANNER_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) { 
				// Escaneo satisfactorio
				String contents = intent.getStringExtra("SCAN_RESULT");
				String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");

				tvScanResults.setText(contents + "\n\n" + formatName);

			} else if (resultCode == Activity.RESULT_CANCELED) {
				// Escaneo cancelado
			}
		} else {
			// Escanear de nuevo
		}
	}
	
	/**
	 * Resultados obtenidos del scanner QR
	 */	
	private void initViews() {
		tvScanResults = (TextView) findViewById(R.id.tvResults);
	}
	
	/**
	 * Definici�n del menu de opciones de la aplicaci�n
	 */	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * Funcionalidad de los �tems del menu
	 */	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.optionLicencia:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gnu.org/licenses/gpl.html")));
			break;
		case R.id.optionCodigo:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pebosch/a4e-gestosapp")));
			break;
		case R.id.optionAyuda:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.a4e.gestosapp")));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}