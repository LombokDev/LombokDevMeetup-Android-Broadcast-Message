package smsbroadcast.lombokdevmeetup.dev.app.lombokdevmeetupsmsbroadcast;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.annimon.stream.Stream;
import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private Button btnChooseFile, btnSend;
  private EditText etMessage;
  private Switch swSapaNama;

  private static final int READ_REQUEST_CODE = 42;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    btnChooseFile = (Button) findViewById(R.id.btnChooseFile);
    btnSend = (Button) findViewById(R.id.btnSend);
    etMessage = (EditText) findViewById(R.id.etMsg);
    swSapaNama = (Switch) findViewById(R.id.swSapaNama);

    btnSend.setOnClickListener(this);
    btnChooseFile.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.btnChooseFile:
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
        break;
      case R.id.btnSend:
        Log.d("is true", swSapaNama.isChecked() + "");
        break;
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

    // The ACTION_OPEN_DOCUMENT intent was sent with the request code
    // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
    // response to some other intent, and the code below shouldn't run at all.

    if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      // The document selected by the user won't be returned in the intent.
      // Instead, a URI to that document will be contained in the return intent
      // provided to this method as a parameter.
      // Pull that URI using resultData.getData().
      Uri uri = null;
      if (resultData != null) {
        uri = resultData.getData();
        Log.i("uri file choosed", "Uri: " + uri.toString() + " path: " + uri.getPath());
        try {
          InputStream inputStream = getContentResolver().openInputStream(uri);
          InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
          CSVReader csvReader = new CSVReader(inputStreamReader, ',');
          List<String[]> forms = csvReader.readAll();
          List<String[]> skipFirstRow = forms.subList(1, forms.size());
          Stream.of(skipFirstRow).forEach(s -> Log.d("csv content", TextUtils.join(" ", s)));
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }


}
