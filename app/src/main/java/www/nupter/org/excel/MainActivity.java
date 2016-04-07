package www.nupter.org.excel;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {

    private Button selectButton;

    private Button checkButton;

    private String path = "";

    private ListView mylist;

    private EditText editText;

    private Button sendButton;

    private TextView resultText;

    private List<String> phones;

    private int i;

    private String content;


    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            resultText.setText("共" + phones.size() + "条" + "已发送" + i + "条");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectButton = (Button) findViewById(R.id.select);
        checkButton = (Button) findViewById(R.id.check);
        checkButton.setVisibility(View.GONE);
        sendButton = (Button) findViewById(R.id.send);
        editText = (EditText) findViewById(R.id.content);
        mylist = (ListView) findViewById(R.id.mylist);
        resultText=(TextView)findViewById(R.id.result);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"), 1);
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phones = getList(path);
                Log.i("phonesize", "" + phones.size());
                Myadapter myadapter = new Myadapter(phones);
                mylist.setAdapter(myadapter);

                resultText.setText("共" + phones.size() + "条" + "已发送0条");

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content=editText.getText().toString();
                Toast.makeText(MainActivity.this,content,Toast.LENGTH_SHORT).show();
                Log.i("fang", content);
                new MyThread().start();
            }
        });

    }


    private class MyThread extends Thread{
        @Override
        public void run() {

            for ( i=0;i<phones.size();i++){
                sendMessag(phones.get(i),content,i+1,phones.size());
                try {

                    Thread.sleep(1000);
                    Message message=new Message();
                    message.what=1;
                    handler.sendMessage(message);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public List getList(String path) {
        List<String> strings = new ArrayList<>();
        jxl.Workbook readwb = null;
        try {
                                          InputStream instream = new FileInputStream(path);
            try {
                readwb = Workbook.getWorkbook(instream);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BiffException e) {
                e.printStackTrace();
            }

            //Sheet的下标是从0开始
            //获取第一张Sheet表
            Sheet readsheet = readwb.getSheet(0);
            //获取Sheet表中所包含的总行数
            int rsRows = readsheet.getRows();

            //获取指定单元格的对象引用
            for (int j = 0; j < rsRows; j++)

            {
                Cell cell = readsheet.getCell(0, j);
                String phone = cell.getContents();
                System.out.print(phone + " ");
                String regExp = "^[0-9]{11}$";
                Pattern p = Pattern.compile(regExp);
                Matcher m = p.matcher(cell.getContents());
                if (m.find()) {
                    strings.add(phone);
                }
            }

            System.out.println();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return strings;
    };

    public void sendMessag(String phone,String message,int i,int num ){
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> msgs = smsManager.divideMessage(message);
        for (String msg : msgs) {
            if (msg != null) {
                smsManager.sendTextMessage(phone, null, msg, null, null);
            }
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = FileUtils.getPath(this, uri);

            Toast.makeText(MainActivity.this, path, Toast.LENGTH_SHORT).show();
            this.path = path;
            checkButton.setVisibility(View.VISIBLE);
        }

    }

    private class Myadapter extends BaseAdapter {

        private List<String> strings;
        private LayoutInflater layoutInflater;

        public Myadapter(List<String> strings) {
            this.strings = strings;
            layoutInflater = LayoutInflater.from(MainActivity.this);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return strings.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = layoutInflater.inflate(R.layout.list_item, parent, false);
            TextView phoneText = (TextView) convertView.findViewById(R.id.phone);
            phoneText.setText(strings.get(position));
            return convertView;
        }
    }
}
