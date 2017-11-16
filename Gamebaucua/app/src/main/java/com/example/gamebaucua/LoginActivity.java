package com.example.gamebaucua;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class LoginActivity extends Activity implements View.OnClickListener {

    EditText etUsername, etPassword;
    Button btnLogin, btnSignUp;
    NotificationDialog notificationDialog;
    ProgressDialog progressDialog;

    public static Socket mSocket;

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
        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.etUsernameLogin);
        etPassword = (EditText) findViewById(R.id.etPasswordLogin);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        btnLogin.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);

        notificationDialog = new NotificationDialog(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý");

        mSocket.connect(); //Kết nốt socketio

        //Lắng nghe sự kiện đăng nhập thành công
        mSocket.on("sign-in-success", onNewMessage_SignInSuccess);
        //Lắng nghe sự kiện đăng nhập thất bại
        mSocket.on("sign-in-fail", onNewMessage_SignInFail);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (username.equals("") || password.equals("")) {
                    notificationDialog.showMessage("Thông báo", "Vui lòng điền đủ thông tin");
                    return;
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    jsonObject.put("password", password);
                    //Gọi tới server để đăng nhập tài khoản
                    mSocket.emit("sign-in", jsonObject);
                    progressDialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Log.d("error", e.getMessage());
                    notificationDialog.showMessage("Thông báo", "Không thể đăng nhập, vui lòng thử lại sau");
                }
                break;
            case R.id.btnSignUp:
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                break;
        }
    }

    //Xử lý sự kiện đăng nhập thất bại
    private Emitter.Listener onNewMessage_SignInFail = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        progressDialog.dismiss();
                        String error = data.getString("error");
                        notificationDialog.showMessage("Thông báo", error);
                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                        Log.d("error", e.getMessage());
                        notificationDialog.showMessage("Thông báo", "Không thể đăng ký, vui lòng thử lại sau");
                    }
                }
            });
        }
    };

    //Xử lý sự kiện đăng nhập thành công
    private Emitter.Listener onNewMessage_SignInSuccess = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    notificationDialog.showMessage("Thông báo", "Đăng nhập thành công");
                }
            });
        }
    };
}
