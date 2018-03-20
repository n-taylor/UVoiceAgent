package ute.webservice.voiceagent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Created by u0450254 on 3/13/2018.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.widget.ExpandableListView;
import android.widget.TextView;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;

import static ute.webservice.voiceagent.Constants.CLINWEB_CENSUS_SPECFIC_QUERY;

public class AIListActivity extends BaseActivity implements AIButton.AIButtonListener, RetrievalListener {

    private static String TAG = AIListActivity.class.getName();

    ListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    
    private Button cancelButton;
    private AIButton aiButton;
    private TextView queryTextView;

    private DataAsked dataAsked;
    private ParseResult PR;
    private String query;

    SharedData sessiondata;
    private String accountID;
    private int account_access;

    //CA variables
//    private CertificateFactory cf = null;
//    private Certificate ca;
//    private SSLContext sslContext = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aibutton_sample_accordion);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.resultListView);

        // preparing list data
        dummyListData();

        expListView.setBackgroundResource(R.drawable.menushape);

        listAdapter = new ListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {

                if (i == 0) {
                    System.out.println("gzero");

                    if (i1 == 0) {

                        query = "2A";

                        dataAsked.setCensusUnit("2A");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("2A has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                       // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 1)
                    {
                        query = "2B";

                        dataAsked.setCensusUnit("2B");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("2B has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 2)
                    {
                        query = "2EAST";

                        dataAsked.setCensusUnit("2EAST");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("2 EAST has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 3)
                    {
                        query = "2NORTH";

                        dataAsked.setCensusUnit("2NORTH");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("2 NORTH has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 4)
                    {
                        query = "2SOUTH";

                        dataAsked.setCensusUnit("2SOUTH");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("2 SOUTH has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }

                    else if (i1 == 5)
                    {
                        query = "3NORTH";

                        dataAsked.setCensusUnit("3NORTH");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("3 NORTH has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 6)
                    {
                        query = "3SOUTH";

                        dataAsked.setCensusUnit("3SOUTH");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("3 SOUTH has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 7)
                    {
                        query = "4NORTH";

                        dataAsked.setCensusUnit("4NORTH");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("4 NORTH has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 8)
                    {
                        query = "4SOUTH";

                        dataAsked.setCensusUnit("4SOUTH");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("4 SOUTH has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 9)
                    {
                        query = "4NORTH";

                        dataAsked.setCensusUnit("4NORTH");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("4 NORTH has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                }
                else if  (i == 1) {

                    if (i1 == 0) {

                        query = "5STB";

                        dataAsked.setCensusUnit("5STB");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("5STB has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 1)
                    {
                        query = "5W";

                        dataAsked.setCensusUnit("5W");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("5W has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 2)
                    {
                        query = "AIMA";

                        dataAsked.setCensusUnit("AIMA");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("AIMA has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 3)
                    {
                        query = "AIMB";

                        dataAsked.setCensusUnit("AIMB");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("AIMB has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 4)
                    {
                        query = "BRN";

                        dataAsked.setCensusUnit("BRN");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("BRN has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }

                    else if (i1 == 5)
                    {
                        query = "CVICU";

                        dataAsked.setCensusUnit("CVICU");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("CVICU has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 6)
                    {
                        query = "CVMU";

                        dataAsked.setCensusUnit("CVMU");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("CVMU has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 7)
                    {
                        query = "ICN";

                        dataAsked.setCensusUnit("ICN");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("ICN has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 8)
                    {
                        query = "IMR";

                        dataAsked.setCensusUnit("IMR");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("IMR has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 9)
                    {
                        query = "LND";

                        dataAsked.setCensusUnit("LND");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("LND has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 10)
                    {
                        query = "MICU";

                        dataAsked.setCensusUnit("MICU");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("MICU has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 11)
                    {
                        query = "MNBC";

                        dataAsked.setCensusUnit("MNBC");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("MNBC has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 12)
                    {
                        query = "NAC";

                        dataAsked.setCensusUnit("NAC");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("NAC has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 13)
                    {
                        query = "NCCU";

                        dataAsked.setCensusUnit("NCCU");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("NCCU has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 14)
                    {
                        query = "NICU";

                        dataAsked.setCensusUnit("NICU");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("NICU has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 15)
                    {
                        query = "NNCCN";

                        dataAsked.setCensusUnit("NNCCN");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("NNCCN has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 16)
                    {
                        query = "NSY";

                        dataAsked.setCensusUnit("NSY");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("NSY has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 17)
                    {
                        query = "OBGY";

                        dataAsked.setCensusUnit("OBGY");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("OBGY has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 18)
                    {
                        query = "OTSS";

                        dataAsked.setCensusUnit("OTSS");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("OTSS has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 19)
                    {
                        query = "SICU";

                        dataAsked.setCensusUnit("SICU");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("SICU has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 20)
                    {
                        query = "SSTU";

                        dataAsked.setCensusUnit("SSTU");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("SSTU has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 21)
                    {
                        query = "WP5";

                        dataAsked.setCensusUnit("WP5");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("WP5 has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                }
                else if (i ==2)
                {
                    if (i1 == 0)
                    {
                    query = "HCBMT";

                    dataAsked.setCensusUnit("HCBMT");
                    dataAsked.setCurrentAction("getCensus");
                    dataAsked.setCurrentReply("HCBMT has this many beds remaning:");
                    dataAsked.setIncomplete(false);
                    dataAsked.setCurrentSurgeryCategory("");
                    // Log.d("OUTPUTRESPONSE", PR.get_reply());

                    RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                    httpTask.addListener(AIListActivity.this);
                    httpTask.execute();
                    }
                    else if (i1 == 1)
                    {
                        query = "HCH4";

                        dataAsked.setCensusUnit("HCH4");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("HCH4 has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 2)
                    {
                        query = "HCH5";

                        dataAsked.setCensusUnit("HCH5");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("HCH5 has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                    else if (i1 == 3)
                    {
                        query = "HCICU";

                        dataAsked.setCensusUnit("HCICU");
                        dataAsked.setCurrentAction("getCensus");
                        dataAsked.setCurrentReply("HCICU has this many beds remaning:");
                        dataAsked.setIncomplete(false);
                        dataAsked.setCurrentSurgeryCategory("");
                        // Log.d("OUTPUTRESPONSE", PR.get_reply());

                        RetrieveTask httpTask = new RetrieveTask(dataAsked, CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                        httpTask.addListener(AIListActivity.this);
                        httpTask.execute();
                    }
                }
                return false;
            }
        });





        aiButton = (AIButton) findViewById(R.id.micButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        queryTextView = (TextView) findViewById(R.id.querytextView);

        //Open shared data
        sessiondata = new SharedData(getApplicationContext());
        accountID = sessiondata.getKeyAccount();
        account_access = sessiondata.getKeyAccess();

        //Set up action bar by toolbar
        Toolbar settintTB= (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(settintTB);

        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        config.setRecognizerStartSound(getResources().openRawResourceFd(R.raw.test_start));
        config.setRecognizerStopSound(getResources().openRawResourceFd(R.raw.test_stop));
        config.setRecognizerCancelSound(getResources().openRawResourceFd(R.raw.test_cancel));

        // set up the microphone/AI button
        aiButton.initialize(config);
        aiButton.setResultsListener(this);

        // set up the cancel button
        cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // stop any speech that is being played currently
                TTS.stop();
            }
        });
        dataAsked = new DataAsked();

        //this.loadCA();

    }

//    private void loadCA(){
//        System.out.println("working:"+System.getProperty("user.dir"));
//        // Load CAs from an InputStream
//        // (could be from a resource or ByteArrayInputStream or ...)
//        //CertificateFactory cf = null;
//        try {
//            cf = CertificateFactory.getInstance("X.509");
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        }
//        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
//        InputStream caInput = null;
//        try {
//            caInput = new BufferedInputStream(this.getBaseContext().getAssets().open("ca.cer"));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //Certificate ca;
//        try {
//            ca = cf.generateCertificate(caInput);
//            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                caInput.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        // Create a KeyStore containing our trusted CAs
//        String keyStoreType = KeyStore.getDefaultType();
//        KeyStore keyStore = null;
//        try {
//            keyStore = KeyStore.getInstance(keyStoreType);
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        }
//        try {
//            keyStore.load(null, null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        }
//        try {
//            keyStore.setCertificateEntry("ca", ca);
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        }
//
//        // Create a TrustManager that trusts the CAs in our KeyStore
//        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
//        TrustManagerFactory tmf = null;
//        try {
//            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        try {
//            tmf.init(keyStore);
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        }
//
//        // Create an SSLContext that uses our TrustManager
//
//        try {
//            sslContext = SSLContext.getInstance("TLS");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        try {
//            sslContext.init(null, tmf.getTrustManagers(), null);
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        }
//    }

    /*
     * Preparing the list data
     */
    private void dummyListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("U Neuro Institute");
        listDataHeader.add("University Hospitals");
        listDataHeader.add("Huntsman Cancer Institute");

        // Adding child data

        List<String> UNI = new ArrayList<String>();
        UNI.add("2A");
        UNI.add("2B");
        UNI.add("2EAST");
        UNI.add("2NORTH");
        UNI.add("2SOUTH");
        UNI.add("3NORTH");
        UNI.add("3SOUTH");
        UNI.add("4NORTH");
        UNI.add("4SOUTH");

        List<String> UH = new ArrayList<String>();
        UH.add("5STB");
        UH.add("5W");
        UH.add("AIMA");
        UH.add("AIMB");
        UH.add("BRN");
        UH.add("CVICU");
        UH.add("CVMU");
        UH.add("ICN");
        UH.add("IMR");
        UH.add("LND");
        UH.add("MICU");
        UH.add("MNBC");
        UH.add("NAC");
        UH.add("NCCU");
        UH.add("NICU");
        UH.add("NNCCN");
        UH.add("NSY");
        UH.add("OBGY");
        UH.add("OTSS");
        UH.add("SICU");
        UH.add("SSTU");
        UH.add("WP5");

        List<String> HC = new ArrayList<String>();
        HC.add("HCBMT ");
        HC.add("HCH4");
        HC.add("HCH5");
        HC.add("HCICU");
      

        listDataChild.put(listDataHeader.get(0), UNI);
        listDataChild.put(listDataHeader.get(1), UH);
        listDataChild.put(listDataHeader.get(2), HC);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // use this method to disconnect from speech recognition service
        // Not destroying the SpeechRecognition object in onPause method would block other apps from using SpeechRecognition service
        aiButton.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // use this method to reinit connection to recognition service
        aiButton.resume();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_aibutton_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_logout:
                // Create an LogoutTask and execute it to logout
                LogoutTask httpTask = new LogoutTask(this);
                httpTask.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Show response from the API.AI server,
     * If parameters are enough and user said "Yes", try to get data from webservice.
     * @param response
     */
    @Override
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                PR = new ParseResult(response);

                query = PR.get_ResolvedQuery();

                dataAsked.setIncomplete(PR.get_ActionIncomplete());
                dataAsked.setCurrentReply(PR.get_reply());
                dataAsked.setCensusUnit(PR.getCensusUnit());
                dataAsked.setCurrentSurgeryCategory(PR.get_param_Surgery());
                dataAsked.setCurrentAction(PR.get_Action());
                Log.d("OUTPUTRESPONSE", PR.get_reply());

                // Retrieve the information and display the results
                RetrieveTask httpTask = new RetrieveTask(dataAsked,
                        CertificateManager.getSSlContext(AIListActivity.this)); // the task to retrieve the information
                httpTask.addListener(AIListActivity.this);
                httpTask.execute();
            }

        });
    }

    @Override
    public void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onError");
                queryTextView.setText("Please try again");
            }
        });
    }

    @Override
    public void onCancelled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onCancelled");
                queryTextView.setText("");
            }
        });
    }

    /**
     * If the query needs to be more specific (i.e. a surgery type or unit name), open the
     * appropriate Activity. Else open the activity to display the results.
     * @param result the result of what what retrieved from the server
     */
    @Override
    public void onRetrieval(String result) {
        if (dataAsked.isIncomplete()){
            if (dataAsked.getCurrentAction().equals(Constants.GET_CENSUS)){
                // TODO: Send to the activity that will prompt for a unit name
            }
            else if (dataAsked.getCurrentAction().equals(Constants.GET_SURGERY_COST)){
                // TODO: Send to the activity that will prompt for a surgery category
            }
        }
        else {
            // open a ResultsActivity with the query and the corresponding result
            Intent intent = new Intent(this, ResultsActivity.class);

            if (result.contains("[{"))
            {
                int aindex = result.indexOf("available");

                aindex += 10;

                int bindex = result.indexOf(",", aindex);

                String sAnswer = result.substring(aindex+1,bindex);

                int answer = Integer.parseInt(sAnswer);

                String beds = "beds";

                if (answer == 1)
                {
                    beds = "bed";
                }

                int sindex = result.indexOf("has");



                result = result.substring(0,sindex)+"has "+sAnswer+" "+beds+" available";
            }
            intent.putExtra("query", query);
            intent.putExtra("result", result);
            startActivity(intent);
        }
    }




}
