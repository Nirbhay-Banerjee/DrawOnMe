package com.example.drawonme
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_main.view.*

import kotlin.collections.ArrayList

//This class or view contains a lot of graphic terms..which may be looked up later
//This class we will use as a view and not as an activity thus it doesn't need to extend from
// appCompatActivity
//This will be a view inside an activity


class DrawingView(context:Context,attrs:AttributeSet): View (context,attrs) {


    //All these variables will be initialised when we initialise the drawingView
    //These variables are basically the required variables or attributes or information
    //which must be stored to actually draw something on the screen

    private var mDrawPath : MyCustomPath? = null   //path of the drawing
    private var mCanvasBitmap : Bitmap? = null     //The array of graphics or pixels
    private var mDrawPaint : Paint? = null
    private var mCanvasPaint : Paint? = null
    private var mBrushSize :Float= 0.toFloat()
    var mColor = Color.BLACK
    private var mCanvas :Canvas? = null   //the white screen on which we can draw
    
    //the following variable's function is to store all the paths which a user draws and persist
    //them on the screen,or else on action finger up,the path would be vanished
    private val mPaths = ArrayList<MyCustomPath>()

    //this one i am creating to add the redo functionality as well
    //simple idea is that for undo,we need to remove the last object from mPaths,and for redo we need
    //to re add that object to mPaths.To do so we must store the undone paths to another array
    private val mUndoPaths = ArrayList<MyCustomPath>()


    init{
        setUpDrawingAbility()
    }

    private fun setUpDrawingAbility() {

        mDrawPaint = Paint()
        mDrawPath = MyCustomPath(mColor,mBrushSize)
        //We need to assert it that mDrawpaint wont be null cuz here we know that we just initialised
        //it as an object of paint. There are some properties of the paint class objects which we need
        //to set up for this drawPaint as well
        mDrawPaint!!.color = mColor
        mDrawPaint!!.style = Paint.Style.STROKE   //WE can right now only stroke,or draw lines
        //Some properties of stroke which are needed to be set.
        //cap is the kind of shape at line endings and join is the style of join for connecting
        // different lines and curves etc
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND

        //initialising the canvas paint object
        //the function of dithering flag is to allow dithering when blitting
        //To “blit” is to copy bits from one part of a computer's graphical memory to another part
        //This technique deals directly with the pixels of an image, and draws them directly to the screen,
        //which makes it a very fast rendering technique
        //Dithering means juxtaposing pixels of two colors to create
        //the illusion that a third color is present. Or easily some type of shaking

        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        //mBrushSize = 20.toFloat()not required now,cuz we can just set the size from the helper
        //method we created


    }


    //This method is called whenever the layout is inflated so here we
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //Here we create the bitmap, which is our method of representing images, with a config of
        //Argb 8888 implies each of the rgb colors can have at most 8 bits or 256 vals,thus our
        //total no of usable colorrs is 256 to power 3. The a stands for translucency alpha
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mCanvasBitmap!!)
    }

    //WE ned to implemnt the functionality of what we need to do when we draw
    //This method just like the previous one comes to us from the View class
    override fun onDraw(canvas: Canvas)//Changed the Canvas? to just canvas
     {
        super.onDraw(canvas)

        //Basically we wanna draw a bitmap with starting coordinates and the paint

         canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)


         // now we need to draw the path which is basically why we are doing all this
        //Only if the mDrawPath is not empty..basically means if our path is not empty,draw
         // Because we cant actually draw an empty path on the screen..basically this is condition
         // before a touch has happened
         //Touches will be detected later using motions sensors class

         //If we need the figures that we draw to persist on the screen,we need to store them
         //The figures are basically custom paths,so we can create a simple arraylist of custom
         //paths in which we can store the paths,which we can later display in this onDraw method
         //The paths must be stored even after the touch has been released thus we need to use
         //action up code block and make some editing there

         //To let the lines persist on the screen we need to show the paths that are stored in
         //the arraylist

         for(paths in mPaths )
         {
             mDrawPaint!!.strokeWidth = paths.brushSize
             mDrawPaint!!.color = paths.color
             canvas.drawPath(paths, mDrawPaint!!)

         }

         if(!mDrawPath!!.isEmpty) {
             //here actually the drawing is done as we set stroke width and color of the path
                 // it actually draws according to our finger movement.
                     //Following are the lines of code that actually show something on the screen

             mDrawPaint!!.strokeWidth = mDrawPath!!.brushSize
             mDrawPaint!!.color = mDrawPath!!.color
             canvas.drawPath(mDrawPath!!, mDrawPaint!!)
         }

         //Now we have a whole thing of "what" needs to be done when we draw
         //But the concept of "when" actually is a drawing made has not been implemented yet
         //Otherwise this onDraw method wont show anything on the screen because the drawpath will
         //Initially be empty.So we gotta change that through touches
    }


    //Now we need to implement that a drawing should happen on touches

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //These variables to keep track of the touch event coordinates
        val touchX = event?.x
        val touchY = event?.y

        //A touch event has multiple actions on which different functionalities can be performed!
        //The 3 most generic and important are
        // up(when we remove our finger from the screen or release the touch),
        // down (when we bring out finger to the screen or touch the screen)
        // and move (when we drag our finger on the screen)

        when(event?.action)
        {
            //If the action is down that is user touches screen
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.brushSize=  mBrushSize
                mDrawPath!!.color = mColor

                mDrawPath!!.reset() //If any other figures were there remove them,

                //Being double sure that indeed a touch is made...cuz when it is the x and y coordinates
                //will change from null
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX,touchY)
                    }
                    //Alternatively we can assert not null of touchx and Y,but this way we wont be
                    //Double sure
                } //Just move the path to wherever touch has been made



             }


            MotionEvent.ACTION_MOVE ->{

                //Same logic but here we just need to draw a line or a continuous figure as a drag
                //has been done
                if (touchY != null) {
                    if (touchX != null) {
                        mDrawPath!!.lineTo(touchX,touchY)
                    }
                }
            }


            MotionEvent.ACTION_UP -> {
                //here user removes finger from the screen so we need to
                //Basically creating a blank drawPath

                mPaths.add(mDrawPath!!)
                mDrawPath = MyCustomPath(mColor,mBrushSize)
                //mPaths.add(mDrawPath!!)

            }
            else->return false
        }

        //Here we invalidate the whole view. If it is visible,the canvas will be called sometime in
        //future from a ui thread

        invalidate()

        return true //if any of the case of motion event is true except the default return true
    }


    //helper method to change the brush size
    fun setBrushSize (size:Float)
    //the use of typed value is written in notepad
    //apply dimensions method takes 3 parameter,one will be what unit we wanna convert into..
    //one will be from what unit and other will be display metrics of the current screen.
    {
        //Depending upon screen size and pixels now the float 20 may have different dimensions

        mBrushSize =

            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,size,resources.displayMetrics)


        mDrawPaint!!.strokeWidth = mBrushSize
    }

    //helper to add color selection functionality

    //parse color method takes input color string and returns color as an int!!
    //returns illegal argument exception in case the parameter entered is not of the type rrggbb,or aarrggbb

    fun selectColor(selectedColor:String)
    {
        mColor = Color.parseColor(selectedColor)
        mDrawPaint!!.color = mColor

        //drawing_view.setBackgroundColor(mColor)
    }

    fun selectColor(selectedColor:Int)
    {
        mColor = selectedColor
        mDrawPaint!!.color = mColor

       // drawing_view.setBackgroundColor(mColor)
    }

    fun undoLast()
    {
        if(mPaths.isNotEmpty())
       mUndoPaths.add( mPaths.removeAt(mPaths.size-1))

        //now invalidate needs to be called because it will call the onDraw method again and draw the
        //paths,just this time the size will be one less of the mPaths
        invalidate()
    }

    fun redoLast()
    {
        if(mUndoPaths.isNotEmpty())
            mPaths.add(mUndoPaths.removeAt(mUndoPaths.size-1))


        //now invalidate needs to be called because it will call the onDraw method again and draw the
        //paths,just this time the size will be one more of the mPaths

        invalidate()
    }






    //Will make this class inner in order to use the variables to and fro
    internal inner class MyCustomPath(var color : Int,var brushSize : Float) : Path() {

}
}