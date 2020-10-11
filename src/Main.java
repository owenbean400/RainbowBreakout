import acm.graphics.*;
import acm.program.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * <strong>Owen Bean Rainbow Break Out</strong>
 * <p>
 * The object of the game is to take down all the breaks without
 * losing all your lives. There are special colored bricks that changes
 * the ball speed to make it more challenging to play. This breakout game
 * comes with gravity, to make it more challenging to play.
 *
 * @author  Owen G. Bean
 * @version 1.0.0
 * @since   10/10/20
 */
public class Main extends GraphicsProgram {

    //window variables
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int windowWidth = (int) Math.round(screenSize.getWidth() * 0.9);
    private int windowHeight = (int) Math.round(screenSize.getHeight() * 0.9);
    final String WINDOW_TITLE = "Owen Bean Breakout Game :)";
    final int FPS = 80;
    final int PAUSE = 10;
    final Color[] RAINBOW_COLOR = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};
    final String CENTER_IMAGE_FILE = "./Rainbow_BreakOut.png";
    private int lives = 3;
    Random random = new Random();

    //bricks variables
    final int BRICK_AMOUNT_ROWS = 6;
    final int BRICK_AMOUNT_COLUMNS = 18;
    final int BRICK_GAP_SIZE = 4;
    private int brickGridHeight = windowHeight / 3;
    private final double[][] brickInfo = new double[BRICK_AMOUNT_ROWS * BRICK_AMOUNT_COLUMNS][5]; //stores info about bricks
    private final GRect[] brickBrick = new GRect[brickInfo.length]; //actually bricks drawings
    private boolean brickAlreadyDrawn = false;
    private int brickCount = BRICK_AMOUNT_ROWS * BRICK_AMOUNT_COLUMNS;

    //paddle variables
    final int PADDLE_HEIGHT = 20;
    final int BOTTOM_BOUNDING_MARGIN = 80;
    private int paddleMousePositionX = windowWidth / 2; //start in the middle of screen on x plane
    private int paddleWidth = windowWidth / 10;
    private Color paddleColor = Color.GRAY;
    private int paddlePositionX = paddleMousePositionX - (paddleWidth / 2);
    private int paddlePositionY = windowHeight - BOTTOM_BOUNDING_MARGIN;

    //Bottom line
    private int linePositionY = windowHeight - BOTTOM_BOUNDING_MARGIN + 10;

    //ball variables
    Color ballColor = Color.BLACK;
    private int ballPositionCenterX = windowWidth / 2; //start at center of screen
    private int ballPositionCenterY = windowHeight / 2; //start at center of screen
    private int ballDiameter = (windowWidth > windowHeight) ? windowWidth / 50 : windowHeight / 50;
    //ball velocity variables
    final private int[] velocityChoicesX = {-3, -2, -1, 1, 2, 3};
    private double velocityBallX = 0; //measurement in pixels per frame
    private double velocityBallY = 0; //measurement in pixels per frame
    private double desireVelocityBallX = velocityChoicesX[random.nextInt(6)];
    private double desireVelocityBallY = 4.0;
    final double GRAVITY = 1.02;
    final double MAX_VELOCITY_Y = 12.5;
    final double MIN_VELOCITY_Y = 1.2;

    //center font size
    final String INFO_TEXT_FONT = "Times-24";
    final String CENTER_TEXT_FONT = "Times-14";
    final String BEGIN_TEXT = "Click the mouse to play! Left or Right click works";
    final String LOOSE_TEXT = "You ran out of lives, soooo YOU LOOOSE";
    final String WIN_TEXT = "You WIN!!!";
    final String CREATOR_INFO = "Owen Bean Rainbow Break Out Created by Owen Bean";
    //white box margins
    final int WHITEBOX_MARGIN_TOP_BOTTOM = 6;
    final int WHITEBOX_MARGIN_LEFT_RIGHT = 12;

    //graphics variable
    private GOval ball = drawBall();
    private GRect paddle = drawPaddle();
    private GLine bottomLine = drawBottomLine();
    private GLabel infoText = drawBottomInfoText();
    private GLabel creatorText = creatorInfoText(CREATOR_INFO);
    private GLabel startingText = drawCenterText(BEGIN_TEXT);
    private GLabel loseText = drawCenterText(LOOSE_TEXT);
    private GLabel winText = drawCenterText(WIN_TEXT);
    private GRect whiteBox = drawCenterBox(0, 0);
    private GImage centerImg = drawCenterImg();
    private boolean playing = false;

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    public void init() {
        //listen to mouse events
        addMouseListeners();
        addKeyListeners();

        //set window information
        setSize(this.windowWidth, this.windowHeight);
        setTitle(this.WINDOW_TITLE);
    }

    @Override
    public void run() {
        this.resize(this.windowWidth, this.windowHeight); //make sure window is window size, acm library glitches 
        // quick pause after resize to allow resize to happen properly
        pause(this.PAUSE);

        //create the grid of bricks
        createBricks(this.BRICK_AMOUNT_COLUMNS, 0, 0, getWidth(), this.brickGridHeight, this.BRICK_GAP_SIZE);

        add(this.centerImg);

        //Graphics looping forever to draw the graphic for the game to create motion and updated graphics
        while (true) {
            //set up variables for graphics
            this.ball = drawBall();
            this.paddle = drawPaddle();
            this.bottomLine = drawBottomLine();
            this.infoText = drawBottomInfoText();
            this.creatorText = creatorInfoText(CREATOR_INFO);

            //method to just if ball is on break and then break it if it is and have ball bounce off brick
            breakBrick();

            //add the graphics to scene
            if (lives > 0)
                add(this.ball);
            add(this.bottomLine);
            add(this.infoText);
            add(this.creatorText);
            add(this.paddle);

            //checking if ball in motion and displays center text if ball not in motion
            if (!this.playing) {
                add(this.whiteBox);
                if (this.lives > 0) {
                    add(this.startingText);
                } else {
                    add(this.loseText);
                }
            } else {
                remove(this.startingText);
                remove(this.whiteBox);
                remove(this.loseText);
            }

            //if they win, show winning text
            if(this.brickCount <= 0){
                add(this.whiteBox);
                add(this.winText);
            }
            else{
                remove(winText);
            }

            //call method of changing velocity
            gravityAcceleration(GRAVITY, MAX_VELOCITY_Y, MIN_VELOCITY_Y);

            //render code every frame depending on frame rate
            pause(1000 / (double) this.FPS);

            //remove graphics from scene (this is so the graphics refresh on the screen, giving the illusion of moving)nd no duplication
            remove(this.ball);
            remove(this.paddle);
            remove(this.bottomLine);
            remove(this.infoText);
            remove(this.creatorText);

            //refresh the graphics if the screen size is moved/changed to keep scale of graphics
            if (this.windowWidth != getWidth() || this.windowHeight != getHeight()) {
                refreshGraphics();
            }
        }

    }

    /**
     * Executed when mouse is moved
     *
     * @param me information on the mouse
     */
    @Override
    public void mouseMoved(MouseEvent me) {
        int mousePositionX = me.getX();
        //paddle bar position follows mouse unless it is outside the scene view
        if (isBetweenNumberRange(mousePositionX, this.paddleWidth / 2, this.windowWidth - (this.paddleWidth / 2))) {
            this.paddleMousePositionX = mousePositionX;
            this.paddlePositionX = this.paddleMousePositionX - (this.paddleWidth / 2);
        }

    }

    @Override
    public void mousePressed(MouseEvent me) {
        //move the ball if user press a key
        if (!this.playing && this.lives > 0 && this.brickCount > 0) {
            this.velocityBallX = this.desireVelocityBallX;
            this.velocityBallY = this.desireVelocityBallY;
            this.playing = true;
            remove(this.startingText);
            remove(this.whiteBox);
        }
    }

    /**
     * Creates a brick grid
     *
     * @param amountColumn sets the amounts of brick in a column
     * @param positionX    sets the x coordinate for the grid of bricks (the left point)
     * @param positionY    sets the y coordinate for the grid of bricks (the top most)
     * @param gridWidth    sets the width of the grid of bricks
     * @param gridHeight   sets the height of the grid of bricks
     * @param gapSize      sets the gap between the bricks
     */
    private void createBricks(int amountColumn, int positionX, int positionY, int gridWidth, int gridHeight, int gapSize) {
        //initiate variable for math
        int fullGapAmountWidth = (gapSize * (amountColumn - 1));
        int fullGapAmountHeight = (gapSize * (12 - 1));
        double brickWidth = (gridWidth - fullGapAmountWidth) / (double) amountColumn;
        double brickHeight = (gridHeight - fullGapAmountHeight) / (double) 12;
        double xPointDraw;
        double yPointDraw;
        //loops through each brick to set the position and size of bricks in an array
        for (int i = 0; i < this.BRICK_AMOUNT_ROWS; i++) {
            yPointDraw = ((brickHeight + gapSize) * i) + positionY;
            for (int j = 0; j < amountColumn; j++) {
                xPointDraw = ((brickWidth + gapSize) * j) + positionX;
                this.brickInfo[getLoopCount(i, j, amountColumn)][0] = xPointDraw;
                this.brickInfo[getLoopCount(i, j, amountColumn)][1] = yPointDraw;
                this.brickInfo[getLoopCount(i, j, amountColumn)][2] = brickWidth;
                this.brickInfo[getLoopCount(i, j, amountColumn)][3] = brickHeight;
                if (!this.brickAlreadyDrawn) {
                    this.brickInfo[getLoopCount(i, j, amountColumn)][4] = 1;
                }
            }
        }

        //loops through each brick to create graphic
        for (int i = 0; i < this.brickBrick.length; i++) {
            //initate variable
            Color oldBrickColor = Color.BLACK;
            //if brick has been drawn, set color to the orinigal color
            if(this.brickAlreadyDrawn){
                oldBrickColor = this.brickBrick[i].getFillColor();
            }
            //create brick object
            this.brickBrick[i] = new GRect(this.brickInfo[i][0], this.brickInfo[i][1], this.brickInfo[i][2], this.brickInfo[i][3]);
            this.brickBrick[i].setFilled(true);
            //if drawn, keep the color, else randomize the color bricks
            if(this.brickAlreadyDrawn){
                this.brickBrick[i].setFillColor(oldBrickColor);
                this.brickBrick[i].setColor(oldBrickColor);
            }
            else{
                this.brickBrick[i].setFillColor(RAINBOW_COLOR[random.nextInt(6)]);
                this.brickBrick[i].setColor(RAINBOW_COLOR[random.nextInt(6)]);
            }
            add(this.brickBrick[i]);
            //if brick is broken, remove it
            if (this.brickInfo[i][4] == 0) {
                remove(this.brickBrick[i]);
            }
        }

        this.brickAlreadyDrawn = true;
    }

    /**
     * Checks if a brick touches the ball and break it
     * <p>
     * checks if the bricks is close enough to test if ball is on brick.
     * if it is, checks to see if a coordinate in the brick is with the ball area.
     * if it is, checks to see if the coordinate that is with ball area
     * is either from the top, bottom, left, right portion of the brick.
     * Once that is determined, it changes the velocity direction based on result.
     * Lastly it makes the brick disappear
     * 
     */
    private void breakBrick() {
        //check if ball is above the height of bricks drawn
        if (ballPositionCenterY < ((brickInfo[0][3] + BRICK_GAP_SIZE) * BRICK_AMOUNT_ROWS) + ballDiameter) {
            //loops each brick to check if ball hits the brick
            bricksLoop:
            for (int i = 0; i < this.brickBrick.length; i++) { //loops each brick
                //variable into array, makes it easy to read math
                double brickPositionX = this.brickInfo[i][0];
                double brickPositionY = this.brickInfo[i][1];
                double brickWidth = this.brickInfo[i][2];
                double brickHeight = this.brickInfo[i][3];
                boolean doesBrickExist = this.brickInfo[i][4] == 1;
                /*
                 *if circle radius of the brick circumscribed and the ball radius is closer than distance
                 *of center point of the rectangle to the center of the ball
                 *then run complex algorithm (for optimization)
                 */
                if (distanceAwayTwoCoors(this.ballPositionCenterX, brickPositionX + (brickWidth / 2.0), this.ballPositionCenterY, brickPositionY + (brickHeight / 2.0)) <= (this.ballDiameter + Math.hypot(brickWidth, brickHeight)) && doesBrickExist) {
                    //nested loop going through each coordinate point on the brick as a grid
                    for (int coordX = (int) Math.round(brickPositionX); coordX < (brickWidth + brickPositionX); coordX++) { //loops each x point window on brick
                        for (int coordY = (int) Math.round(brickPositionY); coordY < (brickHeight + brickPositionY); coordY++) { //loops each y point window on brick
                            //checks to see if that coordinate point is within the circle, runs if it is true
                            if (isPointWithinCircle(coordX, coordY, this.ballPositionCenterX, this.ballPositionCenterY, this.ballDiameter / (double) 2)) {  //if point x and point y are with the circle math
                                remove(this.brickBrick[i]); //remove the brick the circle is touching
                                this.brickInfo[i][4] = 0; //set brick number for is existing to 0(false)
                                this.brickCount--;
                                this.ballColor = brickBrick[i].getFillColor(); //change color to ball color
                                colorChangeXSpeed(brickBrick[i].getFillColor());
                                double horizontalShiftGraph = getCenterPointFromLeftCorner(brickPositionX, brickWidth); //variable of amount of horizontal shifting the graph has to do for the brick to have center point (0,0)
                                double verticalShiftGraph = getCenterPointFromLeftCorner(brickPositionY, brickHeight); //variable of amount of vertical shifting the graph has to do for the brick to have center point (0,0)
                                double riseOverRun = slope(brickHeight, brickWidth); //variable of the slope of the corner of the brick to the opposite corner
                                /*
                                 *splits the brick into 4 trigger area that is cut out of a x from the brick
                                 *used the absolute value graph equation to divide the x within the bricks
                                 */
                                //true if coordinate point of brick that is on the circle is from the bottom
                                if (((riseOverRun * ((Math.abs(coordX)) - horizontalShiftGraph))) < coordY - verticalShiftGraph) { //formula: m * |x - horizontal| < y - vertical
                                    this.velocityBallY = Math.abs(this.velocityBallY); //hits bottom side of brick so velocity is positive (going down)
                                    this.desireVelocityBallY = this.velocityBallY;
                                    break bricksLoop; //breaks out of complex loop for optimization
                                }
                                //true if coordinate point of brick that is on the circle is from the top
                                else if (((-1 * riseOverRun * ((Math.abs(coordX)) - horizontalShiftGraph))) > coordY - verticalShiftGraph) { //formula: -m * |x - horizontal| < y - vertical
                                    this.velocityBallY = -1 * Math.abs(this.velocityBallY); //hits top side of brick so velocity is negative (going up)
                                    this.desireVelocityBallY = this.velocityBallY;
                                    break bricksLoop; //breaks out of complex loop for optimization
                                }
                                //true if coordinate point of brick that is on the circle is from the right
                                else if ((-1 * (Math.abs(coordY))) + verticalShiftGraph > ((riseOverRun * (coordX - horizontalShiftGraph)))) { //formula: -|y| - vertical > m(x - horizontal)
                                    this.velocityBallX = -1 * Math.abs(this.velocityBallX); //hits right side of brick so velocity is positive (going left)
                                    this.desireVelocityBallX = this.velocityBallX;
                                    break bricksLoop; //breaks out of complex loop for optimization
                                }
                                //true if coordinate point of brick that is on the circle is from the left
                                else if (Math.abs(coordY) - verticalShiftGraph < ((riseOverRun * (coordX - horizontalShiftGraph)))) { //formula: |y| - vertical > m(x - horizontal)
                                    this.velocityBallX = Math.abs(this.velocityBallX); //hits left side of brick so velocity is negative (going right)
                                    this.desireVelocityBallX = this.velocityBallX;
                                    break bricksLoop; //breaks out of complex loop for optimization
                                }
                                //if a point is exactly on the line of the split x, highly unlikely due to the double precision
                                else {
                                    this.velocityBallY = Math.abs(this.velocityBallY); //send the ball down
                                    break bricksLoop; //breaks out of complex loop for optimization
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if coordinate point is within a circle
     *
     * @param pointX  point x of a coordinate pair that is being checked
     * @param pointY  point y of a coordinate pair that is being checked
     * @param centerX point x of the center of circle
     * @param centerY point y of the center of circle
     * @param radius  the radius of circle
     * @return true if point is within the circle
     */
    private boolean isPointWithinCircle(double pointX, double pointY, double centerX, double centerY, double radius) {
        double circleGraph1 = Math.pow(pointX - centerX, 2) + Math.pow(pointY - centerY, 2);
        double circleGraph2 = Math.pow(radius, 2);
        return circleGraph1 < circleGraph2;
    }

    /**
     * Gets the number of amount of times looped
     *
     * @param firstLoopInt         integer of the first loops integer (normally i)
     * @param secondLoopInt        integer of the second loop integer (normally j)
     * @param secondLoopTruthLimit integer of the second loop that checks if number is greater than
     * @return integer of times looped in two nested loop
     */
    private int getLoopCount(int firstLoopInt, int secondLoopInt, int secondLoopTruthLimit) {
        return ((secondLoopTruthLimit - 1) * firstLoopInt) + firstLoopInt + secondLoopInt;
    }

    /**
     * Checks if the number is between two numbers
     *
     * @param check       number that needs to be checked
     * @param smallNumber the smallest number in the range
     * @param bigNumber   the largest number in the range
     * @return return true if the check number is between the number range
     */
    private boolean isBetweenNumberRange(int check, int smallNumber, int bigNumber) {
        return check > smallNumber && check < bigNumber;
    }

    /**
     * Gets the distance between two points
     * <p>
     * Function: sqrt((x1 - x2)^2 + (y1 - y2)^2)
     *
     * @param x1 x of first coordinate pair
     * @param x2 x of second coordinate pair
     * @param y1 y of first coordinate pair
     * @param y2 y of second coordinate pair
     * @return return the distance between to coordinate pairs
     */
    private double distanceAwayTwoCoors(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * Gets the slope
     * <p>
     * gets the slope of the height or the difference two y points and the width or the difference of two x points.
     * 
     * @param rise the height or the difference of two y points
     * @param run  the width or the difference of two x points
     * @return returns the slope
     */
    private double slope(double rise, double run) {
        return rise / run;
    }

    /**
     * Gets the center point of either axis
     * <p>
     * Function: x or y + (size / 2)
     * 
     * @param axisCoordinate either x or y coordinate
     * @param size           either width or height of object
     * @return return the x or y of center.
     */
    private double getCenterPointFromLeftCorner(double axisCoordinate, double size) {
        return axisCoordinate + (size / 2.0);
    }

    /**
     * draws the ball
     *
     * @return the circle of the ball object
     */
    private GOval drawBall() {
        //create the ball
        GOval ball = new GOval(this.ballDiameter, this.ballDiameter);
        ball.setFillColor(this.ballColor);
        ball.setFilled(true);
        ball.setLocation(this.ballPositionCenterX - (this.ballDiameter / 2.0), this.ballPositionCenterY - (this.ballDiameter / 2.0));

        //checks if ball hit the side edges of the scene and reverse direction
        if (this.ballPositionCenterX < 0 + (ballDiameter / 2.0)) {
            this.ballColor = Color.GRAY;
            this.velocityBallX = Math.abs(this.velocityBallX); //hits left side of wall so velocity is positive (going right);
        }
        else if(this.ballPositionCenterX > this.windowWidth - (ballDiameter / 2.0)){
            this.ballColor = Color.GRAY;
            this.velocityBallX = -1 * Math.abs(this.velocityBallX); //hits right side of wall so velocity is negative (going right);
        }
        //checks if ball hits the top or bottom of scene
        if(this.ballPositionCenterY < 0 + (ballDiameter / 2.0)){
            this.velocityBallY = Math.abs(this.velocityBallY); //hits top of wall so velocity is positive (going down);
            this.ballColor = Color.GRAY;
        }
        else if (this.ballPositionCenterY > this.linePositionY + (ballDiameter / 2.0)){
            this.ballPositionCenterX = this.windowWidth / 2;
            this.ballPositionCenterY = this.windowHeight / 2;
            this.velocityBallY = 0;
            this.velocityBallX = 0;
            this.lives--;
            this.playing = false;
            this.desireVelocityBallX = this.velocityChoicesX[random.nextInt(6)];
        }

        //checks if ball hits the paddle bar and reverse direction
        if ((isBetweenNumberRange(this.ballPositionCenterX, this.paddlePositionX, this.paddlePositionX + this.paddleWidth)) && (isBetweenNumberRange((int) (ball.getY() + ball.getHeight()), this.paddlePositionY, this.paddlePositionY + this.PADDLE_HEIGHT))) {
            this.paddleColor = ballColor;
            this.velocityBallY = -1 * Math.abs(this.velocityBallY);
            this.desireVelocityBallY = this.velocityBallY;
        }

        if (this.brickCount <= 0){
            this.velocityBallX = 0;
            this.velocityBallY = 0;
        }

        //add the velocity
        this.ballPositionCenterX += this.velocityBallX;
        this.ballPositionCenterY += this.velocityBallY;
        return ball;
    }

    /**
     * draws the bottom paddle board
     *
     * @return the rectangle of the paddle board object
     */
    private GRect drawPaddle() {
        GRect paddle = new GRect(this.paddleWidth, this.PADDLE_HEIGHT);
        if(this.paddlePositionX > windowWidth){
            paddle.setLocation(this.windowWidth - paddleWidth, this.paddlePositionY);
        }
        else if(this.paddlePositionX < 0){
            paddle.setLocation(paddleWidth, this.paddlePositionY);
        }
        else{
            paddle.setLocation(this.paddlePositionX, this.paddlePositionY);
        }
        paddle.setFillColor(this.paddleColor);
        paddle.setFilled(true);
        return paddle;
    }

    /**
     * draws the bottom bounding line
     *
     * @return the line of the bottom line
     */
    private GLine drawBottomLine() {
        return new GLine(0, this.linePositionY, this.windowWidth, this.linePositionY);
    }

    /**
     * Refreshes the graphics to fit in scale
     * <p>
     * It changes all the variables so the graphics are scaled
     */
    private void refreshGraphics() {
        //have the size variable change to correct size
        this.windowWidth = getWidth();
        this.windowHeight = getHeight();

        //reset variable to their proper size since window size changed
        this.ballDiameter = (this.windowWidth > this.windowHeight) ? this.windowWidth / 50 : this.windowHeight / 50;
        this.paddleWidth = this.windowWidth / 10;
        this.brickGridHeight = windowHeight / 3;
        this.linePositionY = windowHeight - BOTTOM_BOUNDING_MARGIN + 10;
        this.paddlePositionY = windowHeight - BOTTOM_BOUNDING_MARGIN;

        //remove the text so it doesn't duplicate on scene
        remove(this.startingText);
        remove(this.whiteBox);
        remove(this.loseText);
        remove(this.winText);
        remove(this.centerImg);

        //declare new text
        startingText = drawCenterText(this.BEGIN_TEXT);
        loseText = drawCenterText(this.LOOSE_TEXT);
        winText = drawCenterText(this.WIN_TEXT);

        //change text and ball position depending on while the ball is in motion or not
        if (!this.playing) {
            this.ballPositionCenterX = this.windowWidth / 2;
            this.ballPositionCenterY = this.windowHeight / 2;
            if (this.lives > 0) {
                add(this.startingText);
                this.whiteBox = drawCenterBox(this.startingText.getWidth() + WHITEBOX_MARGIN_LEFT_RIGHT, this.startingText.getHeight() + WHITEBOX_MARGIN_TOP_BOTTOM);
            } else {
                add(this.loseText);
                this.whiteBox = drawCenterBox(this.loseText.getWidth() + WHITEBOX_MARGIN_LEFT_RIGHT, this.loseText.getHeight() + WHITEBOX_MARGIN_TOP_BOTTOM);
            }
        } else {
            remove(this.startingText);
            remove(this.whiteBox);
            remove(this.loseText);
        }
        //change text if win
        if(this.brickCount <= 0){
            add(this.winText);
            this.whiteBox = drawCenterBox(this.winText.getWidth() + WHITEBOX_MARGIN_LEFT_RIGHT, this.winText.getHeight() + WHITEBOX_MARGIN_TOP_BOTTOM);
            this.ballPositionCenterX = this.windowWidth / 2;
            this.ballPositionCenterY = this.windowHeight / 2;
        }
        else{
            remove(this.winText);
        }
        //removes all bricks so they can be redrawn
        for (GRect brickBox : brickBrick) {
            remove(brickBox);
        }
        this.centerImg = drawCenterImg();
        add(this.centerImg);
        //redraws the grid of bricks
        createBricks(this.BRICK_AMOUNT_COLUMNS, 0, 0, this.windowWidth, this.brickGridHeight, this.BRICK_GAP_SIZE);
    }

    /**
     * draws the bottom information text
     *
     * @return returns the label object of the bottom text graphic
     */
    private GLabel drawBottomInfoText() {
        GLabel label = new GLabel("Bricks left: " + this.brickCount + "     " + "Lives left: " + this.lives);
        label.setFont(this.INFO_TEXT_FONT);
        label.setLocation(this.PADDLE_HEIGHT, this.linePositionY + this.PADDLE_HEIGHT * 2);
        return label;
    }

    /**
     * draws the center game text
     *
     * @param message string of the center text message
     * @return the label object of the center of the scene text graphic
     */
    private GLabel drawCenterText(String message) {
        GLabel label = new GLabel(message);
        label.setFont(this.CENTER_TEXT_FONT);
        label.setLocation((this.windowWidth / 2.0) - (label.getWidth() / 2), (this.windowHeight / 2.0) - (this.ballDiameter * 2));
        return label;
    }

    /**
     * Draws the white box behind the center text
     *
     * @param width width of the box
     * @param height height of the box
     * @return the rect object of the center of the scene white box
     */
    private GRect drawCenterBox(double width, double height){
        GRect box = new GRect(width, height);
        box.setFillColor(Color.WHITE);
        box.setFilled(true);
        box.setLocation((this.windowWidth / 2.0) - (box.getWidth() / 2), ((this.windowHeight / 2.0) - (this.ballDiameter * 2) - (height / 1.5)));
        return box;
    }

    /**
     * Draw center image of game
     *
     * @return the image object of the center of scene image
     */
    private GImage drawCenterImg(){
        GImage image = new GImage(CENTER_IMAGE_FILE);
        image.setLocation((this.windowWidth / 2.0) - (image.getWidth() / 2.0), (this.windowHeight / 2.0) - (image.getHeight() / 2.0));
        return image;
    }

    /**
     * Changes the velocity variable to accelerate or accelerate negatively depending on the direction
     *
     * @param acceleration acceleration multiplier
     * @param maxVelocity the maximum velocity limit
     * @param minVelocity the minimum velocity limit
     */
    private void gravityAcceleration(double acceleration, double maxVelocity, double minVelocity){
        //checks if velocity is positive or negative (up or down)
        if(this.velocityBallY > 0){
            this.velocityBallY *= acceleration; //accelerate the ball due to gravity
            //if velocity past max or min velocity, change velocity to max or min
            if(this.velocityBallY > Math.abs(maxVelocity)){
                this.velocityBallY = Math.abs(maxVelocity);
            }
            else if (this.velocityBallY < Math.abs(minVelocity)){
                this.velocityBallY = Math.abs(minVelocity);
            }
        }
        else if(this.velocityBallY < 0){
            this.velocityBallY *= (1 / acceleration); //accelerate the ball due to gravity
            //if velocity past max or min velocity, change velocity to max or min
            if(this.velocityBallY <  -1 * Math.abs(maxVelocity)){
                this.velocityBallY = -1 * Math.abs(maxVelocity);
            }
            else if(this.velocityBallY > Math.abs(minVelocity)){
                this.velocityBallY = -1 * Math.abs(minVelocity);
            }
        }
        else{
            this.velocityBallY = 0; //keep velocity at zero because it's suppose to be zero
        }
    }

    /**
     * Changes the x velocity based on the color
     *
     * Is mainly used to change the speed of ball when it hit a specific color brick
     *
     * @param color checks the color of object for what the color should change to
     */
    private void colorChangeXSpeed(Color color){
        //color checker for what the speed should change to
        if(color == Color.RED){
            velocityBallX *= (1 / 1.5);
        }
        else if(color == Color.YELLOW){
            velocityBallX *= (1 / 1.2);
        }
        else if (color == Color.GREEN){
            velocityBallX *= 1.5;
        }
        else if (color == Color.MAGENTA){
            velocityBallX *= 1.2;
        }
        //if less than 1, the math will cause error due to fraction multiplication making it go closer to 0
        if(Math.abs(velocityBallX) < 1){
            velocityBallX = 1;
        }
    }

    /**
     * Creator text info on bottom right corner
     * 
     * @param message the message for the info
     * @return the label object of the info text on bottom right corner
     */
    private GLabel creatorInfoText(String message){
        GLabel label = new GLabel(message);
        label.setFont(this.CENTER_TEXT_FONT);
        label.setLocation(this.windowWidth - this.PADDLE_HEIGHT - label.getWidth(), (this.linePositionY + this.PADDLE_HEIGHT * 2) + label.getHeight());
        return label;
    }
}
