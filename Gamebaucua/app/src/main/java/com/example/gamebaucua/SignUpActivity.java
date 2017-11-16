package com.example.gamebaucua;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.widget.AppCompatPopupWindow;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class SignUpActivity extends Activity implements View.OnClickListener{

    EditText etUsername, etPassword, etRetypePassword;
    Button btnSuccess;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.1.6:2000");
        } catch (URISyntaxException e) {
            Log.d("error", e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsername = (EditText) findViewById(R.id.etUsernameSignUp);
        etPassword = (EditText) findViewById(R.id.etPasswordSignUp);
        etRetypePassword = (EditText) findViewById(R.id.etRetypePasswordSignUp);
        btnSuccess = (Button) findViewById(R.id.btnSuccessSignUp);

        btnSuccess.setOnClickListener(this);

        mSocket.connect(); //Kết nốt socketio

        //Lắng nghe sự kiện đăng ký thành công
        mSocket.on("sign-up-success", onNewMessage_SignUpSuccess);
        //Lắng nghe sự kiện đăng ký thất bại
        mSocket.on("sign-up-fail", onNewMessage_SignUpFail);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSuccessSignUp:
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String retypepassword = etRetypePassword.getText().toString().trim();
                if (username.equals("") || password.equals("") || retypepassword.equals("")) {
                    Toast.makeText(this, "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(retypepassword)) {
                    Toast.makeText(this, "Nhập lại mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    jsonObject.put("password", password);
                    mSocket.emit("sign-up", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    //Xử lý sự kiện đăng ký thất bại
    private Emitter.Listener onNewMessage_SignUpFail = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String error = data.getString("err");
                        Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("error", e.getMessage());
                    }
                }
            });
        }
    };

    //Xử lý sự kiện đăng ký thành công
    private Emitter.Listener onNewMessage_SignUpSuccess = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String error = data.getString("success");
                        Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("error", e.getMessage());
                    }
                }
            });
        }
    };
}
