package hisamoto.com.br.tcc;

/**
 * Created by hisamoto on 25/12/16.
 */

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Classe responsável pela escrita e leitura de arquivo.
 * @author Romar Consultoria
 *
 */
public class ManageFile {
    private static final String TAG = "ManageFile";
    private Context context;

    public ManageFile(Context context){
        this.context = context;
    }

    /**
     * Escreve no arquivo texto.
     * @param text Texto a ser escrito.
     * @return True se o texto foi escrito com sucesso.
     */
    public boolean WriteFile(String text){
        try {
            // Abre o arquivo para escrita ou cria se não existir
            //FileOutputStream out = context.openFileOutput("storage/emulated/0/Hisamoto/saidaFFT.csv",
            //        Context.MODE_PRIVATE);
            Log.i(TAG, "Entrou para salvar");
            FileOutputStream fop = null;
            File file;

            text += "\n";

            file = new File("/storage/emulated/0/Hisamoto/saidaFFT.csv");
            fop = new FileOutputStream(file, true);

            if (!file.exists()) {

                file.createNewFile();

                Log.i(TAG, "Criou o arquivo");
            }

            byte[] contentInBytes = text.getBytes();

            fop.write(contentInBytes);
            //fop.write(System.getProperty("line.separator").getBytes());
            fop.flush();
            fop.close();


            //FileInputStream
            //out.write(text.getBytes());
            //out.write("\n".getBytes());
            //out.flush();
            //out.close();
            return true;

        } catch (Exception e) {
            Log.i(TAG, "Deu erro ao salvar arquivo:"+e.getMessage());
            Log.e(TAG, e.toString());
            return false;
        }
    }

    /**
     * Faz a leitura do arquivo
     * @return O texto lido.
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public String ReadFile() throws FileNotFoundException, IOException{
        File file = context.getFilesDir();
        File textfile = new File(file + "/romar.txt");

        FileInputStream input = context.openFileInput("romar.txt");
        byte[] buffer = new byte[(int)textfile.length()];

        input.read(buffer);

        return new String(buffer);
    }
}