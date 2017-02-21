package hisamoto.com.br.tcc;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private TextView valorX;
    private TextView valorY;
    private TextView valorz;
    private TextView textViewDetail;
    static final Random RANDOM = new Random();
    private  float x;
    private  float y;
    private  float z;

    private SensorManager mSensorManager;
    private Sensor mAcelerometro;
    GraphView graph;
    GraphView graphFFT;
    LineGraphSeries<DataPoint> seriesX, seriesY, seriesZ;
    LineGraphSeries<DataPoint> serieFFT;

    int startTime;

    private FFT fft;
    private double[] vector_x;
    private double[] vector_y;
    private int contador = 0;
    private boolean gravarPontos = false;

    private int qtd_pontos = 512;
    private ManageFile manageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        try {

            /************************ Ler arquivo e setar em array para testar função FFT ************************/

            Log.i("leituracsv", "Lendo arquivo Dados.csv");
            Scanner scanner = new Scanner(new File("/storage/emulated/0/Hisamoto/Dados.csv"));

            manageFile = new ManageFile(getApplicationContext());
            Scanner dataScanner = null;
            int index = 0;

            vector_y = new double[1000];
            vector_x = new double[1000];

            while (scanner.hasNextLine()) {
                dataScanner = new Scanner(scanner.nextLine());
                dataScanner.useDelimiter(";");


                while (dataScanner.hasNext()) {

                    String data = dataScanner.next();

                    data = data.replace(",",".");

                    double valor;

                    valor = Double.parseDouble(data);

                    if (index == 0) {
                        vector_x[index] = valor;
                        //Log.i("leituracsv x:", "" + valor);
                    }
                    else if (index == 1) {

                        vector_y[index] = valor;
                        //Log.i("leituracsv y:", "" + valor);
                    }

                    index++;
                }
                index = 0;
            }
            scanner.close();

            /************************************************ Processo FFT ***********************************************/
            fft = new FFT(512);
            fft.fft(vector_x, vector_y);
            Log.i("PontosGravadosFFT", " Imprimindo FFT");

            /***************************************** Inicializo e configuro gráfico ************************************/
            graphFFT = (GraphView) findViewById(R.id.graphFFT);

            Viewport vpFFT = graphFFT.getViewport();

            //vpFFT.setScalable(true);
            //vpFFT.setScrollable(true);
            vpFFT.setMinY(0.1);
            vpFFT.setScalableY(true);
            //vpFFT.setScrollableY(true);

            /************************************** Cria os datapoints para o gŕafico ************************************/
            DataPoint[] datapoints = new DataPoint[512];
            for(int i = 0; i < 512; i++)
            {
                datapoints[i] = new DataPoint(vector_y[i], vector_x[i]);
            }

            /********************************* Gravando no arquivo os dados processados *********************************/
            manageFile.WriteFile("X - Y");

            for(int i = 0; i < 512; i++) {

                Log.i("AnalisePontosx:", "" + vector_x[i]);

                manageFile.WriteFile(vector_x[i] + " - " +vector_y[i]);
            }

            /******************************************* Printando saida para teste *************************************/
            for(int i = 0; i < 512; i++) {

                Log.i("AnalisePontosy:", "" + vector_y[i]);
            }

            /************************************* Plotando dados processados no gráfico*********************************/
            graphFFT.removeAllSeries();
            serieFFT = new LineGraphSeries<DataPoint>(datapoints);

            serieFFT.setColor(Color.RED);
            serieFFT.setAnimated(true);
            serieFFT.setThickness(1);

            //add series with line and dot format specified earlier to graph

            graphFFT.addSeries(serieFFT);
            graphFFT.setTitle("FFT");
            //////////////////////

        } catch (FileNotFoundException e) {
            Log.i("leituracsv", "Deu erro:" + e.getMessage());
            e.printStackTrace();
        }

        /************************************* Iniciando captura de variação X, Y, Z *********************************/
        valorX = (TextView) findViewById(R.id.valorx);
        valorY = (TextView) findViewById( R.id.valory);
        valorz = (TextView) findViewById(R.id.valorz);

        graph = (GraphView) findViewById(R.id.graph);
        Viewport vp = graph.getViewport();
        vp.setXAxisBoundsManual(true);
        vp.setMinX(0);
        vp.setMaxX(1000);

        //seriesX = new LineGraphSeries<>();
        //seriesY = new LineGraphSeries<>();
        seriesZ = new LineGraphSeries<>();

        //seriesX.setColor(Color.BLUE);
        //seriesX.setAnimated(false);
        //seriesX.setThickness(4);

        //seriesY.setColor(Color.GREEN);
        //seriesY.setAnimated(false);
        //seriesY.setThickness(4);

        seriesZ.setColor(Color.MAGENTA);
        seriesZ.setAnimated(false);
        seriesZ.setThickness(4);

        //graph.addSeries(seriesX);
        //graph.addSeries(seriesY);
        graph.setTitle("Aceleração/Tempo");
        graph.addSeries(seriesZ);
        //graphFFT.addSeries(serieFFT);

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

                gravarPontos = true;
                contador = 0;
                vector_x = new double[1000];
                vector_y = new double[1000];


            }
        });


        /******************************************* Limpando vetores *************************************/
        vector_x = new double[1000];
        vector_y = new double[1000];
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //seriesX.appendData(new DataPoint(timestamp, x), true, 1000);
                //seriesY.appendData(new DataPoint(timestamp, y), true, 1000);
                // Pego somente a informação que é util para o projeto
                seriesZ.appendData(new DataPoint(timestamp, z), true, 1000);

                Log.i("FFT", timestamp + " - " + z);

                if(gravarPontos && contador < qtd_pontos) {

                    Log.i("FFT", " Gravando Pontos ");


                    vector_x[contador] = timestamp;
                    vector_y[contador] = z;

                    contador++;

                    /*********************************Se Capturou a quantidade certa de pontos*****************************/
                    if(contador == qtd_pontos) {

                        Log.i("PontosGravados", " Imprimindo ");
                        for(int i=0;i<contador;i++) {

                            Log.i("PontosGravados", " x - " + vector_x[i] + " y - " + vector_y[i]);
                        }
                        fft.fft(vector_x, vector_y);
                        Log.i("PontosGravadosFFT", " Imprimindo FFT");
                        for(int i = 0 ; i < contador ; i++) {

                            Log.i("PontosGravadosFFT", " x - " + vector_x[i] + " y - " + vector_y[i]);

                            //serieFFT.appendData(new DataPoint(contador, vector_y[i]), true, 1000);


                        }

                        graphFFT = (GraphView) findViewById(R.id.graphFFT);

                        Viewport vpFFT = graphFFT.getViewport();

                        vpFFT.setScalable(true);
                        vpFFT.setScrollable(true);
                        vpFFT.setMaxX(100);

                        DataPoint[] datapoints = new DataPoint[qtd_pontos];

                        for(int i = 0; i < qtd_pontos; i++)
                        {
                            datapoints[i] = new DataPoint(i, vector_y[i]);
                        }

                        graphFFT.removeAllSeries();
                        serieFFT = new LineGraphSeries<DataPoint>(datapoints);

                        serieFFT.setColor(Color.RED);
                        serieFFT.setAnimated(true);
                        serieFFT.setThickness(4);


                        graphFFT.addSeries(serieFFT);
                        graphFFT.setTitle("FFT");

                        gravarPontos = false;
                    }
                }
            }
        });

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
