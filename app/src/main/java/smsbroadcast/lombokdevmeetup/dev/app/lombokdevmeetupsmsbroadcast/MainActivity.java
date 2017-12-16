package smsbroadcast.lombokdevmeetup.dev.app.lombokdevmeetupsmsbroadcast;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.opencsv.CSVReader;

import org.apache.commons.text.StrSubstitutor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private Button btnChooseFile, btnSend;
  private EditText etMessage;
  private Switch swSapaNama;
  private List<NamePhone> namePhones;
  private int msgCounter = 0;
  private TextView tvMsgCounter;

  private SmsManager smsManager = SmsManager.getDefault();

  private static final int READ_REQUEST_CODE = 42;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    btnChooseFile = (Button) findViewById(R.id.btnChooseFile);
    btnSend = (Button) findViewById(R.id.btnSend);
    etMessage = (EditText) findViewById(R.id.etMsg);
    swSapaNama = (Switch) findViewById(R.id.swSapaNama);
    tvMsgCounter = (TextView) findViewById(R.id.tvMsgCounter);

    btnSend.setOnClickListener(this);
    btnChooseFile.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.btnChooseFile:
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
        break;
      case R.id.btnSend:
        List<Msg> messages = new ArrayList<>();
        Stream.of(namePhones).forEach(namePhone -> {
          Map<String, String> values = new HashMap<>();
          values.put("name", namePhone.getName());
          StrSubstitutor strSubstitutor = new StrSubstitutor(values, "%(", ")");
          String message = strSubstitutor.replace(etMessage.getText().toString());
          new SendMsgOnBackgound().execute(namePhone.getPhone(), message);
        });
        break;
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

    // The ACTION_OPEN_DOCUMENT intent was sent with the request code
    // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
    // response to some other intent, and the code below shouldn't run at all.

    if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      Uri uri = null;
      if (resultData != null) {
        uri = resultData.getData();
        try {
          InputStream inputStream = getContentResolver().openInputStream(uri);
          InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
          CSVReader csvReader = new CSVReader(inputStreamReader, ',');
          List<String[]> forms = csvReader.readAll();
          namePhones = new ArrayList<>();
          Stream.of(forms.subList(1, forms.size())).forEach(s -> {
            String name = s[2];
            String phone = String.format("0%s", s[3]);
            NamePhone namePhone = new NamePhone(name, phone);
            namePhones.add(namePhone);
          });
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private class Msg {
    private String to, msg;

    public Msg(String to, String msg) {
      this.to = to;
      this.msg = msg;
    }

    public String getTo() {

      return to;
    }

    public void setTo(String to) {
      this.to = to;
    }

    public String getMsg() {
      return msg;
    }

    public void setMsg(String msg) {
      this.msg = msg;
    }
  }

  private class NamePhone {
    private String name, phone;

    public NamePhone(String name, String phone) {
      this.name = name;
      this.phone = phone;
    }

    public String getName() {

      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getPhone() {
      return phone;
    }

    public void setPhone(String phone) {
      this.phone = phone;
    }
  }

  private class SendMsgOnBackgound extends AsyncTask<String, Void, Integer> {

    @Override
    protected void onPreExecute() {
      msgCounter = 0;
    }

    @Override
    protected Integer doInBackground(String... strings) {
      String no = strings[0];
      String msg = strings[1];
      try {
        smsManager.sendTextMessage(no, null, msg, null, null);
        return 1;
      } catch (Exception e) {
        return 0;
      }
    }

    @Override
    protected void onPostExecute(Integer integer) {
      msgCounter += integer;
      tvMsgCounter.setText(String.format("Message sent: %d/%d", msgCounter, namePhones.size()));
    }
  }
}
