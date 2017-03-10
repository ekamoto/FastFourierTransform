package hisamoto.com.br.tcc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Scanner;

public class MainHisamoto extends AppCompatActivity implements SensorEventListener {

    private TextView valorX;
    private TextView valorY;
    private TextView valorz;
    private TextView valor_moe;

    private float x;
    private float y;
    private float z;

    private SensorManager mSensorManager;
    private Sensor mAcelerometro;
    GraphView graph;
    GraphView graphFFT;
    LineGraphSeries<DataPoint> seriesZ;
    LineGraphSeries<DataPoint> serieFFT;

    int startTime;

    private double[] vector_x;
    private double[] vector_y;

    private ManageFile manageFile;
    private int contador = 0;
    private boolean gravarPontos = false;

    private int n = 512;
    private ComplexFFT complexFFT;
    private double qtd_pontos_segundo_teste = 2.0;
    private double qtd_pontos_segundo_amostra = 100;
    private int contador_pontos = 0;
    private double max_point_y = -1.0;
    private double largura = 0;
    private double altura = 0;
    private double massa_viga = 0.0;
    private double vao_peca = 0.0;

    private SharedPreferences sharedPreferences;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /************************************* Iniciando captura de variação X, Y, Z *********************************/
        valorX = (TextView) findViewById(R.id.valorx);
        valorY = (TextView) findViewById(R.id.valory);
        valorz = (TextView) findViewById(R.id.valorz);
        valor_moe = (TextView) findViewById(R.id.valor_moe);

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

        //case SENSOR_DELAY_FASTEST:
        //delay = 0;

        //case SENSOR_DELAY_GAME:
        //delay = 20000;

        //case SENSOR_DELAY_UI:
        //delay = 66667;

        //case SENSOR_DELAY_NORMAL:
        //delay = 200000;

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);

        mAcelerometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!gravarPontos) {

                    max_point_y = -1.0;
                    Snackbar.make(view, "Gravando Pontos...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    contador_pontos = 0;

                    contador = 0;
                    vector_x = new double[10000];
                    vector_y = new double[10000];
                    gravarPontos = true;
                } else {
                    Snackbar.make(view, "Plotando Pontos...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    gravarPontos = false;
                }
            }
        });

        // Executando Teste
        TestFFT();
        /******************************************* Limpando vetores *************************************/
        vector_x = new double[10000];
        Complex[] x = new Complex[n];
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /*
    * Quando ocorre alguma alteração no sensor do celular
    * ele é notificado aqui
    *
    **/
    @Override
    public void onSensorChanged(SensorEvent event) {

        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        valorX.setText(String.valueOf(x));
        valorY.setText(String.valueOf(y));
        valorz.setText(String.valueOf(z));

        Log.i("PontosPSegundo", "" + z);

        updateGraph(startTime++, x, y, z);
    }

    /*Verifica se número é potencia de 2*/
    private boolean ePotenciaDeDois(int numero) {

        int i =0;
        int numero_multi = 1;
        for(;i<20;i++) {

            if(numero_multi == numero){

                return true;
            }

            numero_multi *= 2;
            Log.i("HisamotoGravandoPontos", " Multiplos - " + numero_multi);
        }

        return false;
    }

    void updateGraph(final long timestamp, final float x, final float y, final float z) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //seriesX.appendData(new DataPoint(timestamp, x), true, 1000);
                //seriesY.appendData(new DataPoint(timestamp, y), true, 1000);
                seriesZ.appendData(new DataPoint(timestamp, z), true, 1000);

                if (gravarPontos ||(contador_pontos!=0 && !ePotenciaDeDois(contador_pontos))) {

                    // Conta quantidade de pontos que estão sendo coletados
                    contador_pontos++;

                    Log.i("HisamotoGravandoPontos", " Gravando Pontos - " + contador_pontos);


                    vector_x[contador] = timestamp;
                    vector_y[contador] = z;
                    Log.i("HisamotoGravandoPontos","Get "+contador_pontos+" - " + z);
                    contador++;

                } else if(contador_pontos > 0) {

                    Log.i("HisamotoGravandoPontos", " Plotando pontos: "+contador_pontos);

                    int l = 0; // Ignorar primeiro valor, testar essa alteração
                    Complex[] vecy = new Complex[contador_pontos];

                    for (; l < contador_pontos; l++) {

                        vecy[l] = new Complex((double) vector_y[l], 0);

                    }

                    complexFFT = new ComplexFFT();

                    Complex[] y_processado = complexFFT.fft(vecy);

                    graphFFT = (GraphView) findViewById(R.id.graphFFT);

                    DataPoint[] datapoints = new DataPoint[contador_pontos];

                    for (int i = 0; i < contador_pontos; i++) {

                        double teste_x = i * qtd_pontos_segundo_amostra / (contador_pontos / 2);
                        double teste_y = y_processado[i].abs();
                        Log.i("TestePlot", teste_x + " | " + teste_y);

                        if(i == 0) {

                            teste_y = 0;
                        }
                        // Pegando pico máximo
                        if(teste_y > max_point_y) {

                            max_point_y = teste_y;
                        }

                        datapoints[i] = new DataPoint(teste_x, teste_y);
                    }

                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    altura = Float.valueOf(sharedPreferences.getString("largura_", ""));
                    largura = Float.valueOf(sharedPreferences.getString("altura_", ""));
                    massa_viga = Float.valueOf(sharedPreferences.getString("massa_viga_", ""));
                    vao_peca = Float.valueOf(sharedPreferences.getString("vao_peca_", ""));

                    Log.i("PicoMaximo", "altura: " + altura);
                    Log.i("PicoMaximo", "largura: " + largura);
                    Log.i("PicoMaximo", "massa_vida: " + massa_viga);
                    Log.i("PicoMaximo", "vao_peca: " + vao_peca);

                    //I= b.h³/12
                    double valormoe = ((max_point_y*max_point_y)*massa_viga*(vao_peca*vao_peca*vao_peca))/(2.46*(largura*(altura*altura*altura)/12.0)*9.8);

                    valor_moe.setText("" + valormoe);

                    graphFFT.removeAllSeries();
                    serieFFT = new LineGraphSeries<DataPoint>(datapoints);

                    serieFFT.setColor(Color.BLACK);
                    serieFFT.setAnimated(true);
                    serieFFT.setThickness(4);

                    graphFFT.addSeries(serieFFT);
                    graphFFT.setTitle("FFT");

                    gravarPontos = false;

                    Log.i("Plotounomapa", "Plotouuuuuuuuuuuuuuuuuuuuu");

                    contador_pontos = 0;
                    vector_x = new double[10000];
                    vector_y = new double[10000];

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        Log.i("Menu", "Menu");

        if (id == R.id.action_settings) {
            Log.i("Menu", "Configuracoes");

            Intent i = new Intent();
            i.setClass(getApplicationContext(), SettingsActivity.class);
            startActivity(i);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Função para testar Função FFT
     */
    private void TestFFT() {
        try {

            Complex[] x_hisamoto = new Complex[n];

            /************************ Ler arquivo e setar em array para testar função FFT ************************/
            Scanner scanner = new Scanner(new File("/storage/emulated/0/Hisamoto/Dados.csv"));

            manageFile = new ManageFile(getApplicationContext());
            Scanner dataScanner = null;
            int index = 0;

            vector_x = new double[10000];

            int cont = 0;
            while (scanner.hasNextLine()) {
                dataScanner = new Scanner(scanner.nextLine());
                dataScanner.useDelimiter(";");

                while (dataScanner.hasNext()) {

                    String data = dataScanner.next();
                    double valor = Double.parseDouble(data.replace(",", ".").trim());

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

            for (int i = 0; i < n; i++) {

                double _y = y_hisamoto[i].abs();
                double _x = i * qtd_pontos_segundo_teste / (n / 2);
                Log.i("TestePlot", _x + " | " + _y);
                datapoints[i] = new DataPoint(_x, _y);
            }

            /********************************* Gravando no arquivo os dados processados *********************************/
            manageFile.WriteFile("X - Y");

            for (int i = 0; i < n; i++) {

                manageFile.WriteFile((i * qtd_pontos_segundo_teste / (n / 2)) + " - " + y_hisamoto[i].abs());
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
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MainHisamoto Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://hisamoto.com.br.tcc/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MainHisamoto Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://hisamoto.com.br.tcc/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
