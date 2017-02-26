package hisamoto.com.br.tcc;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Scanner;

public class MainHisamoto extends AppCompatActivity implements SensorEventListener{

    private TextView valorX;
    private TextView valorY;
    private TextView valorz;

    private  float x;
    private  float y;
    private  float z;

    private SensorManager mSensorManager;
    private Sensor mAcelerometro;
    GraphView graph;
    GraphView graphFFT;
    LineGraphSeries<DataPoint> seriesZ;
    LineGraphSeries<DataPoint> serieFFT;

    int startTime;

    private double[] vector_x;

    private ManageFile manageFile;

    private int n = 512;
    private ComplexFFT complexFFT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /************************************* Iniciando captura de variação X, Y, Z *********************************/
        valorX = (TextView) findViewById(R.id.valorx);
        valorY = (TextView) findViewById( R.id.valory);
        valorz = (TextView) findViewById(R.id.valorz);

        graph = (GraphView) findViewById(R.id.graph);
        Viewport vp = graph.getViewport();
        vp.setXAxisBoundsManual(true);
        vp.setMinX(0);
        vp.setMaxX(1000);

        seriesZ = new LineGraphSeries<>();
        seriesZ.setColor(Color.MAGENTA);
        seriesZ.setAnimated(false);
        seriesZ.setThickness(4);

        graph.setTitle("Aceleração/Tempo");
        graph.addSeries(seriesZ);

        startTime = 1;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);

        mAcelerometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Gravando pontos...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                vector_x = new double[1000];
            }
        });

        // Executando Teste
        TestFFT();
        /******************************************* Limpando vetores *************************************/
        vector_x = new double[1000];
        Complex[] x = new Complex[n];
    }

    /**
     * Função para testar Função FFT
     *
     * */
    private void TestFFT() {
        try {

            Complex[] x_hisamoto = new Complex[n];

            /************************ Ler arquivo e setar em array para testar função FFT ************************/
            Scanner scanner = new Scanner(new File("/storage/emulated/0/Hisamoto/Dados.csv"));

            manageFile = new ManageFile(getApplicationContext());
            Scanner dataScanner = null;
            int index = 0;

            vector_x = new double[1000];

            int cont = 0;
            while (scanner.hasNextLine()) {
                dataScanner = new Scanner(scanner.nextLine());
                dataScanner.useDelimiter(";");

                while (dataScanner.hasNext()) {

                    String data = dataScanner.next();
                    double valor = Double.parseDouble(data.replace(",",".").trim());

                    if (index == 1) {

                        x_hisamoto[cont] = new Complex(valor, 0);
                    }

                    index++;
                }
                index = 0;
                cont++;
            }
            scanner.close();

            /************************************************ Processo FFT ***********************************************/
            complexFFT = new ComplexFFT();

            Complex[] y_hisamoto = complexFFT.fft(x_hisamoto);

            /***************************************** Inicializo e configuro gráfico ************************************/
            graphFFT = (GraphView) findViewById(R.id.graphFFT);

            Viewport vpFFT = graphFFT.getViewport();
            vpFFT.setScalable(false);

            /************************************** Cria os datapoints para o gráfico ************************************/
            DataPoint[] datapoints = new DataPoint[512];

            for(int i = 0; i < n; i++) {

                double _y = y_hisamoto[i].abs();
                double _x = i*2.0/(n/2);

                datapoints[i] = new DataPoint(_x, _y);
            }

            /********************************* Gravando no arquivo os dados processados *********************************/
            manageFile.WriteFile("X - Y");

            for(int i = 0; i < n; i++) {

                manageFile.WriteFile((i*2/(n/2)) + " - " + y_hisamoto[i].abs());
            }

            /************************************* Plotando dados processados no gráfico*********************************/
            graphFFT.removeAllSeries();
            serieFFT = new LineGraphSeries<DataPoint>(datapoints);

            serieFFT.setColor(Color.RED);
            serieFFT.setAnimated(true);
            serieFFT.setThickness(1);

            graphFFT.addSeries(serieFFT);
            graphFFT.setTitle("FFT");

        } catch (FileNotFoundException e) {

            Log.i("leituracsv", "Deu erro:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this);
    }

    /*
    * Quando ocorre alguma alteração no sensor do celular
    * ele é notificado aqui
    * */
    @Override
    public void onSensorChanged(SensorEvent event) {

        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        valorX.setText( String.valueOf( x ) );
        valorY.setText( String.valueOf( y ) );
        valorz.setText( String.valueOf( z ) );

        updateGraph(startTime++, x, y, z);
    }


    void updateGraph(final long timestamp, final float x, final float y, final float z) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
