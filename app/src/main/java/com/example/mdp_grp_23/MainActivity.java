package com.example.mdp_grp_23;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Canvas;
import android.widget.ToggleButton;



import com.google.android.material.tabs.TabLayout;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    final Handler handler = new Handler();
    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;

    private static GridMap gridMap;
    static TextView xAxisTextView, yAxisTextView, directionAxisTextView;
    static TextView robotStatusTextView, bluetoothStatus, bluetoothDevice;
    static ImageButton upBtn, downBtn, leftBtn, rightBtn;

    BluetoothDevice mBTDevice;
    private static UUID myUUID;
    ProgressDialog myDialog;
    Bitmap bm, mapscalable;
    String obstacleID;

    private static final String TAG = "Main Activity";
    public static boolean stopTimerFlag = false;
    public static boolean stopWk9TimerFlag = false;

    private int g_coordX;
    private int g_coordY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialization
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        sectionsPagerAdapter.addFragment(new BluetoothCommunications(),"CHAT");
        sectionsPagerAdapter.addFragment(new MappingFragment(),"MAP CONFIG");
        sectionsPagerAdapter.addFragment(new ControlFragment(),"CHALLENGE");
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        // Set up sharedPreferences
        MainActivity.context = getApplicationContext();
        sharedPreferences();
        editor.putString("message", "");
        editor.putString("direction","None");
        editor.putString("connStatus", "Disconnected");
        editor.commit();

        // Toolbar
        ImageButton bluetoothButton = findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popup = new Intent(MainActivity.this, BluetoothSetUp.class);
                startActivity(popup);
            }
        });

        // Bluetooth Status
        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        bluetoothDevice = findViewById(R.id.bluetoothConnectedDevice);

        // Map
        gridMap = new GridMap(this);
        gridMap = findViewById(R.id.mapView);
        xAxisTextView = findViewById(R.id.xAxisTextView);
        yAxisTextView = findViewById(R.id.yAxisTextView);
        directionAxisTextView = findViewById(R.id.directionAxisTextView);

        // initialize ITEM_LIST and imageBearings strings
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                gridMap.ITEM_LIST.get(i)[j] = "";
                GridMap.imageBearings.get(i)[j] = "";
            }
        }

        // Controller
        upBtn = findViewById(R.id.upBtn);
        downBtn = findViewById(R.id.downBtn);
        leftBtn = findViewById(R.id.leftBtn);
        rightBtn = findViewById(R.id.rightBtn);

        // Robot Status
        robotStatusTextView = findViewById(R.id.robotStatus);

        myDialog = new ProgressDialog(MainActivity.this);
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
    }

    public static GridMap getGridMap() {
        return gridMap;
    }
    public static TextView getRobotStatusTextView() {  return robotStatusTextView; }
    public static ImageButton getUpBtn() { return upBtn; }
    public static ImageButton getDownBtn() { return downBtn; }
    public static ImageButton getLeftBtn() { return leftBtn; }
    public static ImageButton getRightBtn() { return rightBtn; }
    public static TextView getBluetoothStatus() { return bluetoothStatus; }
    public static TextView getConnectedDevice() { return bluetoothDevice; }


    public static void sharedPreferences() {
        sharedPreferences = MainActivity.getSharedPreferences(MainActivity.context);
        editor = sharedPreferences.edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

//    // Send Coordinates to alg
    public static void printCoords(String message){
        showLog("Displaying Coords untranslated and translated");
        String[] strArr = message.split("-",2);

        if (BluetoothConnectionService.BluetoothConnectionStatus == true){
                byte[] bytes = strArr[1].getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
        refreshMessageReceivedNS("Untranslated Coordinates: " + strArr[0] + "\n");
        refreshMessageReceivedNS("Translated Coordinates: "+strArr[1]);
        showLog("Exiting printCoords");
    }

    // Send message to bluetooth
    public static void printMessage(String message) {
        showLog("Entering printMessage");
        editor = sharedPreferences.edit();

        if (BluetoothConnectionService.BluetoothConnectionStatus) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
        showLog(message);
//        refreshMessageReceivedNS(message);
        showLog("Exiting printMessage");
    }

    public static void refreshMessageReceivedNS(String message){
        BluetoothCommunications.getMessageReceivedTextView().append(message+ "\n");
    }

    public void refreshDirection(String direction) {
        gridMap.setRobotDirection(direction);
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
        printMessage("Direction is set to " + direction);
    }

    public static void refreshLabel() {
        xAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[0]-1));
        yAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[1]-1));
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    private final BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if(status.equals("connected")){
                try {
                    myDialog.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }

                Log.d(TAG, "mBroadcastReceiver5: Device now connected to "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Device now connected to "
                        + mDevice.getName(), Toast.LENGTH_SHORT).show();
                editor.putString("connStatus", "Connected to " + mDevice.getName());
            }
            else if(status.equals("disconnected")){
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Disconnected from "
                        + mDevice.getName(), Toast.LENGTH_SHORT).show();

                editor.putString("connStatus", "Disconnected");

                myDialog.show();
            }
            editor.commit();
        }
    };

  //     message handler
  //     alg sends x,y,robotDirection,movementAction
  //     alg sends ALG,<obstacle id>
  //   rpi sends RPI,<image id>
    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            showLog("receivedMessage: message --- " + message);
            int[] global_store = gridMap.getCurCoord();
            g_coordX = global_store[0];
            g_coordY = global_store[1];
            ArrayList<String> mapCoord = new ArrayList<>();

            //PLACE HOLDER
            //STATUS:<input>
            if (message.contains("STATUS")) {
                robotStatusTextView.setText(message.split(":")[1]);
            }
            //ROBOT|5,4,E
            if(message.contains("Direction")) {
                String[] cmd = message.split("\\|");
                String[] sentCoords = cmd[1].split(",");
                String[] sentDirection = sentCoords[2].split("\\.");
                BluetoothCommunications.getMessageReceivedTextView().append("\n");
                String direction = "";
                String abc = String.join("", sentDirection);
                if (abc.contains("EAST")) {
                    direction = "right";
                }
                else if (abc.contains("NORTH")) {
                    direction = "up";
                }
                else if (abc.contains("WEST")) {
                    direction = "left";
                }
                else if (abc.contains("SOUTH")) {
                    direction = "down";
                }
                else{
                    direction = "";
                }
//                    updateCoord();
                //IDK how draw canvas
//                Canvas canvas = new Canvas();
//                gridMap.updateRobot(Integer.valueOf(sentCoords[0]) + 1, 20 - Integer.valueOf(sentCoords[1]), direction);
                gridMap.setCurCoord(Integer.valueOf(sentCoords[1]) + 2, 19 - Integer.valueOf(sentCoords[0]), direction);
//                gridMap.updateRobot(Integer.valueOf(sentCoords[0]), Integer.valueOf(sentCoords[1]), direction);
            }
            //image format from RPI is "TARGET~<obID>~<ImValue>" eg TARGET~3~7
            else if(message.contains("TARGET")) {
                String[] cmd = message.split("~");
                BluetoothCommunications.getMessageReceivedTextView().append("Obstacle no. :" + cmd[1] + "Prediction: +" + cmd[2] + "\n");
                gridMap.updateIDFromRpi(cmd[1], cmd[2]);
                obstacleID = String.valueOf(Integer.valueOf(cmd[1]) - 1);
//                    int current_x = 0;
//                    int current_y = 0;
//                    //This code runs in a situation in which either one x or y  is the same
//                    //or both are the same coordinates they'll still move
//                    if (direction.equals("N") || direction.equals("E")){
//                        current_x = Integer.parseInt(sentCoords[0])+1;
//                        current_y = 19 - Integer.parseInt(sentCoords[1]);
//                    }
//                    // When Direction is Heading South, Current X is different, it does need the X+1 attribute
//                    else if (direction.equals("S") || direction.equals("W")){
//                        current_x = Integer.parseInt(sentCoords[0]);
//                        current_y = 19 - Integer.parseInt(sentCoords[1]);
//                    }
//
//                    if(current_x + 1 == g_coordX){
//                        if(!checkIfYWithinGrid(current_y+1)) {
//                            String store = (current_x+1)+","+current_y+","+direction;
//                            updateCoord(current_x+1, current_y);
//                            mapCoord.add(store);
//                        }
//                        else{
//                            String store = (current_x+1)+","+(current_y+1)+","+direction;
//                            updateCoord(current_x+1, current_y+1);
//                            mapCoord.add(store);
//                        }
//                    }
//                    else if (current_y+1 == g_coordY){
//                        if(!checkIfXWithinGrid(current_x+1)){
//                            String store = current_x+","+(current_y+1)+","+direction;
//                            updateCoord(current_x, current_y+1);
//                            mapCoord.add(store);
//                        }
//                        else{
//                            String store = (current_x+1) +","+(current_y+1)+","+direction;
//                            updateCoord(current_x+1,current_y+1);
//                            mapCoord.add(store);
//                        }
//                    }
//                    else{
//                        int result_x = current_x - (g_coordX - 1);
//                        int result_y = current_y - (g_coordY - 1);
//                        switch (direction) {
//                            case "E":
//                                if (result_y > 0) {
//                                    for (int j = 0; j < result_y; j++) {
//                                        if(!checkIfYWithinGrid(g_coordY+1)) {
//                                            String store = g_coordX+","+g_coordY+","+"N";
//                                            updateCoord(g_coordX, g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                        else{
//                                            String store = g_coordX+","+(g_coordY+1)+","+"N";
//                                            updateCoord(g_coordX, g_coordY+1);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                } else if (result_y < 0) {
//                                    for (int j = result_y; j < 0; j++) {
//                                        if(!checkIfYWithinGrid(g_coordY-1)){
//                                            String store = g_coordX+","+g_coordY+","+"S";
//                                            updateCoord(g_coordX, g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                        else{
//                                            String store = g_coordX+","+(g_coordY-1)+","+"S";
//                                            updateCoord(g_coordX, g_coordY-1);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                }
//                                int [] getCurCoords = {g_coordX, g_coordY};
//                                ArrayList<int[]> obstacleList = gridMap.getObstaclesList();
//                                int [] getSingleObstacle = getClosestObstacle(obstacleList, getCurCoords);
//                                int xCoordCheck = getCurCoords[0];
//                                int yCoordCheck = getCurCoords[1];
//                                int compensation = 0;
//                                if (getSingleObstacle!=null) {
//                                    for (int j = xCoordCheck; j < 19; j++) {
//                                        if (j == getSingleObstacle[0] && (yCoordCheck) == getSingleObstacle[1]) {
//                                            compensation += 1;
//                                        }
//                                    }
//                                    int counterCompensation = compensation;
//                                    if (compensation > 0) {
//                                        for (int k = 0; k < compensation; k++) {
//                                            String store = g_coordX+","+ (g_coordY-1)+","+ "S";
//                                            updateCoord(g_coordX,g_coordY-1);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                    for (int j = 0; j < result_x; j++) {
//                                        if (!checkIfXWithinGrid(g_coordX + 1)) {
//                                            String store = g_coordX+","+g_coordY+","+"E";
//                                            updateCoord(g_coordX,g_coordY);
//                                            mapCoord.add(store);
//                                        } else {
//                                            String store = (g_coordX+1)+","+g_coordY+","+"E";
//                                            updateCoord(g_coordX+1,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                    if (counterCompensation > 0) {
//                                        for (int k = 0; k < counterCompensation; k++) {
//                                            String store = g_coordX+","+(g_coordY+1)+","+"N";
//                                            updateCoord(g_coordX,g_coordY+1);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                }
//                                else{
//                                    for (int j = 0; j < result_x; j++) {
//                                        if (!checkIfXWithinGrid(g_coordX + 1)) {
//                                            String store = g_coordX+","+g_coordY+","+"E";
//                                            updateCoord(g_coordX,g_coordY);
//                                            mapCoord.add(store);
//                                        } else {
//                                            String store = (g_coordX+1)+","+g_coordY+","+"E";
//                                            updateCoord(g_coordX+1,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                }
//                                break;
//                            case "W":
//                                if (result_y > 0) {
//                                    for (int j = 0; j < result_y; j++) {
//                                        if(!checkIfYWithinGrid(g_coordY+1)) {
//                                            String store = g_coordX+","+g_coordY+","+"N";
//                                            updateCoord(g_coordX,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                        else{
//                                            String store = g_coordX+","+(g_coordY+1)+","+"N";
//                                            updateCoord(g_coordX,g_coordY+1);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                } else if (result_y < 0) {
//                                    for (int j = result_y; j < 0; j++) {
//                                        if(!checkIfYWithinGrid(g_coordY-1)){
//                                            String store = g_coordX+","+g_coordY+","+"S";
//                                            updateCoord(g_coordX,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                        else{
//                                            String store = g_coordX+","+(g_coordY-1)+","+"S";
//                                            updateCoord(g_coordX,g_coordY-1);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                }
//                                for (int j = result_x; j < 0; j++) {
//                                    if(!checkIfXWithinGrid(g_coordX-1))
//                                    {
//                                        String store = g_coordX+","+g_coordY+","+"W";
//                                        updateCoord(g_coordX,g_coordY);
//                                        mapCoord.add(store);
//                                    }
//                                    else{
//                                        String store = (g_coordX-1)+","+g_coordY+","+"W";
//                                        updateCoord(g_coordX-1,g_coordY);
//                                        mapCoord.add(store);
//                                    }
//                                }
//                                break;
//                            case "S":
//                                if (result_x > 0) {
//                                    for (int j = 0; j < result_x; j++) {
//                                        if(!checkIfXWithinGrid(g_coordX+1)) {
//                                            String store = g_coordX+","+g_coordY+","+"E";
//                                            updateCoord(g_coordX,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                        else{
//                                            String store = (g_coordX+1)+","+g_coordY+","+"E";
//                                            updateCoord(g_coordX+1,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                } else if (result_x < 0) {
//                                    for (int j = result_x; j < 0; j++) {
//                                        if(!checkIfXWithinGrid(g_coordX-1))
//                                        {
//                                            String store = g_coordX+","+g_coordY+","+"W";
//                                            updateCoord(g_coordX,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                        else{
//                                            String store = (g_coordX-1)+","+g_coordY+","+"W";
//                                            updateCoord(g_coordX-1,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                }
//                                for (int j = result_y; j < 0; j++) {
//                                    if(!checkIfYWithinGrid(g_coordY-1)) {
//                                        String store = g_coordX+","+g_coordY+","+"S";
//                                        updateCoord(g_coordX,g_coordY);
//                                        mapCoord.add(store);
//                                    }
//                                    else{
//                                        String store = g_coordX+","+(g_coordY-1)+","+"S";
//                                        updateCoord(g_coordX,g_coordY-1);
//                                        mapCoord.add(store);
//                                    }
//                                }
//                                break;
//                            case "N":
//                                if (result_x > 0) {
//                                    for (int j = 0; j < result_x; j++) {
//                                        if(!checkIfXWithinGrid(g_coordX+1)) {
//                                            String store = g_coordX+","+g_coordY+","+"E";
//                                            updateCoord(g_coordX,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                        else{
//                                            String store = (g_coordX+1)+","+g_coordY+","+"E";
//                                            updateCoord(g_coordX+1,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                } else if (result_x < 0) {
//                                    for (int j = result_x; j < 0; j++) {
//                                        if(!checkIfXWithinGrid(g_coordX-1))
//                                        {
//                                            String store = g_coordX+","+g_coordY+","+"W";
//                                            updateCoord(g_coordX,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                        else{
//                                            String store = (g_coordX-1)+","+g_coordY+","+"W";
//                                            updateCoord(g_coordX-1,g_coordY);
//                                            mapCoord.add(store);
//                                        }
//                                    }
//                                }
//                                for (int j = 0; j < result_y; j++) {
//                                    if(!checkIfYWithinGrid(g_coordY+1)) {
//                                        String store = g_coordX+","+g_coordY+","+"N";
//                                        updateCoord(g_coordX,g_coordY);
//                                        mapCoord.add(store);
//                                    }
//                                    else{
//                                        String store = g_coordX+","+(g_coordY+1)+","+"N";
//                                        updateCoord(g_coordX,g_coordY+1);
//                                        mapCoord.add(store);
//                                    }
//                                }
//                                break;
//                        }
//                    }

//                int time = 0;
//                for (int i=0; i<mapCoord.size();i++){
//                    time+=200;
//                    String[] singleCoord = mapCoord.get(i).split(",");
//                    int coordx = Integer.parseInt(singleCoord[0]);
//                    int coordy = 20 - Integer.parseInt(singleCoord[1]);
//                    String dir = singleCoord[2];
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            gridMap.performAlgoCommand(coordx,coordy,dir);
//                        }
//                    },time);
//                }

//            }
            //image format from RPI is "TARGET~<obID>~<ImValue>" eg TARGET~3~7
//            else if(message.contains("TARGET")) {
//                String[] cmd = message.split("~");
//                gridMap.updateIDFromRpi(cmd[1], cmd[2]);
//                obstacleID = String.valueOf(Integer.valueOf(cmd[1]) - 1);
//            }
//            else if (message.equals("ENDED")) {
//                // if wk 8 btn is checked, means running wk 8 challenge and likewise for wk 9
//                // end the corresponding timer
//                ToggleButton exploreButton = findViewById(R.id.exploreToggleBtn2);
//                ToggleButton fastestButton = findViewById(R.id.fastestToggleBtn2);
//
//                if (exploreButton.isChecked()) {
//                    showLog("explorebutton is checked");
//                    stopTimerFlag = true;
//                    exploreButton.setChecked(false);
//                    robotStatusTextView.setText("Auto Movement/ImageRecog Stopped");
//                    ControlFragment.timerHandler.removeCallbacks(ControlFragment.timerRunnableExplore);
//                } else if (fastestButton.isChecked()) {
//                    showLog("fastestbutton is checked");
//                    stopTimerFlag = true;
//                    fastestButton.setChecked(false);
//                    robotStatusTextView.setText("Week 9 Stopped");
//                    ControlFragment.timerHandler.removeCallbacks(ControlFragment.timerRunnableFastest);
//                }
            }
        }
    };

    public int[] getClosestObstacle(ArrayList<int[]> obstacleList, int[] getCurCoords) {
        if(obstacleList.size()==0){
            return null;
        }
        int coords_X = getCurCoords[0];
        int coords_Y = getCurCoords[1];
        int smallest_index = 0;
        int trackSmallestDistance_X = 10000000;
        int trackSmallestDistance_Y = 10000000;
        for (int i = 0; i < obstacleList.size(); i++) {
            if (trackSmallestDistance_X > Math.abs(obstacleList.get(i)[0] - coords_X)) {
                if (trackSmallestDistance_Y > Math.abs(obstacleList.get(i)[1] - coords_Y)) {
                    smallest_index = i;
                    trackSmallestDistance_X = Math.abs(obstacleList.get(i)[0] - coords_X);
                    trackSmallestDistance_Y = Math.abs(obstacleList.get(i)[1] - coords_Y);
                }
            }
        }
        return obstacleList.get(smallest_index);
    }

    public boolean checkIfXWithinGrid(int coord){
        return coord > 1 && coord < 21;
    }

    public boolean checkIfYWithinGrid(int coord){
        return coord > -1 && coord <20;
    }

    public void updateCoord(int coordX, int coordY){
        g_coordX = coordX;
        g_coordY = coordY;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    mBTDevice = data.getExtras().getParcelable("mBTDevice");
                    myUUID = (UUID) data.getSerializableExtra("myUUID");
                }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, filter2);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        showLog("Exiting onSaveInstanceState");
    }
}