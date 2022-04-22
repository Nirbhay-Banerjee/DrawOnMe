package com.example.drawonme

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.android.synthetic.main.brush_size.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {


    private var mDefaultColorSelected : ImageButton? = null
    private var colorCurrent : Int = Color.BLACK


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setBrushSize(20.toFloat())


        //Here we are using the simple concept of accessing the colors of the pallet or the linear layout
        //just like an arraylist
        mDefaultColorSelected = ll_color_pallet[0] as ImageButton
        //basically changing the layout of the selected image button
        mDefaultColorSelected!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_selected))


        //undo and redo functionalities


        ib_undo.setOnClickListener(){
            drawing_view.undoLast()
        }


        ib_redo.setOnClickListener()
        {
            drawing_view.redoLast()
        }

        //USING AMBILWARNA library to implement the functionality of custom colors to the app
        //Added implementation in the build and used it as per snippet on github

        btn_custom_color.setOnClickListener()
        {

            //in order to instantiate an interface,took help of the object class
            val dialog = AmbilWarnaDialog(this@MainActivity,colorCurrent,true,
                object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onCancel(dialog: AmbilWarnaDialog?) {



                    }

                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {

                        //also overloaded the selectColor method in the drawing view so that i can directly pass int color
                        //to it and set it to the mDrawPaint directly.

                        colorCurrent = color

                        drawing_view.selectColor(color)

                    }
                })
            dialog.show()

            //setting the color selected background to default only once the color is selected from this dialog box
            //and not the pallet
            for(i in 0..5)
            {
                val checkPalletDefault = ll_color_pallet[i] as ImageButton
                checkPalletDefault.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_default))
            }
            //solving a simple bug,that needed the user to click twice on any color after dialog to use it from pallet
            //again.Did some modification in the fun palletButtonSelected also
            mDefaultColorSelected = ll_color_pallet[0] as ImageButton


        }


        //implemented the saving functionality as an async task,calling through the trigger ie,save button

        ib_save.setOnClickListener(){
        if(isPermissionGranted()){
           // Toast.makeText(this,"Reached here",Toast.LENGTH_SHORT).show()

           val bitmap:Bitmap =  getBitmapOfView(fl_drawing)
            BitmapAsyncTask(bitmap).execute()
        }
            else{
                requestStoragePermission()
            }
        }


        val ibBrushSize = ib_brush_sizes

        ibBrushSize.setOnClickListener{
            showBrushSizeDialog()
        }

        //this is the trigger for permission,so im setting it.

        ib_gallery.setOnClickListener()
        {
            if(isPermissionGranted())
            {
                //What all i need to do with it
              //  Toast.makeText(this,"You can do it now",Toast.LENGTH_LONG).show()

                //Now we need to add the functionality of selecting image for gallery.For this intent
                //will be used as it enables us to traverse between activities and more or less that is
                //exactly what we need to do rn.

                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                startActivityForResult(pickPhotoIntent, GALLERY_CODE)


            }
            else{
                requestStoragePermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //using the simple process

        if(resultCode == Activity.RESULT_OK)
        {
            if(requestCode == GALLERY_CODE)
            {       //the data is parameter name of the intent which we pass,and that intent is having
                    // a property called data.We need to see if that data is null or not,ie if the user
                        // has selected an image or not
                try {
                    if(data!!.data != null) //if user has selected something
                    {
                        img_selected.visibility =  View.VISIBLE
                        //uri was basically the link on your device
                        img_selected.setImageURI(data.data)
                    }
                    else{
                        Toast.makeText(this,"Some error occurred",Toast.LENGTH_SHORT).show()
                    }

                }
                catch (e:Exception)
                {
                    e.printStackTrace()
                }

            }
        }
    }

    //Creating a custom dialog for selecting brush sizes

    private fun showBrushSizeDialog()
    {
        //the dialog class can be used
        //3 steps to make a custom dialog
        //1- make the layout resource file
        //2-create object of Dialog class
        //3-set its content view to the desired layout resource file

        val brushDialog = Dialog(this)

        brushDialog.setContentView(R.layout.brush_size)
        brushDialog.setTitle("Select Brush Size")



        val ibLarge = brushDialog.ib_large
        val ibMedium = brushDialog.ib_med
        val ibSmall = brushDialog.ib_small
        val ibSmallTwo = brushDialog.ib_small_two
        val ibMedTwo = brushDialog.ib_med_two

        ibSmall.setOnClickListener{
            drawing_view.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        }
        ibSmallTwo.setOnClickListener{
            drawing_view.setBrushSize(15.toFloat())
            brushDialog.dismiss()
        }



        ibMedium.setOnClickListener{
            drawing_view.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        }

        ibMedTwo.setOnClickListener{
            drawing_view.setBrushSize(25.toFloat())
            brushDialog.dismiss()
        }
        ibLarge.setOnClickListener{
            drawing_view.setBrushSize(40.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()

    }
    //helper
    //through this method we check which of the img buttons is selected by the user and then we will ultimately
    //give the color functionality of it subsequently

    //this method is set as the onClick for all paint colors of the pallet
    fun palletButtonSelected(view: View)
    {
        //first check,see if it is only the default color selected

        val currentColor = view as ImageButton

        //this is the modification that has been done after the custom color box implementation
        if(view == ll_color_pallet[0] as ImageButton)
        {
            currentColor.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_selected))
            if(mDefaultColorSelected !== ll_color_pallet[0] as ImageButton )
            {
                mDefaultColorSelected!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_default))
            }
            drawing_view.selectColor(currentColor.tag.toString())
            mDefaultColorSelected = view

        }

        if(view !==  mDefaultColorSelected)
        {
           // val currentColor = view as ImageButton
            //Changing the appearance of the selected colors
            currentColor.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_selected))
            mDefaultColorSelected!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_default))

        //now we need this method to perform different functions for each image button which is precisely the reason
        //we have used this concept of tags.Now we can just read the tag and do stuff accordingly.Here we learn
        //some very important techniques to create good stuff and reduce the bulkiness of our application.


            //we cant directly use select color,we need to specify select color is present in which class
         drawing_view.selectColor(currentColor.tag.toString())
            //we select the color according to the tags of the image buttons

            mDefaultColorSelected = view

        }


    }






    //following the syntax for permission granting and using a cleaner approach as well

    //Following lines are implementation of permissions feature
    private fun requestStoragePermission()
    {
        //we just add this line to explain  user why actually do we need a particular permission
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE).toString()))
        {
            Toast.makeText(this,"Need permission for setting background",Toast.LENGTH_SHORT).show()
        }

        ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_ACCESS)


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == STORAGE_PERMISSION_ACCESS )
        {
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"Thank You permission granted",Toast.LENGTH_LONG).show()
            }
            else
            {
                Toast.makeText(this,"Please grant permission from settings",Toast.LENGTH_LONG).show()

            }
        }

    }

    //helper

    /*Following method checks if the permission is already granted by the user or not.For
    * this purpose the ContextCompat class is used and this enables us to not ask for permission
    * each and everytime the user has clicked on the feature to access it.
     */


    private fun isPermissionGranted():Boolean
    {
        val check  = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)


        return check == PackageManager.PERMISSION_GRANTED
    }

/*
    Here while exporting or storing on device,our whole game is to convert the view,on which we have drawn stuff
    into a bitmap.Because bitmap/images can themselves be shared and stored but views cant be stored.
    So basically,what all we have done in the app was inside of the drawing view we created and
    now that drawing view is to be converted into a bitmap.

    Following is the method to solve our purpose
*/


private fun getBitmapOfView(view:View) : Bitmap

{
    //basically that view is converted to bitmap using .createBitmap method of the bitmap class.
    //Using this method a simple image can also be converted to bitmap as there are multiple overrides
    //for this method...we can just pass source of img as parameter.But,here we pass the dimensions of
    //the view as parameters,cuz that is what we need to convert to bitmap!!


//Step 1 we create a bitmap which is of the view
    val resultBitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)

    //Step 2 we get the canvas from the bitmap,or the view in specific
    val canvas =  Canvas(resultBitmap)

    //Step 3 we get the background
    val bgDrawable = view.background

    //Step 4 We draw the background on to the canvas (sandwiching)
    if(bgDrawable!=null)
    {

        bgDrawable.draw(canvas)
    }
    else
    {
        canvas.drawColor(Color.WHITE)
    }
    /*
    basically it is a 3 way thing containing background,view and canvas
    background---the image which we use in the bg,which can be nothing and to handle that case we have
    the above mentioned if else statement

    view---the view is the actual thing which contains our canvas

    canvas---where all our drawing and coloring is present.

    In the image to be saved,we need all those 3 so what we want is to sandwich these three layers into
    a single bitmap,which meets our purpose of required image!!
     */

    //Step 5 we finally draw the canvas to the view(sandwiching),and in turn the result bitmap
    view.draw(canvas)

    return resultBitmap


}


   /* fun showColorDialog(view: View) {


    }


    private fun openDialog(flag:Boolean)
    {





        var dialog = AmbilWarnaDialog(this,colorCurrent,flag,
            object : OnAmbilWarnaListener{
        override fun onCancel(dialog: AmbilWarnaDialog?) {
            TODO("Not yet implemented")


        }

        override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
            TODO("Not yet implemented")

            colorCurrent = color

            drawing_view.selectColor(colorCurrent.toString())

        }

    })
        dialog.show()

    }




*/
    //WE are performing the task of saving images to the device as an async task.
/*
    private inner class BitmapAsyncTask(val mBitmap:Bitmap):AsyncTask<Any,Void,String>(){
        override fun doInBackground(vararg params: Any?): String {
            var result = ""


            //Following are the steps to store an image/file/bitmap to the device
            //For this we need to work with streams...first the ByteOutputStream,because the bitmap
            //will actually be stored as an ordered collection of bytes
            //Then,the file output stream,to convert the byte collection to an actual file
            if(mBitmap!=null)
            {
                try{


                    val byteStream = ByteArrayOutputStream()
                    //bitmap being compressed to bytestream
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,byteStream)

                    //Through this line we are just defining the name of the file which is to be
                    //stored from that bitmap.In order to keep name unique,we use current time in millis
                    //feature.
                    val file = File(externalCacheDir!!.absoluteFile.toString()
                            + File.separator + "Doodle_" + System.currentTimeMillis()/1000 + ".png")

                    //getting the file as a file output stream
                    val fos = FileOutputStream(file)
                    fos.write(byteStream.toByteArray())
                    fos.close()
                    //basically now result contains the directory of our file to be stored

                    result = file.absolutePath
                }
                catch (e:Exception)
                {
                    result = ""
                    //Toast.makeText(this@MainActivity,"Some error occured",Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
            else{
                result = ""
            }

            if(result == "")
            {
                Toast.makeText(this@MainActivity,"Some error occurred",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this@MainActivity,"Saved successfully",Toast.LENGTH_SHORT).show()
            }


            return result

        }




    }
*/


    private inner class BitmapAsyncTask(val mBitmap: Bitmap): ViewModel(){

        fun execute() = viewModelScope.launch {

            onPreExecute()

            val result = doInBackground()

            onPostExecute(result)

        }

        //The following variable is for showing a custom progress bar to the user while the file is
        //being saved
        private lateinit var mProgressDialog: Dialog

        private fun onPreExecute() {

            showProgressDialog()

        }

        private suspend fun doInBackground(): String = withContext(Dispatchers.IO) {

            var result = ""

            if(mBitmap!=null){

                try{

                    val bytes = ByteArrayOutputStream()

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val f = File(externalCacheDir!!.absoluteFile.toString() + File.separator + "KidsDrawingApp_" + System.currentTimeMillis() / 1000 + ".png")

                    val fos = FileOutputStream(f)

                    fos.write(bytes.toByteArray())

                    fos.close()

                    result = f.absolutePath

                } catch (e: Exception){

                    result = ""

                    e.printStackTrace()

                }

            }

            return@withContext result

        }

        private fun onPostExecute(result: String?) {

            //once the saving is done,the progress dialog can vanish
            cancelDialog()

            //Showing a kind of toast to the user,that the file has been saved or has not been saved
            if(!result!!.isEmpty()) {

                Toast.makeText(

                    this@MainActivity,

                    "File Saved Succesfully : $result",

                    Toast.LENGTH_SHORT

                ).show()

            } else {

                Toast.makeText(this@MainActivity,

                    "Something went wrong while saving file",

                    Toast.LENGTH_SHORT).show()

            }


            //Only as soon as the image is ready can we share it,thus to add share functionality
            //we must go the onPostExecute method and do stuff fine from there!!

            //Following is the share functionality added

            //We use MediaScannerConnection class for sharing purpose

            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null){


                //using lambda expression passing path and uri as parameters
                    path, uri -> val shareIntent = Intent()

                //there are multiple actions but rn,we need to only share/send the image
                shareIntent.action = Intent.ACTION_SEND

                //some data which must be passed to the intent,its name and the uri..or destination
                //of the image to be shared
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)

                //this is basically the format in which we want to share it
                shareIntent.type = "image/png"

                startActivity(

                    //giving users a chooser to just select the app to share with
                    Intent.createChooser(

                        shareIntent, "Share Via..."

                    )

                )

            }

        }

        private fun showProgressDialog(){

            //here @MainActivity is necessary because only this would mean the bitmap async task class!
            mProgressDialog = Dialog(this@MainActivity)

            mProgressDialog.setContentView(R.layout.dialog_custom_progress)

            mProgressDialog.show()

        }

        private fun cancelDialog(){

            mProgressDialog.dismiss()

        }



    }
    companion object {
        private const val STORAGE_PERMISSION_ACCESS = 1
        private const val GALLERY_CODE = 2
    }




}





//SOME LEARNINGS
/*
UI CONCEPTS
->Layered List
We have actually created a kind of skeleton for the pallet,which describes how the pallet must look
We have also created a different type to show that currently this color of the pallet is selected.
Instead of putting individual on click listeners on every image button of the pallet we will make use of a
new attribute called the tag.
We will later keep a variable which will keep charge of which color is currently selected in the pallet and
will change colors accordingly

Frame Layout -Allows us to have views on top of each other.works like a stack,which ever view is mentioned sabse upar
becomes the lowermost layer of the frame.Here we use frame layout to have an image view and the drawing view
on top of each other,so that we can draw on images and stuff.
*/