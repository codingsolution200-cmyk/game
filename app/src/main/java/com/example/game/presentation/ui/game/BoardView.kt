package com.example.game.presentation.ui.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var playerPositions = mutableListOf(1, 1, 1, 1)
    var playerCount = 2
    private val boardBounds = RectF()

    // ── Paints ──────────────────────────────────────────
    private val boardPaint        = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint       = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 1.2f
    }
    private val glowBorderPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 2.5f
    }
    private val cellNumBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val cellNumRingPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val cellNumPaint      = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }
    private val starPaint         = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val crownPaint        = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val shadowPaint       = Paint(Paint.ANTI_ALIAS_FLAG)

    // Player
    private val playerFillPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val playerRingPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2f }
    private val playerGlowPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val playerNumPaint    = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }

    // Snake
    private val snakeOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val snakeBodyPaint    = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val snakeShimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val snakeBellyPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val snakeScalePaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val snakeHeadFill     = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val snakeHeadRing     = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2.5f }
    private val snakeMouthPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val snakeNostrilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val snakeEyeWhite     = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val snakeEyePupil     = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1A0000"); style = Paint.Style.FILL }
    private val snakeEyeShine     = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val snakeTonguePaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF1744"); style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND; strokeWidth = 2.2f
    }
    private val snakeTailPaint    = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    // Ladder
    private val ladderShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val ladderRailDark    = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val ladderRailMid     = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val ladderRailLight   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val ladderRungDark    = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val ladderRungLight   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val ladderCapFill     = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val ladderCapStroke   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1.8f }
    private val ladderCapShine    = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    // ── Colors ──────────────────────────────────────────
    private val cellEven  = Color.parseColor("#1B1B4B")
    private val cellOdd   = Color.parseColor("#13133A")
    private val cellSnake = Color.parseColor("#2D0F1E")
    private val cellLadr  = Color.parseColor("#0B2D14")
    private val cell100   = Color.parseColor("#2D2400")

    private val playerColors = intArrayOf(
        Color.parseColor("#FF4444"),
        Color.parseColor("#FFAA00"),
        Color.parseColor("#22DD66"),
        Color.parseColor("#BB66FF")
    )
    private val playerGlow = intArrayOf(
        Color.parseColor("#FF000066"),
        Color.parseColor("#FF880066"),
        Color.parseColor("#00FF4466"),
        Color.parseColor("#9900FF66")
    )

    private val snakeMain = listOf(
        "#FF2D55","#FF9500","#AF52DE","#30D158",
        "#FF6B35","#5856D6","#FF3B30","#34C759"
    )
    private val snakeDark = listOf(
        "#C0001E","#C05500","#6B00A8","#009933",
        "#C03C00","#2C2AA0","#C00000","#009922"
    )
    private val snakeLight = listOf(
        "#FF8099","#FFCC80","#D9A0FF","#80EFB0",
        "#FFB399","#9998EE","#FF9090","#90EFB0"
    )

    // ── FIXED: No conflicts, bottom row ladders use offset heads ──
    // Snakes: head → tail
    private val snakes = mapOf(
        99 to 21,
        87 to 24,
        73 to 44,
        62 to 19,
        49 to 11,
        36 to 6,
        27 to 5
    )
    // Ladders: bottom → top  (removed 87→94 conflict, moved 2→38 to 4→38, 7→14 to 6→25, fixed bottom row)
    private val ladders = mapOf(
        4  to 38,   // was 2→38, moved to 4 (cell 2 was too crowded)
        9  to 31,   // was 8→31, moved to 9
        16 to 26,   // was 15→26
        22 to 42,   // was 21→42
        28 to 84,
        51 to 67,
        71 to 91,
        78 to 98,
        3  to 20    // new small ladder bottom row to replace removed ones
    )

    // ── onDraw ──────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size     = minOf(width, height).toFloat()
        val cellSize = size / 10f
        val offsetX  = (width - size) / 2f
        val offsetY  = (height - size) / 2f

        boardBounds.set(offsetX, offsetY, offsetX + size, offsetY + size)

        cellNumPaint.textSize   = cellSize * 0.24f
        starPaint.textSize      = cellSize * 0.42f
        crownPaint.textSize     = cellSize * 0.38f
        playerNumPaint.textSize = cellSize * 0.20f

        canvas.save()
        canvas.translate(offsetX, offsetY)
        drawBoard(canvas, cellSize)
        drawCell100Special(canvas, cellSize)
        ladders.entries.forEachIndexed { i, (b, t) -> drawLadder(canvas, b, t, cellSize) }
        snakes.entries.forEachIndexed  { i, (h, t) -> drawSnake(canvas, h, t, cellSize, i) }
        drawCellNumbers(canvas, cellSize)
        drawPlayers(canvas, cellSize)
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    // ── Board ─────────────────────────────────────────────
    private fun drawBoard(canvas: Canvas, cs: Float) {
        for (row in 0..9) {
            for (col in 0..9) {
                val num = getCellNumber(row, col)
                val x   = col * cs
                val y   = row * cs

                boardPaint.color = when {
                    snakes.containsKey(num)  -> cellSnake
                    ladders.containsKey(num) -> cellLadr
                    num == 100               -> cell100
                    else -> if ((row + col) % 2 == 0) cellEven else cellOdd
                }
                canvas.drawRect(x, y, x + cs, y + cs, boardPaint)

                // Subtle corner glow
                val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        x + cs * 0.2f, y + cs * 0.2f, cs * 0.8f,
                        Color.argb(28, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(x, y, x + cs, y + cs, cornerPaint)

                borderPaint.color = Color.parseColor("#2A2A60")
                canvas.drawRect(x, y, x + cs, y + cs, borderPaint)

                when {
                    snakes.containsKey(num) -> {
                        glowBorderPaint.color = Color.parseColor("#FF2D5540")
                        canvas.drawRect(x+1f, y+1f, x+cs-1f, y+cs-1f, glowBorderPaint)
                    }
                    ladders.containsKey(num) -> {
                        glowBorderPaint.color = Color.parseColor("#22C55E40")
                        canvas.drawRect(x+1f, y+1f, x+cs-1f, y+cs-1f, glowBorderPaint)
                    }
                }

            }
        }
    }

    private fun drawCellNumbers(canvas: Canvas, cs: Float) {
        cellNumRingPaint.strokeWidth = cs * 0.025f
        for (row in 0..9) {
            for (col in 0..9) {
                val num = getCellNumber(row, col)
                if (num == 100) continue

                val x = col * cs
                val y = row * cs
                val badgeLeft = x + cs * 0.08f
                val badgeTop = y + cs * 0.06f
                val badgeWidth = if (num >= 10) cs * 0.34f else cs * 0.24f
                val badgeHeight = cs * 0.2f
                val badgeRect = RectF(
                    badgeLeft,
                    badgeTop,
                    badgeLeft + badgeWidth,
                    badgeTop + badgeHeight
                )
                val radius = cs * 0.07f

                val (badgeColor, ringColor, textColor) = when {
                    snakes.containsKey(num) -> Triple(
                        Color.argb(225, 72, 12, 26),
                        Color.parseColor("#FF8FAB"),
                        Color.parseColor("#FFD4DE")
                    )
                    ladders.containsKey(num) -> Triple(
                        Color.argb(225, 8, 49, 26),
                        Color.parseColor("#5EF090"),
                        Color.parseColor("#D9FFE8")
                    )
                    else -> Triple(
                        Color.argb(205, 22, 26, 70),
                        Color.parseColor("#6772C8"),
                        Color.parseColor("#E6EBFF")
                    )
                }

                cellNumBadgePaint.color = badgeColor
                cellNumRingPaint.color = ringColor
                canvas.drawRoundRect(badgeRect, radius, radius, cellNumBadgePaint)
                canvas.drawRoundRect(badgeRect, radius, radius, cellNumRingPaint)

                cellNumPaint.color = textColor
                canvas.drawText(
                    num.toString(),
                    badgeRect.centerX(),
                    badgeRect.centerY() + cellNumPaint.textSize * 0.3f,
                    cellNumPaint
                )
            }
        }
    }

    private fun drawCell100Special(canvas: Canvas, cs: Float) {
        val pos   = getCellCenter(100, cs)
        val glowR = cs * 0.40f
        val gPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(pos.x, pos.y, glowR,
                Color.parseColor("#FFD70050"), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(pos.x, pos.y, glowR, gPaint)
        starPaint.textSize  = cs * 0.36f
        crownPaint.textSize = cs * 0.30f
        canvas.drawText("⭐", pos.x, pos.y + cs * 0.28f, starPaint)
        canvas.drawText("👑", pos.x, pos.y - cs * 0.02f, crownPaint)
    }

    // ── Real Snake ─────────────────────────────────────────
    private fun drawSnake(canvas: Canvas, head: Int, tail: Int, cs: Float, idx: Int) {
        val hPos  = getCellCenter(head, cs)
        val tPos  = getCellCenter(tail, cs)
        val mainC = Color.parseColor(snakeMain[idx % snakeMain.size])
        val darkC = Color.parseColor(snakeDark[idx % snakeDark.size])
        val lightC= Color.parseColor(snakeLight[idx % snakeLight.size])
        val bodyW = cs * 0.15f
        val path  = buildSnakePath(hPos, tPos, cs)

        // Glow
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.style = Paint.Style.STROKE; it.strokeCap = Paint.Cap.ROUND; it.strokeJoin = Paint.Join.ROUND
            it.strokeWidth = bodyW + cs * 0.14f
            it.color = Color.argb(34, Color.red(mainC), Color.green(mainC), Color.blue(mainC))
            canvas.drawPath(path, it)
        }
        // Outline
        snakeOutlinePaint.color = darkC; snakeOutlinePaint.strokeWidth = bodyW + cs * 0.055f
        canvas.drawPath(path, snakeOutlinePaint)
        // Body
        snakeBodyPaint.shader = LinearGradient(
            hPos.x, hPos.y, tPos.x, tPos.y,
            intArrayOf(lightC, mainC, darkC),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        snakeBodyPaint.strokeWidth = bodyW
        canvas.drawPath(path, snakeBodyPaint)
        snakeBodyPaint.shader = null
        // Belly line
        snakeBellyPaint.color = Color.argb(160, 255, 244, 208)
        snakeBellyPaint.strokeWidth = bodyW * 0.26f
        canvas.drawPath(path, snakeBellyPaint)
        // Shimmer
        snakeShimmerPaint.color = Color.argb(80, 255, 255, 255)
        snakeShimmerPaint.strokeWidth = bodyW * 0.14f
        canvas.drawPath(path, snakeShimmerPaint)

        drawScales(canvas, path, hPos, tPos, cs, darkC, lightC, bodyW)

        // Tail tip
        val tailR = cs * 0.05f
        snakeTailPaint.color = darkC
        canvas.drawCircle(tPos.x + cs * 0.02f, tPos.y + cs * 0.025f, tailR + cs * 0.015f, snakeTailPaint)
        snakeTailPaint.color = lightC
        canvas.drawCircle(tPos.x, tPos.y, tailR, snakeTailPaint)

        drawSnakeHead(canvas, hPos, tPos, cs, mainC, darkC, lightC)
    }

    private fun buildSnakePath(hPos: PointF, tPos: PointF, cs: Float): Path {
        val path = Path()
        path.moveTo(hPos.x, hPos.y)
        val dx   = tPos.x - hPos.x
        val dy   = tPos.y - hPos.y
        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val wave = (cs * 0.72f).coerceAtMost(dist * 0.14f)
        val segments = 4

        for (i in 0 until segments) {
            val t0    = i / segments.toFloat()
            val t1    = (i + 1) / segments.toFloat()
            val sign  = if (i % 2 == 0) 1f else -1f
            val midT  = (t0 + t1) * 0.5f
            val taper = 0.55f + sin(midT * Math.PI).toFloat() * 0.55f
            val perpX = (-dy / dist) * wave * sign * taper
            val perpY = ( dx / dist) * wave * sign * taper
            val cp1x  = hPos.x + dx * (t0 + (t1 - t0) * 0.28f) + perpX * 0.42f
            val cp1y  = hPos.y + dy * (t0 + (t1 - t0) * 0.28f) + perpY * 0.42f
            val cp2x  = hPos.x + dx * (t0 + (t1 - t0) * 0.72f) + perpX * 0.62f
            val cp2y  = hPos.y + dy * (t0 + (t1 - t0) * 0.72f) + perpY * 0.62f
            path.cubicTo(cp1x, cp1y, cp2x, cp2y, hPos.x + dx*t1, hPos.y + dy*t1)
        }
        return path
    }

    private fun drawScales(
        canvas: Canvas,
        path: Path,
        hPos: PointF,
        tPos: PointF,
        cs: Float,
        darkC: Int,
        lightC: Int,
        bodyW: Float
    ) {
        val pathMeasure = PathMeasure(path, false)
        val length = pathMeasure.length
        val count = (length / (cs * 0.28f)).toInt().coerceIn(6, 14)
        val pos = FloatArray(2)
        val tan = FloatArray(2)

        for (i in 1 until count) {
            val distance = length * (i.toFloat() / count)
            pathMeasure.getPosTan(distance, pos, tan)

            val nx = -tan[1]
            val ny = tan[0]
            val spread = if (i % 2 == 0) 0.2f else -0.2f
            val scaleCx = pos[0] + nx * bodyW * spread
            val scaleCy = pos[1] + ny * bodyW * spread
            val progress = i / count.toFloat()
            val scaleR = cs * (0.022f + (1f - progress) * 0.014f)

            snakeScalePaint.color = Color.argb(115, Color.red(darkC), Color.green(darkC), Color.blue(darkC))
            canvas.drawCircle(scaleCx, scaleCy, scaleR, snakeScalePaint)

            snakeScalePaint.color = Color.argb(55, Color.red(lightC), Color.green(lightC), Color.blue(lightC))
            canvas.drawCircle(scaleCx - scaleR * 0.16f, scaleCy - scaleR * 0.16f, scaleR * 0.4f, snakeScalePaint)
        }
    }

    private fun drawSnakeHead(canvas: Canvas, hPos: PointF, tPos: PointF, cs: Float, mainC: Int, darkC: Int, lightC: Int) {
        val r = cs * 0.18f
        // Glow halo
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.shader = RadialGradient(hPos.x, hPos.y, r*1.8f,
                Color.argb(45, Color.red(mainC), Color.green(mainC), Color.blue(mainC)),
                Color.TRANSPARENT, Shader.TileMode.CLAMP)
            canvas.drawCircle(hPos.x, hPos.y, r*1.8f, it)
        }
        // Shadow
        shadowPaint.color = Color.argb(100,0,0,0)
        canvas.drawCircle(hPos.x + cs * 0.025f, hPos.y + cs * 0.03f, r, shadowPaint)
        // Head fill
        snakeHeadFill.shader = RadialGradient(
            hPos.x - r * 0.2f,
            hPos.y - r * 0.3f,
            r * 1.3f,
            lightC,
            mainC,
            Shader.TileMode.CLAMP
        )
        val headRect = RectF(hPos.x - r * 1.16f, hPos.y - r * 0.96f, hPos.x + r * 1.16f, hPos.y + r * 1.02f)
        canvas.drawOval(headRect, snakeHeadFill)
        // Ring
        snakeHeadRing.color = darkC
        snakeHeadRing.strokeWidth = cs * 0.028f
        canvas.drawOval(headRect, snakeHeadRing)
        // Shimmer
        Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.shader = RadialGradient(hPos.x - r*0.3f, hPos.y - r*0.3f, r*0.55f,
                Color.argb(75,255,255,255), Color.TRANSPARENT, Shader.TileMode.CLAMP)
            canvas.drawOval(RectF(hPos.x - r * 0.85f, hPos.y - r * 0.75f, hPos.x + r * 0.4f, hPos.y + r * 0.1f), it)
        }
        // Nostrils
        snakeNostrilPaint.color = Color.argb(150, 70, 0, 0)
        canvas.drawCircle(hPos.x - r * 0.3f, hPos.y + r * 0.18f, r * 0.08f, snakeNostrilPaint)
        canvas.drawCircle(hPos.x + r * 0.3f, hPos.y + r * 0.18f, r * 0.08f, snakeNostrilPaint)
        // Eyes
        val eOff = r * 0.5f; val eR = r * 0.2f; val pR = eR * 0.52f; val sR = eR * 0.22f
        listOf(-eOff, eOff).forEach { xOff ->
            val eyeCx = hPos.x + xOff
            val eyeCy = hPos.y - eOff * 0.2f
            canvas.drawCircle(eyeCx, eyeCy, eR, snakeEyeWhite)
            canvas.drawCircle(eyeCx, eyeCy, pR, snakeEyePupil)
            canvas.drawCircle(eyeCx + pR*0.35f, eyeCy - pR*0.35f, sR, snakeEyeShine)
        }
        // Mouth
        snakeMouthPaint.color = Color.argb(150, 95, 10, 22)
        snakeMouthPaint.strokeWidth = cs * 0.02f
        canvas.drawArc(
            RectF(hPos.x - r * 0.42f, hPos.y + r * 0.08f, hPos.x + r * 0.42f, hPos.y + r * 0.58f),
            15f,
            150f,
            false,
            snakeMouthPaint
        )
        // Tongue
        val dx   = tPos.x - hPos.x; val dy = tPos.y - hPos.y
        val dist = sqrt(dx*dx + dy*dy).coerceAtLeast(1f)
        val nx = dx/dist; val ny = dy/dist
        val tx = hPos.x - nx*r*1.15f; val ty = hPos.y - ny*r*1.05f
        val tex= tx - nx*r*1.15f;     val tey= ty - ny*r*1.15f
        canvas.drawLine(tx, ty, tex, tey, snakeTonguePaint)
        val px = -ny*r*0.35f; val py = nx*r*0.35f
        canvas.drawLine(tex, tey, tex - nx*r*0.4f + px, tey - ny*r*0.4f + py, snakeTonguePaint)
        canvas.drawLine(tex, tey, tex - nx*r*0.4f - px, tey - ny*r*0.4f - py, snakeTonguePaint)
    }

    // ── Real 3D Wooden Ladder ──────────────────────────────
    private fun drawLadder(canvas: Canvas, bottom: Int, top: Int, cs: Float) {
        val bPos = getCellCenter(bottom, cs)
        val tPos = getCellCenter(top, cs)
        val dx   = tPos.x - bPos.x; val dy = tPos.y - bPos.y
        val len  = sqrt(dx*dx + dy*dy).coerceAtLeast(1f)

        val gap = cs * 0.095f; val rW = cs * 0.062f; val ruW = cs * 0.045f
        val nx  = (-dy / len) * gap; val ny = (dx / len) * gap

        // Shadow
        ladderShadowPaint.color = Color.parseColor("#50000000")
        ladderShadowPaint.strokeWidth = rW * 2.4f + 5f
        for (sign in listOf(-1f, 1f)) {
            canvas.drawLine(bPos.x+nx*sign+4f, bPos.y+ny*sign+4f,
                tPos.x+nx*sign+4f, tPos.y+ny*sign+4f, ladderShadowPaint)
        }

        // Rails: dark → mid → light highlight
        for (sign in listOf(-1f, 1f)) {
            ladderRailDark.color  = Color.parseColor("#4A2800"); ladderRailDark.strokeWidth  = rW + 4f
            ladderRailMid.color   = Color.parseColor("#8B4513"); ladderRailMid.strokeWidth   = rW + 1f
            ladderRailLight.color = Color.parseColor("#CD8040"); ladderRailLight.strokeWidth = rW * 0.42f
            canvas.drawLine(bPos.x+nx*sign, bPos.y+ny*sign, tPos.x+nx*sign, tPos.y+ny*sign, ladderRailDark)
            canvas.drawLine(bPos.x+nx*sign, bPos.y+ny*sign, tPos.x+nx*sign, tPos.y+ny*sign, ladderRailMid)
            val hlX = -ny/gap * rW*0.38f; val hlY = nx/gap * rW*0.38f
            canvas.drawLine(bPos.x+nx*sign-hlX, bPos.y+ny*sign-hlY,
                tPos.x+nx*sign-hlX, tPos.y+ny*sign-hlY, ladderRailLight)
        }

        // Rungs
        val rungCount = (len / (cs * 0.40f)).toInt().coerceIn(3, 10)
        for (i in 1 until rungCount) {
            val t  = i.toFloat() / rungCount
            val rx = bPos.x + dx*t; val ry = bPos.y + dy*t
            ladderShadowPaint.strokeWidth = ruW + 3f
            canvas.drawLine(rx-nx*1.2f+3f, ry-ny*1.2f+3f, rx+nx*1.2f+3f, ry+ny*1.2f+3f, ladderShadowPaint)
            ladderRungDark.color = Color.parseColor("#5C2E00"); ladderRungDark.strokeWidth = ruW + 2.5f
            canvas.drawLine(rx-nx*1.2f, ry-ny*1.2f, rx+nx*1.2f, ry+ny*1.2f, ladderRungDark)
            ladderRungLight.color = Color.parseColor("#D2691E"); ladderRungLight.strokeWidth = ruW
            canvas.drawLine(rx-nx*1.2f, ry-ny*1.2f, rx+nx*1.2f, ry+ny*1.2f, ladderRungLight)
            // Rung shine
            Paint(Paint.ANTI_ALIAS_FLAG).also {
                it.color = Color.argb(55, 255,220,150)
                it.style = Paint.Style.STROKE; it.strokeWidth = ruW*0.28f; it.strokeCap = Paint.Cap.ROUND
                val hlX = -ny/gap * ruW*0.28f; val hlY = nx/gap * ruW*0.28f
                canvas.drawLine(rx-nx*1.1f-hlX, ry-ny*1.1f-hlY,
                    rx+nx*1.1f-hlX, ry+ny*1.1f-hlY, it)
            }
        }

        // Bottom cap — Gold
        val bR = cs * 0.092f
        ladderShadowPaint.strokeWidth = 3f
        canvas.drawCircle(bPos.x+1.5f, bPos.y+2f, bR+2f, ladderShadowPaint)
        ladderCapFill.color = Color.parseColor("#FFD700")
        ladderCapStroke.color = Color.parseColor("#A07800")
        canvas.drawCircle(bPos.x, bPos.y, bR, ladderCapFill)
        canvas.drawCircle(bPos.x, bPos.y, bR, ladderCapStroke)
        ladderCapShine.color = Color.argb(130, 255, 255, 200)
        canvas.drawCircle(bPos.x - bR*0.28f, bPos.y - bR*0.28f, bR*0.36f, ladderCapShine)

        // Top cap — Emerald
        val tR = cs * 0.092f
        ladderCapFill.color = Color.parseColor("#00CC66")
        ladderCapStroke.color = Color.parseColor("#006633")
        canvas.drawCircle(tPos.x, tPos.y, tR, ladderCapFill)
        canvas.drawCircle(tPos.x, tPos.y, tR, ladderCapStroke)
        ladderCapShine.color = Color.argb(130, 180, 255, 200)
        canvas.drawCircle(tPos.x - tR*0.28f, tPos.y - tR*0.28f, tR*0.36f, ladderCapShine)
    }

    // ── Players ────────────────────────────────────────────
    private fun drawPlayers(canvas: Canvas, cs: Float) {
        for (i in 0 until playerCount) {
            val center = getCellCenter(playerPositions[i], cs)
            val off    = getPlayerOffset(i, cs)
            val cx     = center.x + off.x; val cy = center.y + off.y
            val r      = cs * 0.182f

            // Outer glow ring
            playerGlowPaint.color = playerGlow[i]; playerGlowPaint.strokeWidth = 7f
            canvas.drawCircle(cx, cy, r + 5f, playerGlowPaint)
            // Shadow
            shadowPaint.color = Color.argb(100, 0, 0, 0)
            canvas.drawCircle(cx + 2.5f, cy + 3.5f, r, shadowPaint)
            // Fill
            playerFillPaint.color = playerColors[i]
            canvas.drawCircle(cx, cy, r, playerFillPaint)
            // White ring
            playerRingPaint.color = Color.argb(200, 255, 255, 255)
            canvas.drawCircle(cx, cy, r, playerRingPaint)
            // Inner shimmer
            Paint(Paint.ANTI_ALIAS_FLAG).also {
                it.shader = RadialGradient(cx - r*0.3f, cy - r*0.35f, r*0.75f,
                    Color.argb(115, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP)
                canvas.drawCircle(cx, cy, r, it)
            }
            // Label
            canvas.drawText("P${i+1}", cx, cy + playerNumPaint.textSize * 0.38f, playerNumPaint)
        }
    }

    // ── Helpers ─────────────────────────────────────────────
    private fun getCellNumber(row: Int, col: Int): Int {
        val rfb = 9 - row
        return if (rfb % 2 == 0) rfb * 10 + col + 1 else rfb * 10 + (9 - col) + 1
    }

    private fun getCellCenter(n: Int, cs: Float): PointF {
        val idx = n - 1; val rfb = idx / 10
        val col = if (rfb % 2 == 0) idx % 10 else 9 - (idx % 10)
        val row = 9 - rfb
        return PointF(col * cs + cs / 2f, row * cs + cs / 2f)
    }

    private fun getPlayerOffset(i: Int, cs: Float) = when (i) {
        0    -> PointF(-cs * 0.19f, -cs * 0.19f)
        1    -> PointF( cs * 0.19f, -cs * 0.19f)
        2    -> PointF(-cs * 0.19f,  cs * 0.19f)
        else -> PointF( cs * 0.19f,  cs * 0.19f)
    }
}
