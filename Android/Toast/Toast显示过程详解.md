
版权声明：本文为博主原创文章，转载请注明出处http://blog.csdn.net/andersen_/article/details/49058785

##前言
   上次看到有人说Toast属于UI操作，只能在主线程中使用。其实不是这样的，Toast是特殊的UI操作，由系统管理，我们只需在线程中创建MessageQueue和Looper让Toast能够回调当前线程即可在任意线程使用，主线程由于属于ActivityThread，默认创建了消息队列，所以可以直接使用Toast，Thread中则需要：

	Looper.prepare();
	Toast.makeText(context,text,duration).show();
	Looper.loop();
>注意：
>1、线程的Handler、Looper和MessageQueue将在另一篇文章中讲解，http://blog.csdn.net/andersen_/article/details/49109303
>2、文中大量涉及到进程间通讯机制Binder的使用，建议先看些Binder的资料
>3、文中源码过长的有省略，想要阅读源码的朋友可以在这两个网站上查看：1、http://androidxref.com 2、 http://www.grepcode.com/

下面结合源码详细介绍一下Toast的显示之路，为了方便理解，先展示UML图及粗糙结构图：

##总结
因为每个Toast显示时间可能冲突，因此需要一个管理者来统一管理他们，在一个队列中一个一个显示，所以要使用管理者（NMS）的binder引用将Toast入队，显示又要调用使用线程的binder引用，因此Toast的显示大量用到了进程间通信。
1、在使用线程中调用Toast.makeText(),创建一个Toast实例
2、调用toast.show();在这个方法中又调用getService()获取NotificationManagerService的binder引用，将toast和mTN（继承自ITransientNotification.Stub的TN类的实例，用来提供给NMS一个Binder引用，需要显示Toast时可以调用TN的方法）等信息enqueue(),插入NMS中的待显示序列。
3、轮到某个Toast显示时，在NMS中调用TN类的show()方法，在show方法中又通过handler在使用线程中直接获取WindowManagerImpl然后addView，将Toast添加在Window中。
4、显示的同时NMS也发送了一个延时消息，显示时间过后就会调用ＴＮ的hide()方法，通过handler在使用线程中调用WindowManagerImpl的removeVIew()方法删除该Toast

![Toast显示序列图](http://img.blog.csdn.net/20151012003327874)

![草图](http://img.blog.csdn.net/20151012003421378)

##源码分析

###Toast的创建

创建Toast我们都会，最简单的方法就是 makeText();
看一下源码：

	

```
public static Toast makeText(Context context, CharSequence text, @Duration int duration) {
		//创建一个Toast
        Toast result = new Toast(context);

        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(com.android.internal.R.layout.transient_notification, null);
        
		//Toast中要显示的TextView
        TextView tv = (TextView)v.findViewById(com.android.internal.R.id.message);
        tv.setText(text);
        result.mNextView = v;
		//Toast显示的时间
        result.mDuration = duration;

        return result;
    }
```

重要语句已注释，现在我们知道在调用makeText时返回值就是创建完毕的Toast。下面看show()方法。
###加入NMS的Toast队列
系统中有可能很多程序都想要显示Toast，如果同时出现就失去了原有的作用，因此必须有一个管理者来统一管理所有需要显示的Toast，这个管理者就是系统服务之一：NotificationManagerService

```
 public void show() {
        if (mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }
		//获取NotificationManagerService的客户端
        INotificationManager service = getService();
        
        String pkg = mContext.getOpPackageName();
        
        //tn是系统将会调用的回调，来真正实现将Toast显示在当前Window
        TN tn = mTN;
        tn.mNextView = mNextView;

        try {
            service.enqueueToast(pkg, tn, mDuration);
        } catch (RemoteException e) {
            // Empty
        }
    }
```
	
看到这里，我们发现了两个重要的类，INotificationManager和TN。

    INotificationManager：（AIDL声明的接口，具体实现在INotificationManager.Stub里,而系统服务NotificationManagerService正式继承了INOtificationManager.Stub）是系统服务NMS在本地的binder引用，也就是客户端，我们要跨进程调用NMS的方法就要用到它。获取方法：
		  
	static private INotificationManager getService() {
        if (sService != null) {
            return sService;
        }
        sService = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        return sService;
    }
    
    TN:是真正实现将Toast显示到当前Window的工具，它继承自ITransientNotification.Stub，也就是一个服务端，NMS可以调用TN的binder引用来调用TN的show()和hide()方法来显示隐藏Toast。
    
   通过调用NotificationManagerService的enqueue()方法,很明显是要把包名，回调、显示时间发送过去统一排队，一个一个显示。下面看看排队时发生了什么
###NMS回调TN
排队时发生了什么呢，贴上源码：重要的就是这个同步的部分，前面省略

```
	synchronized (mToastQueue) {
		    //这里获取请求线程的pid和Uid
            int callingPid = Binder.getCallingPid();
            long callingId = Binder.clearCallingIdentity();
            try {
                ToastRecord record;
                int index = indexOfToastLocked(pkg, callback);
                // If it's already in the queue, we update it in place, we don't
                // move it to the end of the queue.
                //英文注释很清楚了，这个index表示这条Toast是不是已经存在在队列中且尚未显示，如果是，则刷新。
                if (index >= 0) {
                    record = mToastQueue.get(index);
                    record.update(duration);
                } else {
                    // Limit the number of toasts that any given package except the android
                    // package can enqueue.  Prevents DOS attacks and deals with leaks.
                    if (!isSystemToast) {
                        int count = 0;
                        final int N = mToastQueue.size();
                        for (int i=0; i<N; i++) {
                             final ToastRecord r = mToastQueue.get(i);
                             if (r.pkg.equals(pkg)) {
                                 count++;
                                 if (count >= MAX_PACKAGE_NOTIFICATIONS) {
                                     Slog.e(TAG, "Package has already posted " + count
                                            + " toasts. Not showing more. Package=" + pkg);
                                     return;
                                 }
                             }
                        }
                    }

                    record = new ToastRecord(callingPid, pkg, callback, duration);
                    mToastQueue.add(record);
                    index = mToastQueue.size() - 1;
                    keepProcessAliveLocked(callingPid);
                }
                // If it's at index 0, it's the current toast.  It doesn't matter if it's
                // new or just been updated.  Call back and tell it to show itself.
                // If the callback fails, this will remove it from the list, so don't
                // assume that it's valid after this.
                if (index == 0) {
                    showNextToastLocked();
                }
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
```
这其中的内容主要就是判断一下Toast在index中位置是不是0，如果是，就显示，不是，就刷新自己。下面还要看看showNextToastLocked();

```java
private void showNextToastLocked() {
        ToastRecord record = mToastQueue.get(0);
        while (record != null) {
            if (DBG) Slog.d(TAG, "Show pkg=" + record.pkg + " callback=" + record.callback);
            try {
				//正主终于来了，当年存入ToastRecord的回调终于派上用场，这里调用的，就是TN的show方法。
                record.callback.show();
				//计算好时间，显示既定时间之后就删除，这个函数一会儿再说。
                scheduleTimeoutLocked(record);
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Object died trying to show notification " + record.callback
                        + " in package " + record.pkg);
                // remove it from the list and let the process die
                int index = mToastQueue.indexOf(record);
                if (index >= 0) {
                    mToastQueue.remove(index);
                }
                keepProcessAliveLocked(record.pid);
                if (mToastQueue.size() > 0) {
                    record = mToastQueue.get(0);
                } else {
                    record = null;
                }
            }
        }
    }
```
刚才终于通过binder引用回调了TN的show方法，那么我们回过头去看：
###TN调用WMS显示Toast
代码很长，但是总结一下其实就是在请求线程中执行handleShow()方法，利用WindowManager的addView()方法，在Window中添加toast。
```java
@Override
public void show() {
    if (localLOGV) Log.v(TAG, "SHOW: " + this);
    mHandler.post(mShow);
}
```

```java
final Runnable mShow = new Runnable() {
    @Override
    public void run() {
        handleShow();
    }
};
```

```
public void handleShow() {
            if (localLOGV) Log.v(TAG, "HANDLE SHOW: " + this + " mView=" + mView
                    + " mNextView=" + mNextView);
            if (mView != mNextView) {
                // remove the old view if necessary
                handleHide();
                mView = mNextView;
                Context context = mView.getContext().getApplicationContext();
                String packageName = mView.getContext().getOpPackageName();
                if (context == null) {
                    context = mView.getContext();
                }
                mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                // We can resolve the Gravity here by using the Locale for getting
                // the layout direction
                final Configuration config = mView.getContext().getResources().getConfiguration();
                final int gravity = Gravity.getAbsoluteGravity(mGravity, config.getLayoutDirection());
                mParams.gravity = gravity;
                if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
                    mParams.horizontalWeight = 1.0f;
                }
                if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
                    mParams.verticalWeight = 1.0f;
                }
                mParams.x = mX;
                mParams.y = mY;
                mParams.verticalMargin = mVerticalMargin;
                mParams.horizontalMargin = mHorizontalMargin;
                mParams.packageName = packageName;
                if (mView.getParent() != null) {
                    if (localLOGV) Log.v(TAG, "REMOVE! " + mView + " in " + this);
                    mWM.removeView(mView);
                }
                if (localLOGV) Log.v(TAG, "ADD! " + mView + " in " + this);
                mWM.addView(mView, mParams);
                trySendAccessibilityEvent();
            }
        }
```

###NMS回调TN
在NMS中又是怎么计时的呢，还记得刚才在回调TN的show()方法之后调用的方法吗？scheduleTimeoutLocked();

```
 private void scheduleTimeoutLocked(ToastRecord r)
    {
        mHandler.removeCallbacksAndMessages(r);
        Message m = Message.obtain(mHandler, MESSAGE_TIMEOUT, r);
        long delay = r.duration == Toast.LENGTH_LONG ? LONG_DELAY : SHORT_DELAY;
        mHandler.sendMessageDelayed(m, delay);
    }
```
很简单的一个函数，取需要显示的时间delay，然后发送一个延迟delay时间的消息，并且将ToastRecorder对象传递过去，这个消息是干嘛呢，继续往下看：

```
private final class WorkerHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MESSAGE_TIMEOUT:
                    handleTimeout((ToastRecord)msg.obj);
                    break;
            }
        }
    }
```
收到消息后，执行handleTimeOut方法，并且将该ToastRecorder对象传递过去：

```
 private void handleTimeout(ToastRecord record)
    {
        if (DBG) Slog.d(TAG, "Timeout pkg=" + record.pkg + " callback=" + record.callback);
        synchronized (mToastQueue) {
            int index = indexOfToastLocked(record.pkg, record.callback);
            if (index >= 0) {
                cancelToastLocked(index);
            }
        }
    }

```
直接看最后一句，cancelToastLocked，在一个Toast显示的时候其他Toast不能显示，现在肯定是先取消这个锁，继续看：

```
private void cancelToastLocked(int index) {
        ToastRecord record = mToastQueue.get(index);
        try {
			//在这里调用了TN的hide()方法
            record.callback.hide();
        } catch (RemoteException e) {
            Slog.w(TAG, "Object died trying to hide notification " + record.callback
                    + " in package " + record.pkg);
            // don't worry about this, we're about to remove it from
            // the list anyway
        }
        mToastQueue.remove(index);
        keepProcessAliveLocked(record.pid);
        if (mToastQueue.size() > 0) {
            // Show the next one. If the callback fails, this will remove
            // it from the list, so don't assume that the list hasn't changed
            // after this point.
            showNextToastLocked();
        }
    }
```
在这个函数里面，首先是调用TN的hide()方法，隐藏显示完毕的Toast，然后判断还有没有需要显示的Toast，如果有的话，继续显示下一条。

###TN调用WMS删除Toast

到这里已经差不多了，TN的hide被客户端调用，发生的情况和show()差不多，也是在请求线程中通过WindowManager，remove掉刚才添加的Toast。

```
@Override
        public void hide() {
            if (localLOGV) Log.v(TAG, "HIDE: " + this);
            mHandler.post(mHide);
        }
```

```
final Runnable mHide = new Runnable() {
            @Override
            public void run() {
                handleHide();
                // Don't do this in handleHide() because it is also invoked by handleShow()
                mNextView = null;
            }
        };
```

```
 public void handleHide() {
            if (localLOGV) Log.v(TAG, "HANDLE HIDE: " + this + " mView=" + mView);
            if (mView != null) {
                // note: checking parent() just to make sure the view has
                // been added...  i have seen cases where we get here when
                // the view isn't yet added, so let's try not to crash.
                if (mView.getParent() != null) {
                    if (localLOGV) Log.v(TAG, "REMOVE! " + mView + " in " + this);
                    mWM.removeView(mView);
                }

                mView = null;
            }
        }
```
