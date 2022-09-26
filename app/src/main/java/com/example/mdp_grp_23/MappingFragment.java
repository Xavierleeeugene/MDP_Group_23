package com.example.mdp_grp_23;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class MappingFragment extends Fragment {
    private static final String TAG = "MapFragment";

    SharedPreferences mapPref;
    private static SharedPreferences.Editor editor;

    Button resetMapBtn, updateButton, saveMapObstacle, loadMapObstacle;
    ImageButton directionChangeImageBtn, obstacleImageBtn;
    ToggleButton setStartPointToggleBtn;
    GridMap gridMap;

    Switch dragSwitch;
    Switch changeObstacleSwitch;

    static String imageID="";
    static String imageBearing="North";
    static boolean dragStatus;
    static boolean changeObstacleStatus;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_map_config, container,  false);

        gridMap = MainActivity.getGridMap();
        final DirectionsFragment directionFragment = new DirectionsFragment();

        resetMapBtn = root.findViewById(R.id.resetBtn);
        setStartPointToggleBtn = root.findViewById(R.id.startpointToggleBtn);
        directionChangeImageBtn = root.findViewById(R.id.changeDirectionBtn);
        obstacleImageBtn = root.findViewById(R.id.addObstacleBtn);
        updateButton = root.findViewById(R.id.updateMapBtn);
        saveMapObstacle = root.findViewById(R.id.saveBtn);
        loadMapObstacle = root.findViewById(R.id.loadBtn);
        dragSwitch = root.findViewById(R.id.dragSwitch);
        changeObstacleSwitch = root.findViewById(R.id.changeObstacleSwitch);


        resetMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked resetMapBtn");
                showToast("Reseting map...");
                gridMap.resetMap();
            }
        });

        // switch for dragging
        dragSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                showToast("Dragging is " + (isChecked ? "on" : "off"));
                dragStatus = isChecked;
                if (dragStatus) {
                    gridMap.setSetObstacleStatus(false);
                    changeObstacleSwitch.setChecked(false);
                }
            }
        });

        // switch for changing obstacle
        changeObstacleSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                showToast("Changing Obstacle is " + (isChecked ? "on" : "off"));
                changeObstacleStatus = isChecked;
                if (changeObstacleStatus) {
                    gridMap.setSetObstacleStatus(false);
                    dragSwitch.setChecked(false);
                }
            }
        });

        setStartPointToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked setStartPointToggleBtn");
                if (setStartPointToggleBtn.getText().equals("STARTING POINT"))
                    showToast("Cancelled selecting starting point");
                else if (setStartPointToggleBtn.getText().equals("CANCEL")) {
                    showToast("Please select starting point");
                    gridMap.setStartCoordStatus(true);
                    gridMap.toggleCheckedBtn("setStartPointToggleBtn");
                } else
                    showToast("Please select manual mode");
                showLog("Exiting setStartPointToggleBtn");
            }
        });

        saveMapObstacle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getObsPos = "";
                mapPref = getContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
                editor = mapPref.edit();
                if(!mapPref.getString("maps", "").equals("")){
                    editor.putString("maps", "");
                    editor.commit();
                }
                getObsPos = GridMap.saveObstacleList();
                editor.putString("maps",getObsPos);
                editor.commit();
            }
        });

        loadMapObstacle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapPref = getContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
                String obsPos = mapPref.getString("maps","");
                if(obsPos.equals("")){}
                else{
                    String[] obstaclePosition = obsPos.split("\\|");
                    for (String s : obstaclePosition) {
                        String[] coords = s.split(",");
                        gridMap.setObstacleCoord(Integer.parseInt(coords[0]) + 1, Integer.parseInt(coords[1]) + 1, "","");
                        String direction = "";
                        switch (coords[2]) {
                            case "N":
                                direction = "North";
                                break;
                            case "E":
                                direction = "East";
                                break;
                            case "W":
                                direction = "West";
                                break;
                            case "S":
                                direction = "South";
                                break;
                            default:
                                direction = "";
                        }
                        gridMap.imageBearings.get(Integer.parseInt(coords[1]))[Integer.parseInt(coords[0])] = direction;
                    }
                    gridMap.invalidate();
                    showLog("Exiting Load Button");
                }
            }
        });


        directionChangeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked directionChangeImageBtn");
                directionFragment.show(getActivity().getFragmentManager(),
                        "Direction Fragment");
                showLog("Exiting directionChangeImageBtn");
            }
        });

        obstacleImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked obstacleImageBtn");

                if (!gridMap.getSetObstacleStatus()) {
                    showToast("Please plot obstacles");
                    gridMap.setSetObstacleStatus(true);
                    gridMap.toggleCheckedBtn("obstacleImageBtn");
                }
                else if (gridMap.getSetObstacleStatus()) {
                    gridMap.setSetObstacleStatus(false);
                }

                changeObstacleSwitch.setChecked(false);
                dragSwitch.setChecked(false);
                showLog("obstacle status = " + gridMap.getSetObstacleStatus());
                showLog("Exiting obstacleImageBtn");
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked updateButton");

                gridMap.imageBearings.get(9)[5] = "South";
                gridMap.imageBearings.get(15)[15] = "South";
                gridMap.imageBearings.get(14)[7] = "West";
                gridMap.imageBearings.get(4)[15] = "West";
                gridMap.imageBearings.get(9)[12] = "East";
                gridMap.setObstacleCoord(5+1, 9+1, "","");
                gridMap.setObstacleCoord(15+1, 15+1, "","");
                gridMap.setObstacleCoord(7+1, 14+1, "","");
                gridMap.setObstacleCoord(15+1, 4+1, "", "");
                gridMap.setObstacleCoord(12+1, 9+1, "", "");
                gridMap.invalidate();
                showLog("Exiting updateButton");
            }
        });
        return root;
    }

    private void showLog(String message) {
        Log.d(TAG, message);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
