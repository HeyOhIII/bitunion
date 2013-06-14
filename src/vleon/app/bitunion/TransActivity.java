package vleon.app.bitunion;

import vleon.app.bitunion.api.BuAPI;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class TransActivity extends SherlockActivity {

	String mUsername, mPassword;
	int mNetType;
	boolean mAutoLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		readConfig();
		// ��ȡ������û���Ϣ�ɹ����û������������Զ���¼����ת��������
		// ������ת����¼����
//		Toast.makeText(this, "���ɽ���", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.putExtra("username", mUsername);
		intent.putExtra("password", mPassword);
		intent.putExtra("nettype", mNetType);
		intent.putExtra("autologin", mAutoLogin);
		if (mUsername != null && mPassword != null && mAutoLogin) {
//			Toast.makeText(this, "��ת��������", Toast.LENGTH_SHORT).show();
			intent.setClass(this, MainActivity.class);
		} else {
//			Toast.makeText(this, "��ת����¼����", Toast.LENGTH_SHORT).show();
			intent.setClass(this, LoginActivity.class);
		}
		startActivity(intent);
		finish();
	}

	/*
	 * ��ȡ�ͱ����û�����
	 */
	public void saveConfig() {
		SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
		SharedPreferences.Editor editor = config.edit();
		editor.putInt("nettype", mNetType);
		editor.putString("username", mUsername);
		editor.putString("password", mPassword);
		editor.putBoolean("autologin", mAutoLogin);
		editor.commit();
	}

	public void readConfig() {
		SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
		mNetType = config.getInt("nettype", BuAPI.BITNET);
		// mStartFid = config.getInt("startfid", 14);
		mUsername = config.getString("username", null);
		mPassword = config.getString("password", null);
		mAutoLogin = config.getBoolean("autologin", false);
	}
	

}
