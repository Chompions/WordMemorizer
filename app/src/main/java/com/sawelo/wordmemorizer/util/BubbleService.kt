package com.sawelo.wordmemorizer.util

import com.sawelo.wordmemorizer.R
import com.torrydo.floatingbubbleview.FloatingBubble
import com.torrydo.floatingbubbleview.FloatingBubbleService

class BubbleService: FloatingBubbleService() {
    override fun setupBubble(action: FloatingBubble.Action): FloatingBubble.Builder {
        return FloatingBubble.Builder(this)

            // set bubble icon, currently accept only drawable and bitmap
            .setBubble(R.drawable.ic_launcher_foreground)
            // set bubble's width/height
            .setBubbleSizeDp(60, 60)
            // set style for bubble, by default bubble use fade animation
            .setBubbleStyle(null)
            // set start point of bubble, (x=0, y=0) is top-left
            .setStartPoint(0, 0)
            // enable auto animate bubble to the left/right side when release, true by default
            .enableAnimateToEdge(true)

            // set close-bubble icon, currently accept only drawable and bitmap
            .setCloseBubble(R.drawable.ic_baseline_close_24)
            // set close-bubble's width/height
            .setCloseBubbleSizeDp(60, 60)
            // set style for close-bubble, null by default
            .setCloseBubbleStyle(null)
            // show close-bubble, true by default
            .enableCloseIcon(true)

            .addFloatingBubbleTouchListener(object : FloatingBubble.TouchEvent {

                override fun onClick() {
                    println("WWFUHFWIUWFH")
                }
            })
            .setAlpha(1f)
    }
}