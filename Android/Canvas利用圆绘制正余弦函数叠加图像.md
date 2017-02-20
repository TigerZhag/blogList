## 前言
---
  昨天看傅里叶变换的资料，看到这篇文章：[如果看了此文你还不懂傅里叶变换，就过来掐死我吧](http://blog.jobbole.com/70549/)，
  讲得很清晰，中间一幅图的美丽深深吸引了我。。。  
![正弦函数叠加图像](http://img.blog.csdn.net/20160316205905436)  
于是萌生了利用Canvas把它画下来的想法。可是正余弦函数的基础早已经忘得差不多了，
所以先画个大概，慢慢更新当作毕设前练手吧。现在的样子：  
![现在的粗糙样子](http://img.blog.csdn.net/20160316211335255)

>博客新手。。如果各位看官觉得条理比较乱请指教一下

---
## 准备工作

---
### 画圆相关
先自定义了一个Circle类，方便添加任意多个圆

```java
public class Circle {
    //主颜色
    private int color;
    //半径
    private int radius;
    //圆心
    private Point mCenterPoint;
    //圆上动点当前位置
    private Point mMovePoint;
    //当前动点移动角度
    private int mCurrentDegree;
    //动点角度增量
    private int mIncreaseDegree;
    //getter和setter省略
}
```

画圆之前要先弄清楚一个概念，弧度和角度，引用维基定义：
> 弧度：单位弧度定义为圆弧长度等于半径时的圆心角。  
> 角度：一周角分为360等份，每份定义为1度（1°）  

图片均引自：[这篇博客](https://github.com/GcsSloop/AndroidNote/blob/master/CustomView/Base/%5B2%5DAngleAndRadian.md)  
![弧度示意图](https://camo.githubusercontent.com/e8f7014184e559b0d2684c02b8faa0efe56d0184/687474703a2f2f7777332e73696e61696d672e636e2f6c617267652f30303558746469326a7731663173306733726367326a333038633064773379772e6a7067)
![弧度示意图](https://camo.githubusercontent.com/1a05c67f37e6e25212d403d78a6398e4395959b9/687474703a2f2f7777312e73696e61696d672e636e2f6c617267652f30303558746469326a77316631733066393735686d6a333038633064776d78682e6a7067)

大圆的相关参数

```java
    private List<Circle> circles = new ArrayList<>();
    
    //第一个大圆的相关参数
    private int mMainColor = 0xFF009688;
    private int mCircleStartDegree = 0;
    private int mCircleIncreaseDegree = 4;
    private Point mCircleCenter = new Point(dp2px(50),dp2px(20));
    private int mLargeRadius = dp2px(30);
```

### 画图像相关

```java
    //圆当前指向图像的点
    private Point mOriginWavePoint = new Point(dp2px(150),dp2px(100));
    //图像上的点的集合，用链表方便增删
    private int mWaveMaxLength = 180;
    private List<Point> mWavePoints = new LinkedList<>();

```
---

## 绘制
---
### 画圆
添加第一个大圆：

```java
private void init() {
        Circle circle = new Circle(mMainColor);
        circle.setmIncreaseDegree(mCircleIncreaseDegree);
        circle.setmCurrentDegree(mCircleStartDegree);
        circle.setRadius(mLargeRadius);
        circle.setmCenterPoint(mCircleCenter);

        circles.add(circle);
    }
```
添加圆：

```java
    public void addCircle(Circle circle){
        int mMinRadius = 2;
        if (circles.size() > 0){
            mMinRadius = circles.get(circles.size() - 1).getRadius();
            if (mMinRadius <= 1) return;
        }
        circle.setRadius(circles.get(circles.size() - 1).getRadius()/3);
        circle.setmIncreaseDegree(circles.get(circles.size() - 1).getmIncreaseDegree() * 3);
        circles.add(circle);
    }
```

画圆：

```java
    private void drawCircle(Canvas canvas) {
        canvas.save();
        initPaint();
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
            //依次设置大圆动点为小圆圆心
            if (i == 0) {
                circle.setmCenterPoint(new Point(dp2px(50),dp2px(100)));
            }else {
                circle.setmCenterPoint(circles.get(i - 1).getmMovePoint());
            }
            initCirclePaint();
            //根据圆的颜色设置画笔
            mPaint.setColor(circle.getColor());
            canvas.drawCircle(circle.getmCenterPoint().x,circle.getmCenterPoint().y,
                    circle.getRadius(),mPaint);
            //根据动点旋转角度求坐标
            int movePointX = (int) (circle.getmCenterPoint().x + circle.getRadius() * Math.cos(Math.toRadians(circle.getmCurrentDegree())));
            int movePointY = (int) (circle.getmCenterPoint().y + circle.getRadius() * Math.sin(Math.toRadians(circle.getmCurrentDegree())));
            circle.setmMovePoint(new Point(movePointX,movePointY));
            //重设当前旋转角度
            int currentDegree = (circle.getmCurrentDegree() - circle.getmIncreaseDegree()) % 360;
            circle.setmCurrentDegree(currentDegree);
            //绘制圆心到动点连线
            canvas.drawLine(circle.getmCenterPoint().x,circle.getmCenterPoint().y,
                    movePointX,movePointY,mPaint);
        }
        canvas.restore();
    }
```
### 画函数图像

```java
    private void drawWave(Canvas canvas) {
        //最外圈圆的动点坐标
        Point movePoint = circles.get(circles.size() - 1).getmMovePoint();
        //设置图像最左侧一点的纵坐标
        mOriginWavePoint.y = movePoint.y;
        //图像最长为180个像素
        if (mWavePoints.size() >= 180){
            mWavePoints.remove(0);
        }
        mWavePoints.add(new Point(mOriginWavePoint));
        //画一个点
        initPointPaint();
        canvas.drawPoint(mOriginWavePoint.x, mOriginWavePoint.y,mPaint);
        canvas.drawLine(movePoint.x,movePoint.y, mOriginWavePoint.x, mOriginWavePoint.y,mPaint);

        Path path = new Path();
        path.moveTo(mWavePoints.get(0).x,mWavePoints.get(0).y);
        for (int i = mWavePoints.size() - 1 ; i > 0; i --){
            path.lineTo(mWavePoints.get(i).x + mWavePoints.size() - i,mWavePoints.get(i).y);
        }
        canvas.drawPath(path,mPaint);
    }
```
---
附上源码：[源码](https://github.com/TigerZhag/FourierView)