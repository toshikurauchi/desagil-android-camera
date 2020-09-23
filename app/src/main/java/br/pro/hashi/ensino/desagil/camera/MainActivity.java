package br.pro.hashi.ensino.desagil.camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Esta constante é um código para identificar um pedido de "tirar foto".
    private static final int REQUEST_TAKE_PHOTO = 1;

    // Este atributo é o caminho do arquivo onde a última foto foi salva.
    private String lastPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonExample = findViewById(R.id.button_example);

        buttonExample.setOnClickListener((view) -> {

            // Constrói uma Intent que corresponde ao pedido de "tirar foto".
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Se não existe no celular uma Activity que aceite esse Intent,
            // não podemos fazer nada. Parece um caso meio absurdo, mas o
            // usuário pode ter desativado a câmera por algum motivo.
            if (intent.resolveActivity(getPackageManager()) == null) {
                return;
            }

            // A ideia é criar um arquivo e compartilhar esse arquivo com o
            // aplicativo que receba a Intent, para que ele possa gravar a
            // foto nele. Por isso precisamos de um provedor de arquivos.

            // Escolhemos para guardar o arquivo a pasta padrão para fotos.
            File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            // Esse método createTempFile é especial: além de criar o arquivo,
            // também garante que o nome desse arquivo vai ser único na pasta.
            File file;
            try {
                file = File.createTempFile("image", ".jpg", directory);
            } catch (IOException exception) {
                file = null;
            }

            // Se o arquivo não pôde ser criado, não podemos fazer nada.
            if (file == null) {
                return;
            }

            // Guarda o caminho do arquivo no atributo.
            lastPath = file.getAbsolutePath();

            // O arquivo será passado para o aplicativo de câmera através de
            // uma URI criada pelo provedor de arquivos. Atenção: o nome de
            // pacote no parâmetro "authority" deve ser EXATAMENTE IGUAL no
            // nome no arquivo de configuração manifests/AndroidManifest.xml.
            Uri uri = FileProvider.getUriForFile(
                    this,
                    "br.pro.hashi.ensino.desagil.camera.fileprovider",
                    file
            );

            // Anexa a URI na Intent, para que o aplicativo de câmera a receba.
            // Essa é uma maneira comum de uma Activity passar dados para outra.
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            // Dispara a Intent. Lembrando que quem vai receber essa Intent não
            // é este aplicativo, mas algum outro que saiba recebê-la. No caso,
            // estamos esperando que o aplicativo de câmera faça isso.
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        });
    }


    // Quando usamos o método startActivityForResult para disparar uma Intent,
    // esse método onActivityResult é chamado quando a Activity que recebeu
    // a Intent terminou de receber e obteve algum resultado. Neste caso,
    // quando o aplicativo de câmera tirou a foto e guardou no arquivo.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Confirma que de fato é o resultado da Intent de "tirar foto"
        // e que de fato a Activity que recebeu a Intent teve resultado.
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            // Descobre a URI do último arquivo que foi criado. Aqui não usamos
            // a URI do provedor de arquivos porque o uso dele é apenas local.
            Uri uri = Uri.fromFile(new File(lastPath));

            // Carrega uma imagem a partir da URI, se possível.
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException exception) {
                bitmap = null;
            }

            // Se foi possível, coloca essa imagem no elemento que
            // incluímos no layout especialmente para exibi-la.
            if (bitmap != null) {
                ImageView imageExample = findViewById(R.id.image_example);
                imageExample.setImageBitmap(bitmap);
            }
        }
    }
}
