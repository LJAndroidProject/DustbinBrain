package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import mcv.facepass.types.FacePassFace;


/**
 * Created by wangjingyi on 28/03/2017.
 */

public class FaceView extends View {
    private List<String> ids;
    private List<String> yaws;
    private List<String> pitchs;
    private List<String> rolls;
    private List<String> blurs;
    private List<String> smiles;
    private List<Rect> rect;
    private Paint paint = new Paint();
    private Paint idPaint = new Paint();
    private Paint posePaint = new Paint();
    private Paint backPaint = new Paint();
    public static int circleDimater = 461;

    private void initData() {
        initPaint();
        ids = new ArrayList<String>();
        yaws = new ArrayList<>();
        pitchs = new ArrayList<>();
        rolls = new ArrayList<>();
        blurs = new ArrayList<>();
        smiles = new ArrayList<>();
        rect = new ArrayList<Rect>();
        paint.setARGB(122, 255, 255, 255);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10.0f);

        backPaint.setARGB(122, 255, 255, 255);
        backPaint.setStyle(Paint.Style.FILL);

        idPaint.setARGB(255, 80, 80, 80);
        idPaint.setTextSize(40);

        posePaint.setARGB(255, 80, 80, 80);
        posePaint.setTextSize(25);
    }


    private Paint mCirclePaint;
    private Paint mFaceRectPaint;
    private Paint mFacePaint;
    private Rect mOutRect;
    private void initPaint() {
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeWidth(1);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.white));

        mFaceRectPaint = new Paint();
        mFaceRectPaint.setAntiAlias(true);
        mFaceRectPaint.setStrokeWidth(3);
        mFaceRectPaint.setColor(Color.GREEN);

        mFacePaint = new Paint();
        mFacePaint.setStyle(Paint.Style.STROKE);
        mFacePaint.setStrokeWidth(3);
        mFacePaint.setColor(Color.RED);

    }

    public FaceView(Context context) {
        super(context);
        initData();
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    public void addId(String label) {
        ids.add(label);
    }

    public void addYaw(String label) {
        yaws.add(label);
    }
    public void addPitch(String label) {
        pitchs.add(label);
    }
    public void addRoll(String label) {
        rolls.add(label);
    }
    public void addBlur(String label)  {
        blurs.add(label);
    }
    public void addSmile(String lable) {
        smiles.add(lable);
    }

    public void addRect(RectF rect) {
        Rect buffer = new Rect();
        buffer.left = (int) rect.left;
        buffer.top = (int) rect.top;
        buffer.right = (int) rect.right;
        buffer.bottom = (int) rect.bottom;
        this.rect.add(buffer);
    }

    public void clear() {
        rect.clear();
        ids.clear();
        yaws.clear();
        rolls.clear();
        blurs.clear();
        pitchs.clear();
        smiles.clear();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCycle(canvas);
        for (int i = 0; i < rect.size(); i++) {
            Rect r = rect.get(i);
            canvas.drawRect(r, paint);
            canvas.drawRect(r.right+5, r.top - 5, r.right + ids.get(i).length() * 25, r.top + 200, backPaint);
            canvas.drawText(ids.get(i), r.right + 5, r.top + 30, idPaint);
            canvas.drawText(yaws.get(i), r.right + 5, r.top + 60, posePaint);
            canvas.drawText(pitchs.get(i), r.right + 5, r.top + 93, posePaint);
            canvas.drawText(rolls.get(i), r.right + 5, r.top + 126, posePaint);
            canvas.drawText(blurs.get(i), r.right + 5, r.top + 159, posePaint);
            canvas.drawText(smiles.get(i), r.right + 5, r.top + 192, posePaint);
        }
        this.clear();

    }

    private void drawCycle(Canvas canvas) {
        int measuredHeight = getHeight();
        int measuredWidth = getWidth();
//        LogUtils.dTag("FaceView","measuredWidth:"+measuredWidth+",measuredHeight"+measuredHeight);
        canvas.drawCircle(measuredWidth/2,measuredHeight/2, circleDimater /2, mCirclePaint);
    }

    // 关键点否在圆内
    public boolean isInCircle(FacePassFace face, int headerExtra) {

        // 近似 头顶两个坐标
        PointF leftUpDot  = new PointF(face.rect.left,face.rect.top-headerExtra);
        PointF rightUpDot  = new PointF(face.rect.right,face.rect.top - headerExtra);
        PointF leftDownDot  = new PointF(face.rect.left,face.rect.bottom);
        PointF rightDownDot = new PointF(face.rect.right,face.rect.bottom);
        // 头顶不在圆内，并且y坐标超过了圆顶点, 则判定false
        if(!pointIsInCircle(leftUpDot) && leftUpDot.y < (getHeight()/2 - circleDimater /2) ){
            return false;
        }
        if(!pointIsInCircle(rightUpDot)&& rightUpDot.y < (getHeight()/2 - circleDimater /2)){
            return false;
        }
        if(!pointIsInCircle(leftDownDot) && leftDownDot.y > (getHeight()/2 - circleDimater /2+circleDimater)){
            return false;
        }
        if(!pointIsInCircle(rightDownDot)&& rightUpDot.y > (getHeight()/2 - circleDimater /2+circleDimater)){
            return false;
        }

        return true;
    }

    PointF circleCenter;
    // 点是否在圆内
    private boolean pointIsInCircle(PointF pointF){
        if(circleCenter == null)
            circleCenter = new PointF(getWidth() / 2, getHeight() / 2);
        return Math.pow((pointF.x - circleCenter.x), 2) + Math.pow((pointF.y - circleCenter.y), 2) <= Math.pow(circleDimater /2, 2);
    }
}
