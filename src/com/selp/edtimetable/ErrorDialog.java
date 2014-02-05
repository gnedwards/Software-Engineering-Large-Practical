package com.selp.edtimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.DialogInterface.OnClickListener;
import android.util.Log;
/**Error dialog that appears if there are any errors in the app, informing the user of the problem
 * and giving the option to exit the app.
 **/
public class ErrorDialog extends Activity{
	public ErrorDialog(){
		
	}
	public void show(String message, final Context context, final Context baseContext) {
		String TAG = "ErrorDialog";
		AlertDialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		
		builder.setPositiveButton("Exit", new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				android.os.Process.killProcess(android.os.Process.myPid());
			}

		});
		dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		Log.i(TAG,"Dialog showing");
	}
}
