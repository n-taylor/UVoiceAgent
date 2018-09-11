package ute.webservice.voiceagent.util;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDeleteHC4;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DeleteContextsTask extends AsyncTask<Void, Void, String> {

    private static final String DELETE_CONTEXTS = "https://api.dialogflow.com/v1/contexts?sessionId=";

    private String sessionId;

    public DeleteContextsTask(String sessionId){
        this.sessionId = sessionId;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String request = DELETE_CONTEXTS + sessionId;
        StringBuilder responseBuilder = new StringBuilder();
        CloseableHttpClient client = HttpClientBuilder.create().build();

        try {
            HttpDeleteHC4 delete = new HttpDeleteHC4(request);
            delete.addHeader("Authorization", "Bearer " + Config.ACCESS_TOKEN);
            delete.addHeader("Content-Type", "application/json");
            CloseableHttpResponse response = client.execute(delete);
            HttpEntity entity = response.getEntity();

            if (entity != null){
                BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                String line = "";

                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                return responseBuilder.toString();
            }
        }
        catch (Exception ex) {
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response){
        System.out.println(response);
    }
}
