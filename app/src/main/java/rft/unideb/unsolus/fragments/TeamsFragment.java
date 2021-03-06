package rft.unideb.unsolus.fragments;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rft.unideb.unsolus.PlayerManagementActivity;
import rft.unideb.unsolus.R;
import rft.unideb.unsolus.TeamManagementActivity;
import rft.unideb.unsolus.entities.Player;
import rft.unideb.unsolus.entities.Team;
import rft.unideb.unsolus.entities.Teammember;
import rft.unideb.unsolus.network.ApiService;
import rft.unideb.unsolus.network.RetrofitBuilder;
import rft.unideb.unsolus.others.ExpandableListAdapter;
import rft.unideb.unsolus.others.TokenManager;

import static android.content.ContentValues.TAG;


public class TeamsFragment extends Fragment {

    private Button playerManagement;
    private Button teamManagement;

    private ApiService service;
    private TokenManager tokenManager;
    private Call<Team> call;
    private Call<Teammember> membersManage;
    private Call<List<Team>> teamsList;
    private Call<List<Player>> membersList;

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expandableListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    private int counter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teams, container, false);

        playerManagement = (Button) view.findViewById(R.id.btn_managePlayer);
        teamManagement = (Button) view.findViewById(R.id.btn_manageTeam);

        teamManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), TeamManagementActivity.class));
            }
        });

        playerManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PlayerManagementActivity.class));
            }
        });

        tokenManager = TokenManager.getInstance(this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithToken(ApiService.class,tokenManager);

        expandableListView = (ExpandableListView) view.findViewById(R.id.teams_Expandable);
        getAllTeam();
        listAdapter = new ExpandableListAdapter(this.getActivity(), listDataHeader, listDataChild);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    String actID;// = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).substring(10,11);
                    String[] actIDs = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).split(" ");
                    actID = actIDs[3];
                    getPlayerPerTeam(Integer.parseInt(actID));
                return false;
            }
        });


        expandableListView.setAdapter(listAdapter);

        return view;
    }

    private void getPlayerPerTeam(int id) {
        final List<String> members = new ArrayList<String>();
        membersList = service.getPlayersPerTeam(id, tokenManager.getToken().getToken());
        membersList.enqueue(new Callback<List<Player>>() {
            @Override
            public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                Log.w(TAG, "onResponse: " + response );
                if (response.isSuccessful()){
                    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(getActivity());
                    dlgAlert.setTitle("Team members");
                    for (Player p : response.body()){
                           members.add(p.getGamerName());
                    }
                    String names = new String();
                    for (int i = 0; i< members.size(); i++)
                        names = names + members.get(i) + "\n";

                    dlgAlert.setMessage(names);

                    dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dlgAlert.setCancelable(true);
                    dlgAlert.create().show();
                }
            }

            @Override
            public void onFailure(Call<List<Player>> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });
    }

    private void getAllTeam() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        listDataHeader.add(" ");
        counter = 0;
        List<String> db = new ArrayList<String>();
        db.add(" ");
        listDataChild.put(listDataHeader.get(0), db);

        teamsList = service.getAllTeam(tokenManager.getToken().getToken());
        teamsList.enqueue(new Callback<List<Team>>() {
            @Override
            public void onResponse(Call<List<Team>> call, Response<List<Team>> response) {
                Log.w(TAG, "onResponse: " + response );
                if (response.isSuccessful()){
                    List<String> stuffsFromTeams;
                    for(Team m : response.body()){
                        listDataHeader.add(m.getTeamname().toString());
                        counter++;
                        stuffsFromTeams = new ArrayList<String>();
                        stuffsFromTeams.add("Team ID - " + m.getTeamID()
                                            + " \n \t Game - " + m.getGamename()
                                            + " \n \t Team goal - "+  m.getTeamgoal()
                                            + " \n \t Server - " + m.getServer()
                                            + " \n \t Language - " + m.getLanguage());
                       // getPlayerPerTeam(m.getTeamID());
                        listDataChild.put(listDataHeader.get(counter), stuffsFromTeams);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Team>> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
