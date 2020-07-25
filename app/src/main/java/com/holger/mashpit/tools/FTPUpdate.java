package com.holger.mashpit.tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class FTPUpdate extends AsyncTask<Void, Void, Void> {
    private static final String DEBUG_TAG = "FTPUpdate";

    String IP;
    String user;
    String pass;
    FTPClient myFTP;

    private WeakReference<Context> weakContext;
    private String localDirPath;

    public FTPUpdate(Context mContext, String IP, String user, String pass, String path)
    {
        this.IP = IP;
        this.localDirPath = path;
        this.user=user;
        this.pass=pass;
        this.weakContext = new WeakReference<>(mContext);
        execute();

    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            String remoteDirPath = "/lfs.img";
            UploadFileByFTP(IP,user,pass,remoteDirPath,localDirPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private ProgressDialog mProgress;
    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        mProgress = new ProgressDialog(weakContext.get());
        mProgress.setMessage("Uploading new image .. Please wait...");
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mProgress.dismiss(); //Dismiss the above Dialogue
    }

    public void UploadFileByFTP(final String IP, String userName,String password, String serverFilePath, String localFilePath) throws Exception {
        mProgress.setMessage("Uploading new image .. Please wait...");
        for (int i = 0; i < 5; i++) {
            Log.i(DEBUG_TAG, "Connecting to: " + IP+" attempt: "+i);
            myFTP=connectFTP(IP);
            if(myFTP!=null)
            {
                Log.i(DEBUG_TAG, "Connecting to: " + IP+" successful!");
                break;
            }
            Thread.sleep(5000);
        }

        try {
            if (!myFTP.login(userName, password)) {
                myFTP.logout();
            }
            myFTP.setFileType(FTPClient.BINARY_FILE_TYPE);
            myFTP.enterLocalPassiveMode();
//            ftp.setBufferSize(2024*2048);//To increase the  download speed
            File file = new File(localFilePath);
            final int lenghtOfFile = Integer.parseInt(String.valueOf(file.length()));
            InputStream input = new FileInputStream(localFilePath);
            CountingInputStream cis = new CountingInputStream(input) {
                protected void beforeRead(int n) throws IOException {
                    super.beforeRead(n);
                    int percent = (getCount() * 100) / lenghtOfFile;
                    Log.d(DEBUG_TAG, "bytesTransferred /uploaded"+percent);
                    Log.d(DEBUG_TAG,"Uploaded "+getCount() + "/" + percent);
                    mProgress.setProgress(percent);
                }
            };
            myFTP.storeFile(serverFilePath, cis);
            input.close();
            myFTP.noop(); // check that control connection is working OK
            myFTP.logout();
        }
        catch (FTPConnectionClosedException e) {
            Log.d(DEBUG_TAG, "ERROR FTPConnectionClosedException:"+e.toString());
            throw e;
        } catch (IOException e) {
            Log.d(DEBUG_TAG, "ERROR IOException:"+e.toString());
            throw e;
        } catch (Exception e) {
            Log.d(DEBUG_TAG, "ERROR Exception:"+e.toString());
            throw e;
        } finally {
            if (myFTP.isConnected()) {
                myFTP.disconnect();
            }
        }
    }

    public FTPClient connectFTP(String IP) {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(IP);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                Log.d(DEBUG_TAG, "Connection Error");
                ftp.disconnect();
                return null;
            }
        } catch (IOException e) {
            Log.d(DEBUG_TAG, "Connection Error");
            return null;
        }
        return ftp;
    }
}