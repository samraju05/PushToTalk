package com.example.user.pushtotalktest.certificate;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.user.pushtotalktest.R;

import java.io.File;

public class QRPushToTalkCertificateGenerateTask extends AsyncTask<Void, Void, File> {

    private Context context;
    private ProgressDialog loadingDialog;

    public QRPushToTalkCertificateGenerateTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        loadingDialog = new ProgressDialog(context);
        loadingDialog.setIndeterminate(true);
        loadingDialog.setMessage(context.getString(R.string.generateCertProgress));

        /*loadingDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                cancel(true);

            }
        });*/
        loadingDialog.show();
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            return QRPushToTalkCertificateManager.generateCertificate();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (result != null) {
            Toast.makeText(context, context.getString(R.string.generateCertSuccess, result.getName()), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.generateCertFailure, Toast.LENGTH_SHORT).show();
        }

        loadingDialog.dismiss();
    }
}
