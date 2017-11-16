package com.example.gamebaucua;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	GridView gridView;
	Custom_GridView_BanCo adapter;
	Integer[] dsHinh = { R.drawable.nai, R.drawable.bau, R.drawable.ga,R.drawable.ca, R.drawable.cua, R.drawable.tom };
	AnimationDrawable cdXiNgau1, cdXiNgau2, cdXiNgau3;
	ImageView hinhXiNgau1, hinhXiNgau2, hinhXiNgau3;
	Random randomXiNgau;
	int giaTriXiNgau1, giaTriXiNgau2, giaTriXiNgau3;
	public static Integer[] gtDatCuoc = new Integer[6];
	int tongtiencu, tongtienmoi;
	TextView tvTien, tvThoiGian;
	Timer timer = new Timer();
	Handler handler;
	int tienThuong, kiemtra, id_amthanh;
	SharedPreferences luuTru;
	SoundPool amThanhXiNgau = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	MediaPlayer nhacnen = new MediaPlayer();
	CheckBox ktAmthanh;
	CountDownTimer demthoigian;
	
	Callback callback = new Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			RandomXiNgau1();
			RandomXiNgau2();
			RandomXiNgau3();
			
			for (int i = 0; i < gtDatCuoc.length; i++) {
				if(gtDatCuoc[i] != 0){
					if(i == giaTriXiNgau1){
						tienThuong += gtDatCuoc[i];
					}
					if(i == giaTriXiNgau2){
						tienThuong += gtDatCuoc[i];
					}
					if(i == giaTriXiNgau3){
						tienThuong += gtDatCuoc[i];
					}
					if(i != giaTriXiNgau1 && i != giaTriXiNgau2 && i != giaTriXiNgau3){
						tienThuong -= gtDatCuoc[i];
					}
				}
			}
			if(tienThuong > 0){
				Toast.makeText(getApplicationContext(), "Bn đã trúng thưởng  " + tienThuong, Toast.LENGTH_SHORT).show();
			}else if (tienThuong == 0) {
				Toast.makeText(getApplicationContext(), "Hên quá mém thua ! " , Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getApplicationContext(), "Xui quá thua  " + tienThuong + " rồi !" , Toast.LENGTH_SHORT).show();
			}
			
			LuuDuLieuNguoiDung(tienThuong);
			tvTien.setText(String.valueOf(tongtienmoi));
			return false;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		hinhXiNgau1 = (ImageView) findViewById(R.id.imgXingau1);
		hinhXiNgau2 = (ImageView) findViewById(R.id.imgXingau2);
		hinhXiNgau3 = (ImageView) findViewById(R.id.imgXingau3);
		tvTien = (TextView) findViewById(R.id.textTien);
		ktAmthanh = (CheckBox) findViewById(R.id.checkAmthanh);
		tvThoiGian = (TextView) findViewById(R.id.tvThoiGian);
		
		gridView = (GridView) findViewById(R.id.gvBanco);
		adapter = new Custom_GridView_BanCo(this, R.layout.custom_banco, dsHinh);
		gridView.setAdapter(adapter);
		
		luuTru = getSharedPreferences("luutruthongtin", Context.MODE_PRIVATE);
		tongtiencu = luuTru.getInt("TongTien", 1000);
		tvTien.setText(String.valueOf(tongtiencu));
		
		id_amthanh = amThanhXiNgau.load(this, R.raw.dice, 1);
		nhacnen = MediaPlayer.create(this, R.raw.nhacnenbaucua);
		
		nhacnen.start();
		
		ktAmthanh.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean kt) {
				if(kt){
					nhacnen.stop();
				}else{
					try {
						nhacnen.prepare();
						nhacnen.start();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		});
		
		
		demthoigian = new CountDownTimer(180000,1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				long milis = millisUntilFinished;
				long gio = TimeUnit.MILLISECONDS.toHours(milis);
				long phut = TimeUnit.MILLISECONDS.toMinutes(milis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milis));
				long giay = TimeUnit.MILLISECONDS.toSeconds(milis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milis));
				
				String giophutgiay = String.format("%02d:%02d:%02d", gio,phut,giay);
				tvThoiGian.setText(giophutgiay);
			}
			
			@Override
			public void onFinish() {
				Editor edit = luuTru.edit();
				tongtiencu = luuTru.getInt("TongTien", 1000);
			    tongtienmoi = tongtiencu + 1000;
				edit.putInt("TongTien", tongtienmoi );
				edit.commit();
				
				tvTien.setText(String.valueOf(tongtienmoi));
				demthoigian.cancel();
				demthoigian.start();
			}
		};
		
		demthoigian.start();
		handler = new Handler(callback);
	}
	
	private void LuuDuLieuNguoiDung(int tienthuong){
		Editor edit = luuTru.edit();
		tongtiencu = luuTru.getInt("TongTien", 1000);
	    tongtienmoi = tongtiencu + tienthuong;
		edit.putInt("TongTien", tongtienmoi );
		edit.commit();
	}

	public void LacXiNgau(View v) {
		hinhXiNgau1.setImageResource(R.drawable.anhdongxingau1);
		hinhXiNgau2.setImageResource(R.drawable.anhdongxingau2);
		hinhXiNgau3.setImageResource(R.drawable.anhdongxingau3);

		cdXiNgau1 = (AnimationDrawable) hinhXiNgau1.getDrawable();
		cdXiNgau2 = (AnimationDrawable) hinhXiNgau2.getDrawable();
		cdXiNgau3 = (AnimationDrawable) hinhXiNgau3.getDrawable();
		
		kiemtra = 0;
		for (int i = 0; i < gtDatCuoc.length; i++) {
			kiemtra += gtDatCuoc[i];
		}
		
		if(kiemtra == 0){
			Toast.makeText(getApplicationContext(), "Bạn vui lòng đặt cược ! ", Toast.LENGTH_SHORT).show();
		}else{
			if(kiemtra > tongtiencu ){
				Toast.makeText(getApplicationContext(), "Bạn không đủ tiền để đặt cược ! ", Toast.LENGTH_SHORT).show();
			}else{
				
				amThanhXiNgau.play(id_amthanh, 1.0f, 1.0f, 1, 0, 1.0f);
				cdXiNgau1.start();
				cdXiNgau2.start();
				cdXiNgau3.start();
				
				tienThuong = 0;
				timer.schedule(new LacXiNgau(), 1000);
				
			}
			
		}
		
		
		
	}

	class LacXiNgau extends TimerTask {

		@Override
		public void run() {
			handler.sendEmptyMessage(0);
		}
	}

	private void RandomXiNgau1() {

		randomXiNgau = new Random();
		int rd = randomXiNgau.nextInt(6);
		switch (rd) {

		case 0:
			hinhXiNgau1.setImageResource(dsHinh[0]);
			giaTriXiNgau1 = rd;
			break;

		case 1:
			hinhXiNgau1.setImageResource(dsHinh[1]);
			giaTriXiNgau1 = rd;
			break;

		case 2:
			hinhXiNgau1.setImageResource(dsHinh[2]);
			giaTriXiNgau1 = rd;
			break;

		case 3:
			hinhXiNgau1.setImageResource(dsHinh[3]);
			giaTriXiNgau1 = rd;
			break;

		case 4:
			hinhXiNgau1.setImageResource(dsHinh[4]);
			giaTriXiNgau1 = rd;
			break;

		case 5:
			hinhXiNgau1.setImageResource(dsHinh[5]);
			giaTriXiNgau1 = rd;
			break;

		}
	}
	
	private void RandomXiNgau2() {

		randomXiNgau = new Random();
		int rd = randomXiNgau.nextInt(6);
		switch (rd) {

		case 0:
			hinhXiNgau2.setImageResource(dsHinh[0]);
			giaTriXiNgau2 = rd;
			break;

		case 1:
			hinhXiNgau2.setImageResource(dsHinh[1]);
			giaTriXiNgau2 = rd;
			break;

		case 2:
			hinhXiNgau2.setImageResource(dsHinh[2]);
			giaTriXiNgau2 = rd;
			break;

		case 3:
			hinhXiNgau2.setImageResource(dsHinh[3]);
			giaTriXiNgau2 = rd;
			break;

		case 4:
			hinhXiNgau2.setImageResource(dsHinh[4]);
			giaTriXiNgau2 = rd;
			break;

		case 5:
			hinhXiNgau2.setImageResource(dsHinh[5]);
			giaTriXiNgau2 = rd;
			break;

		}
	}
	
	private void RandomXiNgau3() {

		randomXiNgau = new Random();
		int rd = randomXiNgau.nextInt(6);
		switch (rd) {

		case 0:
			hinhXiNgau3.setImageResource(dsHinh[0]);
			giaTriXiNgau3 = rd;
			break;

		case 1:
			hinhXiNgau3.setImageResource(dsHinh[1]);
			giaTriXiNgau3 = rd;
			break;

		case 2:
			hinhXiNgau3.setImageResource(dsHinh[2]);
			giaTriXiNgau3 = rd;
			break;

		case 3:
			hinhXiNgau3.setImageResource(dsHinh[3]);
			giaTriXiNgau3 = rd;
			break;

		case 4:
			hinhXiNgau3.setImageResource(dsHinh[4]);
			giaTriXiNgau3 = rd;
			break;

		case 5:
			hinhXiNgau3.setImageResource(dsHinh[5]);
			giaTriXiNgau3 = rd;
			break;

		}
	}

}
