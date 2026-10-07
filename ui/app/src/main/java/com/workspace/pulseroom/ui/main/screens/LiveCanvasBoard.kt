package com.workspace.pulseroom.ui.main.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import com.workspace.pulseroom.ui.main.model.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import com.workspace.pulseroom.theme.PulseRoomTheme
import androidx.compose.ui.graphics.drawscope.Stroke as DrawScopeStroke

@Composable
fun LiveCanvasBoard(){
    // 1. Puraani saari lines ka collection
    var completedStrokes by remember { mutableStateOf(listOf<Stroke>()) }
    // 2. Woh line jo user abhi (current moment par) draw kar raha hai

    var  currentStroke by remember { mutableStateOf<Stroke?>(null)}

    // Spec color for the canvas background
    val canvasBg = Color(0xFFFBF8F8)
    val drawColor = Color(0xFF101917)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasBg)
        // 3. Gesture Detection: Ungli ko track karna
            .pointerInput(Unit){
                detectDragGestures(
                    onDragStart = {offset->
                        // Jab ungli screen pe touch hui, ek naya stroke shuru karo
                        currentStroke = Stroke(
                            points = listOf(offset),
                            color = drawColor,
                            strokeWidth = 8f
                        )
                    },
                    onDrag = {change, dragAmount ->
                        // Drag hote time event ko consume karo taaki screen scroll na ho jaye
                        change.consume()

                        // Current stroke mein naye (x,y) points add karte jao
                        currentStroke?.let{stroke->
                            val newPoints = stroke.points + change.position
                            currentStroke = stroke.copy(points = newPoints)
                        }
                    },
                    onDragEnd = {
                        // Jab ungli uthayi, toh is stroke ko completed list mein daal do
                        currentStroke?.let { stroke ->
                            completedStrokes = completedStrokes + stroke
                            currentStroke = null // Nayi line ke liye current ko clear kar do
                        }
                    }
                )
            }
    ){
        // 4. Drawing Logic: State ko padh kar screen par paint karna
        // Pehle saari completed lines draw karo
        completedStrokes.forEach {stroke ->
            drawPath(
                path = createPathFromPoints(stroke.points),
                color = stroke.color,
                style = DrawScopeStroke(
                    width = stroke.strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Fir wo line draw karo jo abhi ban rahi hai (live)
        currentStroke?.let {stroke ->
            drawPath(
                path = createPathFromPoints(stroke.points),
                color = stroke.color,
                style = DrawScopeStroke(
                    width = stroke.strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

// Ek helper function jo (x,y) points ki list ko Compose ke 'Path' object mein badalta hai
private fun createPathFromPoints(points: List<Offset>): Path {
    val path = Path()
    if (points.isNotEmpty()) {
        path.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
    }
    return path
}

@Preview(showBackground = true)
@Composable
private fun LiveCanvasBoardPreview() {
    PulseRoomTheme {
        LiveCanvasBoard()
    }
}

