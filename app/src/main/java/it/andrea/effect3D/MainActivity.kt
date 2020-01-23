package it.andrea.effect3D

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var layerAnimationHelper: LayerAnimationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layerAnimationHelper = LayerAnimationHelper(this).apply {
            val viewLayers = ArrayList<View>()
            viewLayers.add(tv_test)

            addLayers(viewLayers)

            lifecycle.addObserver(this)
        }
    }


    override fun onDestroy() {
        lifecycle.removeObserver(layerAnimationHelper)
        super.onDestroy()
    }
}
