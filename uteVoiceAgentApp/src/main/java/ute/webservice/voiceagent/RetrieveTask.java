package ute.webservice.voiceagent;

import android.os.AsyncTask;
import java.util.ArrayList;
import javax.net.ssl.SSLContext;

/**
 * Create AsyncTask thread to send a query received from DialogFlow to the webservice and display response.
 * After creating an instance of RetrieveTask, call addListener(this) and then call execute().
 * Get the information received from the server in onRetrieved (required for RetrievalListeners)
 * Created by Nathan Taylor on 3/14/18
 */
class RetrieveTask extends AsyncTask<Void,Integer,String> {

    private Exception exception;
    private DataAsked dataAsked;
    private SSLContext sslContext;

    private ArrayList<RetrievalListener> listeners;

    public RetrieveTask(DataAsked dataAsked, SSLContext sslContext){
        this.dataAsked = dataAsked;
        this.sslContext = sslContext;
        listeners = new ArrayList<>();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String data=null;
        try {
            data = dataAsked.getHttpClientReply(sslContext);
        } catch (Exception e) {
            this.exception = e;
        }
        return data;
    }

    @Override
    protected void onPostExecute(String str){
        if (str != null){
            for (RetrievalListener listener : listeners) {
                listener.onRetrieval(str);
            }
        }
    }

    /**
     * Register to be notified when a result is obtained from the server.
     * @param listener the RetrievalListener to be notified.
     */
    public void addListener(RetrievalListener listener) {
        listeners.add(listener);
    }

}