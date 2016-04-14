package org.mamkschools.mhs.fbla_mobileapp_2016.lib;


import android.content.Context;

import org.json.JSONObject;
import org.mamkschools.mhs.fbla_mobileapp_2016.R;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Handles secure server communications
 * SecureAPI singleton class
 * Created by Yi Zhao
 */
public class SecureAPI {

    private static SecureAPI mySecureAPI = null;
    private SSLSocketFactory mySocketFactory = null;



    public static SecureAPI getInstance(Context c){
        if( mySecureAPI == null){
            try {
                mySecureAPI = new SecureAPI(c.getApplicationContext()
                        .getResources().openRawResource(R.raw.servercert));
            }catch (Exception e){
                Debug.log(e.getMessage());
            }
        }
        return mySecureAPI;

    }
    public static SecureAPI getInstance() throws Exception{
        if( mySecureAPI == null){
            throw new NullPointerException();
        }
        return getInstance(null);
    }
    private void createSocketFactory(InputStream is) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate certificate = certificateFactory.generateCertificate(is);
        Debug.log("ca=" + ((X509Certificate) certificate).getSubjectDN());


        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", certificate);

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        mySocketFactory = sslContext.getSocketFactory();
    }

    private String getPostString(Map<String, String> params) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> p : params.entrySet()) {
            if(first)
                first = false;
            else
                stringBuilder.append("&");

            stringBuilder.append(URLEncoder.encode(p.getKey(), "UTF-8"));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(p.getValue(), "UTF-8"));
        }
        return stringBuilder.toString();
    }

    private String getResponseFromStream(InputStream is) {
        Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private SecureAPI(InputStream inputStream) throws Exception {
        this.createSocketFactory(inputStream);
    }

   public JSONObject HTTPSGET(String action) throws Exception {
        URL url = new URL(Constants.API_BASE_URL + action);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setReadTimeout(10000);
        httpsURLConnection.setConnectTimeout(15000);
        httpsURLConnection.setSSLSocketFactory(mySocketFactory);

        String response = getResponseFromStream(httpsURLConnection.getInputStream());
        return new JSONObject(response);
    }

    public void HTTPSFETCHPIC(String action, File file) throws Exception {
        URL url = new URL(Constants.API_BASE_URL + action);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setSSLSocketFactory(mySocketFactory);

        InputStream input = new BufferedInputStream(httpsURLConnection.getInputStream(), 8192);
        // Output stream
        OutputStream output = new FileOutputStream(file);

        int count;
        byte data[] = new byte[1024];

        while ((count = input.read(data)) != -1) {
            output.write(data, 0, count);
        }
        output.flush();
        output.close();
        input.close();
    }

    public JSONObject HTTPSPOST(String action, Map<String, String> params) throws Exception {
        URL url = new URL(Constants.API_BASE_URL + action);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();

        httpsURLConnection.setSSLSocketFactory(mySocketFactory);
        httpsURLConnection.setReadTimeout(10000);
        httpsURLConnection.setConnectTimeout(15000);
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setDoOutput(true);

        OutputStream outputStream = httpsURLConnection.getOutputStream();
        BufferedWriter bufferedWriter =
                new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        bufferedWriter.write(getPostString(params));
        bufferedWriter.flush();
        bufferedWriter.close();
        outputStream.close();
        httpsURLConnection.connect();

        String response = getResponseFromStream(httpsURLConnection.getInputStream());
        return new JSONObject(response);
    }

    public JSONObject HTTPSPOSTMULTI(String action, Map<String, String> params, Map<String, ByteArrayOutputStream> files, Context ctx) throws Exception {
        URL url = new URL(Constants.API_BASE_URL + action);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();

        //Constants
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        httpsURLConnection.setSSLSocketFactory(mySocketFactory);
        httpsURLConnection.setReadTimeout(10000);
        httpsURLConnection.setConnectTimeout(15000);
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setRequestProperty("Connection", "Keep-Alive");
        httpsURLConnection.setRequestProperty("Cache-Control", "no-cache");
        httpsURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        DataOutputStream request = new DataOutputStream(
                httpsURLConnection.getOutputStream());

        //Process params
        for (Map.Entry<String, String> entry : params.entrySet()) {
            //Start wrapping
            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + crlf);
            request.writeBytes(crlf);

            request.writeBytes(entry.getValue());

            //End wrap
            request.writeBytes(crlf);
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
        }

        //Process files
        int it = 0;
        for (Map.Entry<String, ByteArrayOutputStream> entry : files.entrySet())
        {
            //File wrapping
            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" +
                    entry.getKey() + "\";filename=\"" +
                    it + "\"" + crlf);
            request.writeBytes(crlf);

            byte [] data = entry.getValue().toByteArray();

            request.write(data, 0, data.length);

            //End file wrapping
            request.writeBytes(crlf);
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
            ++it;
        }

        //Write and connect
        request.flush();
        request.close();
        httpsURLConnection.connect();

        String response = getResponseFromStream(httpsURLConnection.getInputStream());
        return new JSONObject(response);
    }
}
