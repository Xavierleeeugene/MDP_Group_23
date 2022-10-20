package com.example.mdp_grp_23;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class GridMap extends View{
    public GridMap(Context c) {
        super(c);
        initMap();
        setWillNotDraw(false);
    }

    SharedPreferences sharedPreferences;

    private final Paint blackPaint = new Paint();
    private final Paint whitePaint = new Paint();
    private final Paint maroonPaint = new Paint();
    private final Paint obstacleColor = new Paint();
    private final Paint robotColor = new Paint();
    private final Paint endColor = new Paint();
    private final Paint startColor = new Paint();
    private final Paint waypointColor = new Paint();
    private final Paint unexploredColor = new Paint();
    private final Paint exploredColor = new Paint();
    private final Paint arrowColor = new Paint();
    private final Paint fastestPathColor = new Paint();

    private static String robotDirection = "None";
    private static int[] startCoord = new int[]{-1, -1};
    private static int[] curCoord = new int[]{-1, -1};
    private static int[] oldCoord = new int[]{-1, -1};
    private static ArrayList<int[]> obstacleCoord = new ArrayList<>();
    private static boolean canDrawRobot = false;
    private static boolean startCoordStatus = false;
    private static boolean setObstacleStatus = false;
    private static final boolean unSetCellStatus = false;
    private static final boolean setExploredStatus = false;
    private static boolean validPosition = false;
    private static final String TAG = "GridMap";
    private static final int COL = 20;
    private static final int ROW = 20;
    private static float cellSize;
    private static Cell[][] cells;
    Map<String, String> val2IdxMap;

    private boolean mapDrawn = false;

    public ArrayList<String[]> ITEM_LIST = new ArrayList<>(Arrays.asList(
            new String[20], new String[20], new String[20], new String[20], new String[20],
            new String[20], new String[20], new String[20], new String[20], new String[20],
            new String[20], new String[20], new String[20], new String[20], new String[20],
            new String[20], new String[20], new String[20], new String[20], new String[20]
    ));
    public static ArrayList<String[]> imageBearings = new ArrayList<>(Arrays.asList(
            new String[20], new String[20], new String[20], new String[20], new String[20],
            new String[20], new String[20], new String[20], new String[20], new String[20],
            new String[20], new String[20], new String[20], new String[20], new String[20],
            new String[20], new String[20], new String[20], new String[20], new String[20]
    ));

    static ClipData clipData;
    static Object localState;
    int initialColumn, initialRow;
    public Canvas canvas;

    public GridMap(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initMap();
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(25);
        whitePaint.setTextAlign(Paint.Align.CENTER);
        maroonPaint.setColor(getResources().getColor(R.color.brightRed));
        maroonPaint.setStrokeWidth(8);
        obstacleColor.setColor(getResources().getColor(R.color.rockColor));
        robotColor.setColor(getResources().getColor(R.color.lightRed));
        robotColor.setStrokeWidth(2);
        endColor.setColor(Color.RED);
        startColor.setColor(Color.CYAN);
        waypointColor.setColor(Color.GREEN);
        unexploredColor.setColor(getResources().getColor(R.color.luigiGreen));
        exploredColor.setColor(getResources().getColor(R.color.exploredColor2));
        arrowColor.setColor(Color.BLACK);
        fastestPathColor.setColor(Color.MAGENTA);
        Paint newpaint = new Paint();
        newpaint.setColor(Color.TRANSPARENT);

        // get shared preferences
        sharedPreferences = getContext().getSharedPreferences("Shared Preferences",
                Context.MODE_PRIVATE);

        this.val2IdxMap = new HashMap<>();
    }

    private void initMap() {
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Logd("Entering onDraw");
        Logd("canDrawRobot = " + getCanDrawRobot());
        super.onDraw(canvas);
        Logd("Redrawing map");

        // Create cell coords
        Log.d(TAG,"Creating Cell");

        if (!mapDrawn) {
            mapDrawn = true;
            this.createCell();
        }

        drawIndividualCell(canvas);
        drawHorizontalLines(canvas);
        drawVerticalLines(canvas);
        drawGridNumber(canvas);
        if (getCanDrawRobot())
            drawRobot(canvas, curCoord);
        drawObstacles(canvas);

        Logd("Exiting onDraw");
    }

    // draws obstacle cells whenever map refreshes, pos/correct item selection is here
    private void drawObstacles(Canvas canvas) {
        Logd("Entering drawObstacles");

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                // draw image id
//                canvas.drawText(
//                        ITEM_LIST.get(19-i)[j],
//                        cells[j+1][19-i].startX + ((cells[1][1].endX - cells[1][1].startX) / 2),
//                        cells[j+1][i].startY + ((cells[1][1].endY - cells[1][1].startY) / 2) + 10,
//                        whitePaint
//                );
                // draw obstacle id + image id
                for(int q = 0; q < obstacleCoord.size(); q++) {
                    if (obstacleCoord.get(q)[0] == j && obstacleCoord.get(q)[1] == i) {
                        if (ITEM_LIST.get(obstacleCoord.get(q)[1])[obstacleCoord.get(q)[0]] == null || ITEM_LIST.get(obstacleCoord.get(q)[1])[obstacleCoord.get(q)[0]] == ""
                                || ITEM_LIST.get(obstacleCoord.get(q)[1])[obstacleCoord.get(q)[0]] == "Nil") {
                            Log.d(TAG, "drawObstacles: drawing obstacle ID");
                            canvas.drawText(
                                    String.valueOf(q + 1),
                                    cells[j + 1][19 - i].startX + ((cells[1][1].endX - cells[1][1].startX) / 2),
                                    cells[j + 1][19 - i].startY + ((cells[1][1].endY - cells[1][1].startY) / 2) + 10,
                                    whitePaint
                            );

                        } else {
                            Log.d(TAG, "drawObstacles: drawing image ID");
                            canvas.drawText(
                                    ITEM_LIST.get(obstacleCoord.get(q)[1])[obstacleCoord.get(q)[0]],
                                    cells[j + 1][19 - i].startX + ((cells[1][1].endX - cells[1][1].startX) / 2),
                                    cells[j + 1][19 - i].startY + ((cells[1][1].endY - cells[1][1].startY) / 2) + 10,
                                    whitePaint
                            );
                        }

                    }
                    ;
                }

                // color the face direction
                switch (imageBearings.get(19-i)[j]) {
                    case "North":
                        canvas.drawLine(
                                cells[j + 1][20 - i].startX,
                                cells[j + 1][i].startY,
                                cells[j + 1][20 - i].endX,
                                cells[j + 1][i].startY,
                                maroonPaint
                        );
                        break;
                    case "South":
                        canvas.drawLine(
                                cells[j + 1][20 - i].startX,
                                cells[j + 1][i].startY + cellSize,
                                cells[j + 1][20 - i].endX,
                                cells[j + 1][i].startY + cellSize,
                                maroonPaint
                        );
                        break;
                    case "East":
                        canvas.drawLine(
                                cells[j + 1][20 - i].startX + cellSize,
                                cells[j + 1][i].startY,
                                cells[j + 1][20 - i].startX + cellSize,
                                cells[j + 1][i].endY,
                                maroonPaint
                        );
                        break;
                    case "West":
                        canvas.drawLine(
                                cells[j + 1][20 - i].startX,
                                cells[j + 1][i].startY,
                                cells[j + 1][20 - i].startX,
                                cells[j + 1][i].endY,
                                maroonPaint
                        );
                        break;
                }
            }
        }
        Logd("Exiting drawObstacles");
    }

    private void drawIndividualCell(Canvas canvas) {
        Logd("Entering drawIndividualCell");
        for (int x = 1; x <= COL; x++)
            for (int y = 0; y < ROW; y++)
                if (!cells[x][y].type.equals("image") && cells[x][y].getId() == -1) {
                    canvas.drawRect(
                            cells[x][y].startX,
                            cells[x][y].startY,
                            cells[x][y].endX,
                            cells[x][y].endY,
                            cells[x][y].paint
                    );
                } else {
                    Paint textPaint = new Paint();
                    textPaint.setTextSize(20);
                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawRect(
                            cells[x][y].startX,
                            cells[x][y].startY,
                            cells[x][y].endX,
                            cells[x][y].endY,
                            cells[x][y].paint
                    );
                    canvas.drawText(
                            String.valueOf(cells[x][y].getId()),
                            (cells[x][y].startX+cells[x][y].endX)/2,
                            cells[x][y].endY + (cells[x][y].startY-cells[x][y].endY)/4,
                            textPaint
                    );
                }
        Logd("Exiting drawIndividualCell");
    }

    private void drawHorizontalLines(Canvas canvas) {
        for (int y = 0; y <= ROW; y++)
            canvas.drawLine(
                    cells[1][y].startX,
                    cells[1][y].startY - (cellSize / 30),
                    cells[20][y].endX,
                    cells[20][y].startY - (cellSize / 30),
                    whitePaint
            );
    }

    private void drawVerticalLines(Canvas canvas) {
        for (int x = 0; x <= COL; x++)
            canvas.drawLine(
                    cells[x][0].startX - (cellSize / 30) + cellSize,
                    cells[x][0].startY - (cellSize / 30),
                    cells[x][0].startX - (cellSize / 30) + cellSize,
                    cells[x][19].endY + (cellSize / 30),
                    whitePaint
            );
    }

    // this draws the axis numbers
    private void drawGridNumber(Canvas canvas) {
        Logd("Entering drawGridNumber");
        for (int x = 1; x <= COL; x++) {
            if (x > 10)
                canvas.drawText(
                        Integer.toString(x-1),
                        cells[x][20].startX + (cellSize / 5),
                        cells[x][20].startY + (cellSize / 1.5f),
                        blackPaint
                );
            else
                canvas.drawText(
                        Integer.toString(x-1),
                        cells[x][20].startX + (cellSize / 3),
                        cells[x][20].startY + (cellSize / 1.5f),
                        blackPaint
                );
        }
        for (int y = 0; y < ROW; y++) {
            if ((20 - y) > 10)
                canvas.drawText(
                        Integer.toString(ROW -1 - y),
                        cells[0][y].startX + (cellSize / 4),
                        cells[0][y].startY + (cellSize / 1.5f),
                        blackPaint
                );
            else
                canvas.drawText(
                        Integer.toString(ROW - 1 - y),
                        cells[0][y].startX + (cellSize / 2.5f),
                        cells[0][y].startY + (cellSize / 1.5f),
                        blackPaint
                );
        }
        Logd("Exiting drawGridNumber");
    }

    public ArrayList<int[]> getObstaclesList(){
        return obstacleCoord;
    }

    private void drawRobot(Canvas canvas, int[] curCoord) {

        float xCoord, yCoord;
        BitmapFactory.Options op = new BitmapFactory.Options();
        Bitmap bm, mapscalable;

        Logd("Entering drawRobot");
        Logd("curCoord[0] = " + curCoord[0] + ", curCoord[1] = " + curCoord[1]);
        int androidRowCoord = curCoord[1];

        if ((androidRowCoord-1) < 0 || androidRowCoord > 19) {
            Logd("row is out of bounds");
            return;
        } else if (curCoord[0] > 20 || curCoord[0] < 2) {
            Logd("col is out of bounds");
            return;
        } else {
            // draws the 2x2 squares in colour robotColor
            // horizontal lines
            for (int y = androidRowCoord - 2; y <= androidRowCoord; y++) {
                canvas.drawLine(
                        cells[curCoord[0] - 1][21 - y - 2].startX,
                        cells[curCoord[0]][21 - y - 2].startY,
                        cells[curCoord[0]][21 - y - 2].endX,
                        cells[curCoord[0]][21 - y - 2].startY,
                        robotColor
                );
            }
            // vertical lines
            for (int x = curCoord[0] - 2; x <= curCoord[0]; x++) {
                canvas.drawLine(
                        cells[x][21 - androidRowCoord - 1].endX,
                        cells[x][21 - androidRowCoord - 1].endY,
                        cells[x][21 - androidRowCoord - 1].endX,
                        cells[x][21 - androidRowCoord - 2].startY,
                        robotColor
                );
            }


            // use cells[initialCol][20 - initialRow] as ref
            switch (this.getRobotDirection()) {
                case "up":
                    //This makes the coordinates adjustable instead of static
                    op.inMutable = true;
                    bm =BitmapFactory.decodeResource(getResources(),R.drawable.arrow_direction_up_resting, op);
                    mapscalable = Bitmap.createScaledBitmap(bm, 51,51, true);
                    xCoord =cells[curCoord[0] - 1][20 - androidRowCoord].startX;
                    yCoord = cells[curCoord[0]][20 - androidRowCoord - 1].startY;
                    canvas.drawBitmap(mapscalable, xCoord, yCoord, null);
                    break;
                case "down":
                    op.inMutable = true;
                    bm =BitmapFactory.decodeResource(getResources(),R.drawable.arrow_direction_down_resting, op);
                    mapscalable = Bitmap.createScaledBitmap(bm, 51,51, true);
                    xCoord =cells[curCoord[0] - 1][20 - androidRowCoord].startX;
                    yCoord = cells[curCoord[0]][20 - androidRowCoord - 1].startY;
                    canvas.drawBitmap(mapscalable, xCoord, yCoord, null);
                    break;
                case "right":
                    op.inMutable = true;
                    bm =BitmapFactory.decodeResource(getResources(),R.drawable.arrow_direction_right_resting, op);
                    mapscalable = Bitmap.createScaledBitmap(bm, 51,51, true);
                    xCoord =cells[curCoord[0] - 1][20 - androidRowCoord].startX;
                    yCoord = cells[curCoord[0]][20 - androidRowCoord - 1].startY;
                    canvas.drawBitmap(mapscalable, xCoord, yCoord, null);

                    break;
                case "left":
                    op.inMutable = true;
                    bm =BitmapFactory.decodeResource(getResources(),R.drawable.arrow_direction_left_resting, op);
                    mapscalable = Bitmap.createScaledBitmap(bm, 51,51, true);
                    xCoord =cells[curCoord[0] - 1][20 - androidRowCoord].startX;
                    yCoord = cells[curCoord[0]][20 - androidRowCoord - 1].startY;
                    canvas.drawBitmap(mapscalable, xCoord, yCoord, null);
                    break;
                default:
                    Toast.makeText(
                            this.getContext(),
                            "Error with drawing robot (unknown direction)",
                            Toast.LENGTH_SHORT
                    ).show();
                    break;
            }
        }
        Logd("Exiting drawRobot");
    }

    public String getRobotDirection() {
        return robotDirection;
    }

    private void setValidPosition(boolean status) {
        validPosition = status;
    }

    public boolean getValidPosition() {
        return validPosition;
    }

    public void setSetObstacleStatus(boolean status) {
        setObstacleStatus = status;
    }

    public boolean getSetObstacleStatus() {
        return setObstacleStatus;
    }

    public void setStartCoordStatus(boolean status) {
        startCoordStatus = status;
    }

    private boolean getStartCoordStatus() {
        return startCoordStatus;
    }

    public boolean getCanDrawRobot() {
        return canDrawRobot;
    }

    private void createCell() {
        Logd("Entering cellCreate");
        cells = new Cell[COL + 1][ROW + 1];
        this.calculateDimension();
        cellSize = this.getCellSize();

        for (int x = 0; x <= COL; x++)
            for (int y = 0; y <= ROW; y++)
                cells[x][y] = new Cell(
                        x * cellSize + (cellSize / 30),
                        y * cellSize + (cellSize / 30),
                        (x + 1) * cellSize,
                        (y + 1) * cellSize,
                        unexploredColor,
                        "unexplored"
                );
        Logd("Exiting createCell");
    }

    public void setStartCoord(int col, int row) {
        Logd("Entering setStartCoord");
        startCoord[0] = col;
        startCoord[1] = row;
        String direction = getRobotDirection();
        if(direction.equals("None")) {
            direction = "up";
        }
        if (this.getStartCoordStatus())
            this.setCurCoord(col, row, direction);
        Logd("Exiting setStartCoord");
    }

    private int[] getStartCoord() {
        return startCoord;
    }

    public void setCurCoord(int col, int row, String direction) {
        Logd("Entering setCurCoord");
        if (row < 1 || row > 19) {
            Logd("y is out of bounds");
            return;
        }
        if (col > 20 || col < 2) {
            Logd("x is out of bounds");
            return;
        }
        curCoord[0] = col;
        curCoord[1] = row;
        this.setRobotDirection(direction);
        this.updateRobotAxis(col, row, direction);

        row = this.convertRow(row);

        for (int x = col - 1; x <= col; x++)
            for (int y = row - 1; y <= row; y++)
                cells[x][y].setType("robot");

        Logd("Exiting setCurCoord");
    }

    public void updateRobot(int col, int row, String direction) {
//        int testCol = col;
//        int testRow = row;
//        this.setRobotDirection(direction);
//        this.updateRobotAxis(testCol, testRow, direction);
//        this.drawRobot(canvas,curCoord);
//        curCoord = new int[]{col, row};
//        robotDirection = direction;


//        for (int x = testCol - 1; x <= testCol; x++)
//            for (int y = row - 1; y <= row; y++)
//                cells[x][y].setType("robot");
    }

    public int[] getCurCoord() {
        return curCoord;
    }

    private void calculateDimension() {
        this.setCellSize(getWidth()/(COL+1));
    }

    private int convertRow(int row) {
        return (20 - row);
    }

    private void setCellSize(float cellSize) {
        GridMap.cellSize = cellSize;
    }

    private float getCellSize() {
        return cellSize;
    }

    private void setOldRobotCoord(int oldCol, int oldRow) {
        Logd("Entering setOldRobotCoord");
        oldCoord[0] = oldCol;
        oldCoord[1] = oldRow;
        oldRow = this.convertRow(oldRow);

        if (oldRow == 0) {
            Logd("oldRow has gone out of grid.");
            return;
        }
        for (int x = oldCol - 1; x <= oldCol; x++)
            for (int y = oldRow - 1; y <= oldRow; y++)
                cells[x][y].setType("explored");
        Logd("Exiting setOldRobotCoord");
    }

    private int[] getOldRobotCoord() {
        return oldCoord;
    }

    public void setRobotDirection(String direction) {
        sharedPreferences = getContext().getSharedPreferences("Shared Preferences",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        robotDirection = direction;
        editor.putString("direction", direction);
        editor.apply();
        this.invalidate();
    }

    private void updateRobotAxis(int col, int row, String direction) {
        TextView xAxisTextView =  ((Activity)this.getContext()).findViewById(R.id.xAxisTextView);
        TextView yAxisTextView =  ((Activity)this.getContext()).findViewById(R.id.yAxisTextView);
        TextView directionAxisTextView =  ((Activity)this.getContext())
                .findViewById(R.id.directionAxisTextView);

        xAxisTextView.setText(String.valueOf(col-1));
        yAxisTextView.setText(String.valueOf(row-1));
        directionAxisTextView.setText(direction);
    }

    public void setObstacleCoord(int col, int row, String imageId, String imageBearing) {
        Logd("Entering setObstacleCoord");
        int[] obstacleCoord = new int[]{col - 1, row - 1};
        GridMap.obstacleCoord.add(obstacleCoord);
        row = this.convertRow(row);
        cells[col][row].setType("obstacle");
        Logd("Exiting setObstacleCoord");

        int obstacleNumber = GridMap.obstacleCoord.size();

        if(imageId == "1")
            obstacleNumber -= 1;


        View mView1 = ((Activity) this.getContext()).getLayoutInflater()
                .inflate(R.layout.activity_dialog,
                        null);

//        final Spinner mIDSpinner12 = mView1.findViewById(R.id.imageIDSpinner2);
//
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//                this.getContext(), R.array.imageID_array,
//                android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mIDSpinner12.setAdapter(adapter);

        String newID = val2IdxMap.get(imageId);

        // PLACE HOLDER//
        MainActivity.printMessage("COORD of Obstacle ID " + obstacleNumber + ":" + (col - 1) + "," + (19 - row) + ", Bearing: " + imageBearing);
    }

    private ArrayList<int[]> getObstacleCoord() {
        return obstacleCoord;
    }

    private static void Logd(String message) {
        Log.d(TAG, message);
    }

    private class Cell {
        float startX, startY, endX, endY;
        Paint paint;
        String type;
        int id = -1;

        private Cell(float startX, float startY, float endX, float endY, Paint paint, String type) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.paint = paint;
            this.type = type;
        }

        public void setType(String type) {
            this.type = type;
            switch (type) {
                case "obstacle":
                    this.paint = obstacleColor;
                    break;
                case "robot":
                    this.paint = robotColor;
                    break;
                case "end":
                    this.paint = endColor;
                    break;
                case "start":
                    this.paint = startColor;
                    break;
                case "waypoint":
                    this.paint = waypointColor;
                    break;
                case "unexplored":
                    this.paint = unexploredColor;
                    break;
                case "explored":
                    this.paint = exploredColor;
                    break;
                case "arrow":
                    this.paint = arrowColor;
                    break;
                case "fastestPath":
                    this.paint = fastestPathColor;
                    break;
                case "image":
                    this.paint = obstacleColor;
                default:
                    Logd("setType default: " + type);
                    break;
            }
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }
    }

    int endColumn, endRow;
    String oldItem;
    // drag event to move obstacle
    @Override
    public boolean onDragEvent(DragEvent dragEvent) {
        Logd("Entering onDragEvent");
        clipData = dragEvent.getClipData();
        localState = dragEvent.getLocalState();

        String tempID, tempBearing, testID;
        endColumn = endRow = -999;
        oldItem = ITEM_LIST.get(initialRow - 1)[initialColumn - 1];
        testID = oldItem;
        Logd("dragEvent.getAction() == " + dragEvent.getAction());
        Logd("dragEvent.getResult() is " + dragEvent.getResult());
        Logd("initialColumn = " + initialColumn + ", initialRow = " + initialRow);

        // drag and drop out of gridmap
        if ((dragEvent.getAction() == DragEvent.ACTION_DRAG_ENDED)
                && (endColumn == -999 || endRow == -999) && !dragEvent.getResult()) {
            // check if 2 arrays are same, then remove
            for (int i = 0; i < obstacleCoord.size(); i++) {
                if (Arrays.equals(obstacleCoord.get(i), new int[]{initialColumn - 1, initialRow - 1}))
                    obstacleCoord.remove(i);
            }
            cells[initialColumn][20-initialRow].setType("unexplored");
            ITEM_LIST.get(initialRow-1)[initialColumn-1] = "";
            imageBearings.get(initialRow-1)[initialColumn-1] = "";
        }
        // drop within gridmap
        else if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
            endColumn = (int) (dragEvent.getX() / cellSize);
            endRow = this.convertRow((int) (dragEvent.getY() / cellSize));

            // if the currently dragged cell is empty, do nothing
            if (ITEM_LIST.get(initialRow-1)[initialColumn-1].equals("")
                    && imageBearings.get(initialRow-1)[initialColumn-1].equals("")) {
                Logd("Cell is empty");
            }
            // if dropped within mapview but outside drawn grids, remove obstacle from lists
            else if (endColumn <= 0 || endRow <= 0) {
                for (int i = 0; i < obstacleCoord.size(); i++) {
                    if (Arrays.equals(obstacleCoord.get(i),
                            new int[]{initialColumn - 1, initialRow - 1}))
                        obstacleCoord.remove(i);
                }
                cells[initialColumn][20-initialRow].setType("unexplored");
                ITEM_LIST.get(initialRow-1)[initialColumn-1] = "";
                imageBearings.get(initialRow-1)[initialColumn-1] = "";
            }
            // if dropped within gridmap, shift it to new position unless already got existing
            else if ((1 <= initialColumn && initialColumn <= 20)
                    && (1 <= initialRow && initialRow <= 20)
                    && (1 <= endColumn && endColumn <= 20)
                    && (1 <= endRow && endRow <= 20)) {
                tempID = ITEM_LIST.get(initialRow-1)[initialColumn-1];
                tempBearing = imageBearings.get(initialRow-1)[initialColumn-1];

                // check if got existing obstacle at drop location
                if (!ITEM_LIST.get(endRow - 1)[endColumn - 1].equals("")
                        || !imageBearings.get(endRow - 1)[endColumn - 1].equals("")) {
                    Logd("An obstacle is already at drop location");
                } else {
                    ITEM_LIST.get(initialRow - 1)[initialColumn - 1] = "";
                    imageBearings.get(initialRow - 1)[initialColumn - 1] = "";
                    ITEM_LIST.get(endRow - 1)[endColumn - 1] = tempID;
                    imageBearings.get(endRow - 1)[endColumn - 1] = tempBearing;

                    setObstacleCoord(endColumn, endRow, "1", tempBearing);

                    for (int i = 0; i < obstacleCoord.size(); i++) {
                        if (Arrays.equals(obstacleCoord.get(i), new int[]{initialColumn - 1, initialRow - 1}))
                            obstacleCoord.remove(i);
                    }
                    cells[initialColumn][20 - initialRow].setType("unexplored");
                }
            } else {
                Logd("Drag event failed.");
            }
        }
        Logd("initialColumn = " + initialColumn
                + ", initialRow = " + initialRow
                + "\nendColumn = " + endColumn
                + ", endRow = " + endRow);
        this.invalidate();
        return true;
    }

    public void callInvalidate() {
        Logd("Entering callinvalidate");
        this.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Logd("Entering onTouchEvent");
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int column = (int) (event.getX() / cellSize);
            int row = this.convertRow((int) (event.getY() / cellSize));
            initialColumn = column;
            initialRow = row;

            ToggleButton setStartPointToggleBtn = ((Activity)this.getContext())
                    .findViewById(R.id.startpointToggleBtn);
            Logd("event.getX = " + event.getX() + ", event.getY = " + event.getY());
            Logd("row = " + row + ", column = " + column);

            oldItem = ITEM_LIST.get(initialRow - 1)[initialColumn - 1];
            String testID = oldItem;

            // start drag
            if (MappingFragment.dragStatus) {
                if (!((1 <= initialColumn && initialColumn <= 20)
                        && (1 <= initialRow && initialRow <= 20))) {
                    return false;
                } else if (ITEM_LIST.get(row - 1)[column - 1].equals("")
                        && imageBearings.get(row - 1)[column - 1].equals("")) {
                    return false;
                }
                View.DragShadowBuilder dragShadowBuilder = new MyDragShadowBuilder(this);
                this.startDrag(null, dragShadowBuilder, null, 0);
            }

            // start change obstacle
            if (MappingFragment.changeObstacleStatus) {
                if (!((1 <= initialColumn && initialColumn <= 20)
                        && (1 <= initialRow && initialRow <= 20))) {
                    return false;
                } else if (ITEM_LIST.get(row - 1)[column - 1].equals("")
                        && imageBearings.get(row - 1)[column - 1].equals("")) {
                    return false;
                } else {
                    Logd("Enter change obstacle status");
                    String imageId = ITEM_LIST.get(row -1)[column - 1];
                    String imageBearing = imageBearings.get(row - 1)[column - 1];
                    final int tRow = row;
                    final int tCol = column;

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(this.getContext());
                    View mView = ((Activity) this.getContext()).getLayoutInflater()
                            .inflate(R.layout.activity_dialog,
                                    null);
                    mBuilder.setTitle("Change Existing Obstacle ID/Bearing");
                    final Spinner mIDSpinner = mView.findViewById(R.id.imageIDSpinner2);
                    final Spinner mBearingSpinner = mView.findViewById(R.id.bearingSpinner2);

                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                            this.getContext(), R.array.imageID_array,
                            android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mIDSpinner.setAdapter(adapter);
                    ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                            this.getContext(), R.array.imageBearing_array,
                            android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mBearingSpinner.setAdapter(adapter2);

                    // start at current id and bearing
                    if (imageId.equals("")||imageId.equals("Nil")) {
                        mIDSpinner.setSelection(0);
                    } else {
                        mIDSpinner.setSelection(Integer.parseInt(imageId) - 1);
                    }
                    switch (imageBearing) {
                        case "North": mBearingSpinner.setSelection(0);
                            break;
                        case "South": mBearingSpinner.setSelection(1);
                            break;
                        case "East": mBearingSpinner.setSelection(2);
                            break;
                        case "West": mBearingSpinner.setSelection(3);
                    }



                    // do what when user presses ok
                    mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String newID = mIDSpinner.getSelectedItem().toString();
                            String newBearing = mBearingSpinner.getSelectedItem().toString();

                            ITEM_LIST.get(tRow - 1)[tCol - 1] = newID;
                            imageBearings.get(tRow - 1)[tCol - 1] = newBearing;
                            Logd("tRow - 1 = " + (tRow - 1));
                            Logd("tCol - 1 = " + (tCol - 1));
                            Logd("newID = " + newID);
                            Logd("newBearing = " + newBearing);
                            MainActivity.printMessage("COORD of Image ID " + newID + ":" + (tCol - 1) + "," + (tRow - 1) + ", Bearing: " + newBearing);
                            String oldObstacleId = UUID.randomUUID().toString(); // TODO generate random string id
                            if (val2IdxMap.containsKey(testID)) {
                                oldObstacleId = val2IdxMap.get(testID);
                            }
                            val2IdxMap.put(newID, oldObstacleId);

                            callInvalidate();
                        }
                    });

                    // dismiss
                    mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    mBuilder.setView(mView);
                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                    Window window =  dialog.getWindow();
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                    layoutParams.width = 150;
                    window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                Logd("Exit change obstacle");
            }

            // change robot size and make sure its within the grid
            if (startCoordStatus) {
                if (canDrawRobot) {
                    // removes green grids when user changes robot startpoint
                    for (int i = 0; i < 21; i++) {
                        for (int j = 0; j < 21; j++) {
                            if (cells[i][j].type.equals("robot")) {
                                cells[i][j].setType("explored");
                            }
                        }
                    }
                    // don't set robot if obstacles are there
                    int[] startCoord = this.getStartCoord();

                    if (startCoord[0] >= 2 && startCoord[1] >= 2) {
                        Logd("startCoord = " + startCoord[0] + " " + startCoord[1]);
                        for (int x = startCoord[0] - 1; x <= startCoord[0]; x++)
                            for (int y = startCoord[1] - 1; y <= startCoord[1]; y++)
                                cells[x][y].setType("unexplored");
                    }
                }
                else
                    canDrawRobot = true;
                Logd("curCoord[0] = " + curCoord[0] + ", curCoord[1] = " + curCoord[1]);
                Logd("");
                this.setStartCoord(column, row);
                startCoordStatus = false;
                String direction = getRobotDirection();
                if(direction.equals("None")) {
                    direction = "up";
                }
                try {
                    int directionInt = 0;
                    switch (direction) {
                        case "up":
                            directionInt = 0;
                            break;
                        case "left":
                            directionInt = 3;
                            break;
                        case "right":
                            directionInt = 1;
                            break;
                        case "down":
                            directionInt = 2;
                            break;
                    }
                    Logd("starting " + "(" + (row - 1) + ","
                            + (column - 1) + "," + directionInt + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateRobotAxis(column, row, direction);
                if (setStartPointToggleBtn.isChecked())
                    setStartPointToggleBtn.toggle();
                this.invalidate();
                return true;
            }

            // add id and the image bearing, popup to ask for user input
            if (setObstacleStatus) {
                if ((1 <= row && row <= 20) && (1 <= column && column <= 20)) {
                    // get user input from spinners in MapTabFragment static values
                    String imageID = (MappingFragment.imageID).equals("Nil") ?
                            "" : MappingFragment.imageID;
                    String imageBearing = MappingFragment.imageBearing;

                    // after init, at stated col and row, add the id to use as ref to update grid
                    ITEM_LIST.get(row - 1)[column - 1] = imageID;
                    imageBearings.get(row - 1)[column - 1] = imageBearing;

                    // this function affects obstacle turning too
                    this.setObstacleCoord(column, row, testID, imageBearing);
                }
                this.invalidate();
                return true;
            }
            if (setExploredStatus) {
                cells[column][20-row].setType("explored");
                this.invalidate();
                return true;
            }

            // added removing imageID and imageBearing
            if (unSetCellStatus) {
                ArrayList<int[]> obstacleCoord = this.getObstacleCoord();
                cells[column][20-row].setType("unexplored");
                for (int i=0; i<obstacleCoord.size(); i++)
                    if (obstacleCoord.get(i)[0] == column && obstacleCoord.get(i)[1] == row)
                        obstacleCoord.remove(i);
                ITEM_LIST.get(row)[column-1] = "";  // remove imageID
                imageBearings.get(row)[column-1] = "";  // remove bearing
                this.invalidate();
                return true;
            }
        }
        Logd("Exiting onTouchEvent");
        return false;
    }

    public void toggleCheckedBtn(String buttonName) {
        ToggleButton setStartPointToggleBtn = ((Activity)this.getContext())
                .findViewById(R.id.startpointToggleBtn);
        ImageButton obstacleImageBtn = ((Activity)this.getContext())
                .findViewById(R.id.addObstacleBtn);

        if (!buttonName.equals("setStartPointToggleBtn"))
            if (setStartPointToggleBtn.isChecked()) {
                this.setStartCoordStatus(false);
                setStartPointToggleBtn.toggle();
            }
        if (!buttonName.equals("obstacleImageBtn"))
            if (obstacleImageBtn.isEnabled())
                this.setSetObstacleStatus(false);
    }


    public void resetMap() {
        Logd("Entering resetMap");
        TextView robotStatusTextView =  ((Activity)this.getContext())
                .findViewById(R.id.robotStatus);
        updateRobotAxis(1, 1, "None");
        robotStatusTextView.setText("Not Available");


        this.toggleCheckedBtn("None");

        startCoord = new int[]{-1, -1};
        curCoord = new int[]{-1, -1};
        oldCoord = new int[]{-1, -1};
        robotDirection = "None";
        obstacleCoord = new ArrayList<>();
        mapDrawn = false;
        canDrawRobot = false;
        validPosition = false;

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                ITEM_LIST.get(i)[j] = "";
                imageBearings.get(i)[j] = "";
            }
        }
        Logd("Exiting resetMap");
        this.invalidate();
    }

    // e.g obstacle is on right side of 2x2 and can turn left and vice versa
    public void moveRobot(String direction) {
        Logd("Entering moveRobot");
        setValidPosition(false);
        int[] curCoord = this.getCurCoord();
        ArrayList<int[]> obstacleCoord = this.getObstacleCoord();
        this.setOldRobotCoord(curCoord[0], curCoord[1]);
        int[] oldCoord = this.getOldRobotCoord();
        String robotDirection = getRobotDirection();
        String backupDirection = robotDirection;

        // check if got obstacle when moving one grid up before turning in each case
        switch (robotDirection) {
            case "up":
                switch (direction) {
                    case "forward":
                        if (curCoord[1] != 19) {
                            curCoord[1] += 1;
                            validPosition = true;
                        }
                        break;
                    case "right":
                        if ((1 < curCoord[1] && curCoord[1] < 19)
                                && (0 < curCoord[0] && curCoord[0] < 20)) {
                            curCoord[1] += 1;
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord)) {
                                validPosition = false;
                                curCoord[1] -= 1;
                            } else {
                                curCoord[0] += 1;
                                robotDirection = "right";
                                validPosition = true;
                            }
                        }
                        break;
                    case "back":
                        if (curCoord[1] != 1) {
                            curCoord[1] -= 1;
                            validPosition = true;
                        }
                        break;
                    case "left":
                        if ((0 < curCoord[1] && curCoord[1] < 19)
                                && (2 < curCoord[0] && curCoord[0] <= 20)) {
                            curCoord[1] += 1;
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord)) {
                                validPosition = false;
                                curCoord[1] -= 1;
                            } else {
                                curCoord[0] -= 1;
                                robotDirection = "left";
                                validPosition = true;
                            }
                        }
                        break;
                    default:
                        robotDirection = "error up";
                        break;
                }
                break;
            case "right":
                switch (direction) {
                    case "forward":
                        if (0 < curCoord[0] && curCoord[0] < 20) {
                            curCoord[0] += 1;
                            validPosition = true;
                        }
                        break;
                    case "right":
                        if ((1 < curCoord[1] && curCoord[1] < 20)
                                && (0 < curCoord[0] && curCoord[0] < 20)) {
                            curCoord[0] += 1;
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord)) {
                                validPosition = false;
                                curCoord[0] -= 1;
                            } else {
                                curCoord[1] -= 1;
                                robotDirection = "down";
                                validPosition = true;
                            }
                        }
                        break;
                    case "back":
                        if (curCoord[0] > 2) {
                            curCoord[0] -= 1;
                            validPosition = true;
                        }
                        break;
                    case "left":
                        if ((0 < curCoord[1] && curCoord[1] < 19)
                                && (0 < curCoord[0] && curCoord[0] < 20)) {
                            curCoord[0] += 1;
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord)) {
                                validPosition = false;
                                curCoord[0] -= 1;
                            } else {
                                curCoord[1] += 1;
                                robotDirection = "up";
                                validPosition = true;
                            }
                        }
                        break;
                    default:
                        robotDirection = "error right";
                }
                break;
            case "down":
                switch (direction) {
                    case "forward":
                        if (curCoord[1] != 1) {
                            curCoord[1] -= 1;
                            validPosition = true;
                        }
                        break;
                    case "right":
                        if ((1 < curCoord[1] && curCoord[1] < 19)
                                && (2 < curCoord[0] && curCoord[0] <= 20)) {
                            curCoord[1] -= 1;
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord)) {
                                validPosition = false;
                                curCoord[1] += 1;
                            } else {
                                curCoord[0] -= 1;
                                robotDirection = "left";
                                validPosition = true;
                            }
                        }
                        break;
                    case "back":
                        if (0 < curCoord[1] && curCoord[1] < 19) {
                            curCoord[1] += 1;
                            validPosition = true;
                        }
                        break;
                    case "left":
                        if ((1 < curCoord[1] && curCoord[1] < 20)
                                && (0 < curCoord[0] && curCoord[0] <= 20)) {
                            curCoord[1] -= 1;
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord)) {
                                validPosition = false;
                                curCoord[1] += 1;
                            } else {
                                curCoord[0] += 1;
                                robotDirection = "right";
                                validPosition = true;
                            }
                        }
                        break;
                    default:
                        robotDirection = "error down";
                }
                break;
            case "left":
                switch (direction) {
                    case "forward":
                        if (curCoord[0] > 2) {
                            curCoord[0] -= 1;
                            validPosition = true;
                        }
                        break;
                    case "right":
                        if ((0 < curCoord[1] && curCoord[1] < 19)
                                && (2 < curCoord[0] && curCoord[0] < 20)) {
                            curCoord[0] -= 1;
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord)) {
                                validPosition = false;
                                curCoord[0] += 1;
                            } else {
                                curCoord[1] += 1;
                                robotDirection = "up";
                                validPosition = true;
                            }
                        }
                        break;
                    case "back":
                        if (curCoord[0] < 20) {
                            curCoord[0] += 1;
                            validPosition = true;
                        }
                        break;
                    case "left":
                        if ((0 < curCoord[1] && curCoord[1] <= 20)
                                && (2 < curCoord[0] && curCoord[0] < 20)) {
                            curCoord[0] -= 1;
                            if (checkObstaclesRightInFront(curCoord, obstacleCoord)) {
                                validPosition = false;
                                curCoord[0] += 1;
                            } else {
                                curCoord[1] -= 1;
                                robotDirection = "down";
                                validPosition = true;
                            }
                        }
                        break;
                    default:
                        robotDirection = "error left";
                }
                break;
            default:
                robotDirection = "error moveCurCoord";
                break;
        }
        Logd("Enter checking for obstacles in destination 2x2 grid");
        if (getValidPosition())
            // check obstacle for new position
            for (int x = curCoord[0] - 1; x <= curCoord[0]; x++) {
                for (int y = curCoord[1] - 1; y <= curCoord[1]; y++) {
                    for (int i = 0; i < obstacleCoord.size(); i++) {
                        Logd("x-1 = " + (x-1) + ", y = " + y);
                        Logd("obstacleCoord.get(" + i + ")[0] = " + obstacleCoord.get(i)[0]
                                + ", obstacleCoord.get(" + i + ")[1] = " + obstacleCoord.get(i)[1]);
                        if (obstacleCoord.get(i)[0] == (x-1) && obstacleCoord.get(i)[1] == y) { // HERE x
                            setValidPosition(false);
                            robotDirection = backupDirection;
                            break;
                        }
                    }
                    if (!getValidPosition())
                        break;
                }
                if (!getValidPosition())
                    break;
            }
        Logd("Exit checking for obstacles in destination 2x2 grid");
        if (getValidPosition())
            this.setCurCoord(curCoord[0], curCoord[1], robotDirection);
        else {
            if (direction.equals("forward") || direction.equals("back"))
                robotDirection = backupDirection;
            this.setCurCoord(oldCoord[0], oldCoord[1], robotDirection);
        }
        this.invalidate();
        Logd("Exiting moveRobot");
    }

    public boolean checkObstaclesRightInFront(int[] coord, List<int[]> obstacles) {
        Logd("Enter checking for obstacles directly in front");
        // check obstacle for new position
        for (int x = coord[0] - 1; x <= coord[0]; x++) {
            for (int y = coord[1] - 1; y <= coord[1]; y++) {
                for (int i = 0; i < obstacles.size(); i++) {
                    Logd("x-1 = " + (x-1) + ", y = " + y);
                    Logd("obstacle.get(" + i + ")[0] = " + obstacles.get(i)[0]
                            + ", obstacle.get(" + i + ")[1] = " + obstacles.get(i)[1]);
                    if (obstacles.get(i)[0] == (x-1) && obstacles.get(i)[1] == y) { // HERE x
                        return true;
                    }
                }
            }
        }
        Logd("Exit checking for obstacles directly in front");
        return false;   // false means no obstacles
    }

    private static class MyDragShadowBuilder extends View.DragShadowBuilder {
        private Point mScaleFactor;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v) {
            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            // Defines local variables
            int width;
            int height;

            // Sets the width of the shadow to half the width of the original View
            width = (int) (cells[1][1].endX - cells[1][1].startX);

            // Sets the height of the shadow to half the height of the original View
            height = (int) (cells[1][1].endY - cells[1][1].startY);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);
            // Sets size parameter to member that will be used for scaling shadow image.
            mScaleFactor = size;

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            // Draws the ColorDrawable in the Canvas passed in from the system.
            canvas.scale(mScaleFactor.x/(float)getView().getWidth(),
                    mScaleFactor.y/(float)getView().getHeight());
            getView().draw(canvas);
        }

    }

    // week 8 req to update robot pos when alg sends updates
    public void performAlgoCommand(int x, int y, String direction) {
        Logd("Enter performAlgoCommand");
        Logd("x = " + x + "\n" + "y = " + y);
        if ((x > 1 && x < 21) && (y > -1 && y < 20)) {
            Logd("within grid");
            robotDirection = (robotDirection.equals("None")) ? "up" : robotDirection;
            switch (direction) {
                case "N":
                    robotDirection = "up";
                    break;
                case "S":
                    robotDirection = "down";
                    break;
                case "E":
                    robotDirection = "right";
                    break;
                case "W":
                    robotDirection = "left";
                    break;
            }
        }
        // if robot pos was not set initially, don't set as explored before moving to new coord
        if (!(curCoord[0] == -1 && curCoord[1] == -1)) {
            Logd("if robot was not at invalid pos prev");
            if ((curCoord[0] > 1 && curCoord[0] < 21) && (curCoord[1] > -1 && curCoord[1] < 20)) {
                Logd("prev pos was within grid");
                for (int i = curCoord[0] - 1; i <= curCoord[0]; i++) {
                    for (int j = curCoord[1] - 1; j <= curCoord[1]; j++) {
                        if (!(cells[i][20-j-1]).type.equals("obstacle")){
                            cells[i][20 - j - 1].setType("explored");
                        }
                    }
                }
            }
        }
        // if robot is still in frame
        if ((x > 1 && x < 21) && (y > -1 && y < 20)) {
            Logd("within grid");
            setCurCoord(x, y, robotDirection);    // set new coords and direction
            canDrawRobot = true;
        }
        // if robot goes out of frame
        else {
            Logd("set canDrawRobot to false");
            canDrawRobot = false;
            curCoord[0] = -1;
            curCoord[1] = -1;
        }
        this.invalidate();
        Logd("Exit performAlgoCommand");
    }

    //Week 8 Translation Mapping
    public ArrayList<int[]> translateCoord(ArrayList<int[]> ogCoords, int protocol){
        //Implementation of Protocol Number to decide the type of translation
        //Sender to receiver
        //Receiver to sender
        if (protocol == 0){
            for (int i = 0; i< ogCoords.size(); i++){
                ogCoords.get(i)[1] = Math.abs(ogCoords.get(i)[1] - 19);
            }
        }
        return ogCoords;
    }

    public static String saveObstacleList(){
        String message ="";
        for (int i = 0; i < obstacleCoord.size(); i++) {
            message += ((obstacleCoord.get(i)[0]) + ","
                    + (obstacleCoord.get(i)[1]) + ","
                    + imageBearings.get(obstacleCoord.get(i)[1])[obstacleCoord.get(i)[0]].charAt(0))+"|";
        }
        return message;
    }

    // week 8 req to send algo obstacle info
    //Code edited to remove 0.5
    public String getObstacles() {
        String msg = "ALG|";
        ArrayList<Character> directionArr = new ArrayList<>();

        // public void setObstacleCoord(int col, int row) {
        for (int i = 0; i < obstacleCoord.size(); i++) {
            if (i==obstacleCoord.size()-1) {
                directionArr.add(imageBearings.get(obstacleCoord.get(i)[1])[obstacleCoord.get(i)[0]].charAt(0));
                msg += ((obstacleCoord.get(i)[0]) + ","
                        + (obstacleCoord.get(i)[1]) + ","
                        + imageBearings.get(obstacleCoord.get(i)[1])[obstacleCoord.get(i)[0]].charAt(0));

            }
            else{
                directionArr.add(imageBearings.get(obstacleCoord.get(i)[1])[obstacleCoord.get(i)[0]].charAt(0));
                msg += ((obstacleCoord.get(i)[0]) + ","
                        + (obstacleCoord.get(i)[1]) + ","
                        + imageBearings.get(obstacleCoord.get(i)[1])[obstacleCoord.get(i)[0]].charAt(0)
                        + "|");
            }
        }

        //Translation Message to Algo
        msg+="-";
        msg+="ALG|";

        ArrayList<int[]> coordlist = new ArrayList<>();

        for (int i = 0; i<obstacleCoord.size(); i++){
            int col = obstacleCoord.get(i)[0];
            int row = obstacleCoord.get(i)[1];
            int[] newCoord = new int[]{col, row};
            coordlist.add(newCoord);
        }

        ArrayList<int[]>translateCoords = translateCoord(coordlist,0);


        for (int i = 0; i < translateCoords.size(); i++){
            if (i ==  translateCoords.size()-1){
                msg += ((translateCoords.get(i)[1]) + ","
                        + (translateCoords.get(i)[0]) + ","
                        + directionArr.get(i));


            }
            else{
                msg += ((translateCoords.get(i)[1]) + ","
                        + (translateCoords.get(i)[0]) + ","
                        + directionArr.get(i)
                        + "|");
            }
        }

        msg += "\n";
        return msg;
    }

    // wk 8 task
    public boolean updateIDFromRpi(String obstacleID, String imageID) {
        Logd("updateIDFromRpi");
        int x = obstacleCoord.get(Integer.parseInt(obstacleID))[0];
        int y = obstacleCoord.get(Integer.parseInt(obstacleID))[1];
        ITEM_LIST.get(y)[x] = (imageID.equals("-1")) ? "" : imageID;
        this.invalidate();
        return true;
    }
}
