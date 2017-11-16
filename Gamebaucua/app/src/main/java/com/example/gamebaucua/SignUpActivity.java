package com.example.gamebaucua;

import android.app.Activity;
import android.app.ProgressDialog;
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
    NotificationDialog notificationDialog;
    ProgressDialog progressDialog;
    Socket mSocket = LoginActivity.mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsername = (EditText) findViewById(R.id.etUsernameSignUp);
        etPassword = (EditText) findViewById(R.id.etPasswordSignUp);
        etRetypePassword = (EditText) findViewById(R.id.etRetypePasswordSignUp);
        btnSuccess = (Button) findViewById(R.id.btnSuccessSignUp);

        btnSuccess.setOnClickListener(this);

        notificationDialog = new NotificationDialog(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý");

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
                    notificationDialog.showMessage("Thông báo", "Vui lòng điền đủ thông tin");
                    return;
                }
                if (!password.equals(retypepassword)) {
                    notificationDialog.showMessage("Thông báo", "Nhập lại mật khẩu không đúng");
                    return;
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    jsonObject.put("password", password);
                    //Gọi tới server để đăng ký tài khoản
                    mSocket.emit("sign-up", jsonObject);
                    progressDialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Log.d("error", e.getMessage());
                    notificationDialog.showMessage("Thông báo", "Không thể đăng ký, vui lòng thử lại sau");
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

    //Xử lý sự kiện đăng ký thành công
    private Emitter.Listener onNewMessage_SignUpSuccess = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        progressDialog.dismiss();
                        String success = data.getString("success");
                        notificationDialog.showMessage("Thông báo", success);
                        clearInfo();
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

    public void clearInfo() {
        etUsername.setText("");
        etPassword.setText("");
        etRetypePassword.setText("");
    }
}
